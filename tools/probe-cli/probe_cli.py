#!/usr/bin/env python3
"""
Skinthesia probe CLI - bench diagnostics for the ESP32 skin probe.

Talks to the probe firmware (firmware/esp32-probe/SRC_CODE/SRC_CODE.ino) over
the same line-based TCP protocol the Android app uses, so the hardware can be
brought up and checked from a laptop without the phone in the loop.

    python probe_cli.py read                 # pH + hydration from the real probe
    python probe_cli.py read --sebum         # ...plus the optical sebum test
    python probe_cli.py send TEST_HYDRATION  # one raw command
    python probe_cli.py emulate              # local stand-in for the probe

Join the probe's Wi-Fi network ("Detector_Device") before using read/send.
Standard library only; Python 3.8+.
"""

import argparse
import random
import socket
import socketserver
import sys
import time

DEFAULT_HOST = "192.168.4.1"
DEFAULT_PORT = 5000

# The sebum test waits up to 10 s for skin contact, then samples for up to 15 s.
SEBUM_TIMEOUT_S = 30.0
DEFAULT_TIMEOUT_S = 4.0


class ProbeConnection:
    """One TCP session with the probe: one command line out, one line back."""

    def __init__(self, host, port, timeout):
        self._sock = socket.create_connection((host, port), timeout=timeout)
        self._sock.settimeout(timeout)
        self._reader = self._sock.makefile("r", encoding="ascii", newline="\n")
        self.greeting = self._read_greeting()

    def _read_greeting(self):
        # The firmware pushes "AUTO_PH:<value>" as soon as it accepts a client.
        previous = self._sock.gettimeout()
        self._sock.settimeout(0.5)
        try:
            return self._reader.readline().strip() or None
        except socket.timeout:
            return None
        finally:
            self._sock.settimeout(previous)

    def send(self, command, timeout=None):
        if timeout is not None:
            self._sock.settimeout(timeout)
        self._sock.sendall((command + "\n").encode("ascii"))
        line = self._reader.readline()
        if not line:
            raise ConnectionError("probe closed the connection")
        return line.strip()

    def close(self):
        try:
            self._reader.close()
        finally:
            self._sock.close()

    def __enter__(self):
        return self

    def __exit__(self, *exc):
        self.close()


def _connect(args):
    try:
        return ProbeConnection(args.host, args.port, args.timeout)
    except OSError as error:
        sys.exit(
            f"Could not reach the probe at {args.host}:{args.port} ({error}).\n"
            "Is the laptop joined to the probe's Wi-Fi network?"
        )


def _row(label, value, unit=""):
    print(f"  {label:<18} {value}{(' ' + unit) if unit else ''}")


def cmd_read(args):
    with _connect(args) as probe:
        print(f"Connected to {args.host}:{args.port}")
        if probe.greeting:
            _row("Greeting", probe.greeting)

        ph = probe.send("TEST_PH")
        _row("pH", ph)

        hydration = probe.send("TEST_HYDRATION")
        if hydration == "HYDRATION_ERROR":
            _row("Hydration", "no echo received (check probe contact)")
        else:
            _row("Hydration distance", hydration, "cm")

        if args.sebum:
            print("  Place the probe on skin for the sebum test...")
            started = time.monotonic()
            sebum = probe.send("TEST_SEBUM", timeout=SEBUM_TIMEOUT_S)
            _row("Sebum type", sebum, f"({time.monotonic() - started:.1f} s)")


def cmd_send(args):
    timeout = SEBUM_TIMEOUT_S if args.command.upper() == "TEST_SEBUM" else None
    with _connect(args) as probe:
        if probe.greeting:
            print(f"< {probe.greeting}")
        print(f"> {args.command}")
        print(f"< {probe.send(args.command, timeout=timeout)}")


class _EmulatedProbeHandler(socketserver.StreamRequestHandler):
    """Answers the firmware's commands with plausible values. For development only."""

    def handle(self):
        peer = "%s:%s" % self.client_address
        print(f"[emulator] client connected: {peer}")
        try:
            self._serve()
        except (ConnectionResetError, ConnectionAbortedError, BrokenPipeError):
            pass
        print(f"[emulator] client disconnected: {peer}")

    def _serve(self):
        self._reply(f"AUTO_PH:{random.randint(55, 65) / 10:.1f}")
        for raw in self.rfile:
            request = raw.decode("ascii", "replace").strip().upper()
            if not request:
                continue
            if request == "TEST_PH":
                reply = f"{random.randint(55, 65) / 10:.1f}"
            elif request == "TEST_HYDRATION":
                reply = f"{random.uniform(2.0, 30.0):.2f}"
            elif request == "TEST_SEBUM":
                time.sleep(1.5)
                reply = random.choice(["DRY", "NORMAL", "OILY"])
            else:
                reply = "INVALID_REQUEST"
            print(f"[emulator] {request} -> {reply}")
            self._reply(reply)

    def _reply(self, line):
        self.wfile.write((line + "\r\n").encode("ascii"))


class _ThreadingServer(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True


def cmd_emulate(args):
    with _ThreadingServer((args.bind, args.port), _EmulatedProbeHandler) as server:
        print(f"[emulator] EMULATED probe listening on {args.bind}:{args.port} (Ctrl+C to stop)")
        print("[emulator] values are generated locally - this is not a real measurement")
        try:
            server.serve_forever()
        except KeyboardInterrupt:
            print("\n[emulator] stopped")


def build_parser():
    parser = argparse.ArgumentParser(
        prog="probe_cli",
        description="Bench diagnostics for the Skinthesia ESP32 skin probe.",
    )
    sub = parser.add_subparsers(dest="action", required=True)

    def add_target(p):
        p.add_argument("--host", default=DEFAULT_HOST, help=f"probe address (default {DEFAULT_HOST})")
        p.add_argument("--port", type=int, default=DEFAULT_PORT, help=f"probe TCP port (default {DEFAULT_PORT})")
        p.add_argument("--timeout", type=float, default=DEFAULT_TIMEOUT_S, help="per-command timeout in seconds")

    read = sub.add_parser("read", help="read pH and hydration (and optionally sebum)")
    add_target(read)
    read.add_argument("--sebum", action="store_true", help="also run the optical sebum test (needs skin contact)")
    read.set_defaults(func=cmd_read)

    send = sub.add_parser("send", help="send one raw protocol command")
    add_target(send)
    send.add_argument("command", help="e.g. TEST_PH, TEST_HYDRATION, TEST_SEBUM")
    send.set_defaults(func=cmd_send)

    emulate = sub.add_parser("emulate", help="run a local stand-in for the probe")
    emulate.add_argument("--bind", default="127.0.0.1", help="interface to listen on (default 127.0.0.1)")
    emulate.add_argument("--port", type=int, default=DEFAULT_PORT, help=f"TCP port (default {DEFAULT_PORT})")
    emulate.set_defaults(func=cmd_emulate)

    return parser


def main(argv=None):
    args = build_parser().parse_args(argv)
    args.func(args)


if __name__ == "__main__":
    main()
