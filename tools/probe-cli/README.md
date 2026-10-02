# probe-cli

Bench diagnostics for the Skinthesia ESP32 probe. It speaks the same TCP
protocol as the Android app ([docs/PROBE_PROTOCOL.md](../../docs/PROBE_PROTOCOL.md)),
so hardware can be brought up, checked and demonstrated from a laptop without
the phone in the loop.

Requires Python 3.8 or newer. Standard library only, so there is nothing to install.

## Usage

Join the probe's `Detector_Device` Wi-Fi network first, then:

```bash
python probe_cli.py read            # pH and hydration
python probe_cli.py read --sebum    # also the optical sebum test (hold probe on skin)
python probe_cli.py send TEST_PH    # send one raw command and print the reply
```

Example output:

```
Connected to 192.168.4.1:5000
  Greeting           AUTO_PH:6.1
  pH                 6.0
  Hydration distance 23.45 cm
  Place the probe on skin for the sebum test...
  Sebum type         NORMAL (4.2 s)
```

Options: `--host` (default `192.168.4.1`), `--port` (default `5000`),
`--timeout` per command in seconds (default `4`; sebum always allows 30 s).

## Emulator

`emulate` runs a local stand-in that answers the same commands with generated
values. Use it to develop and test clients when no probe is at hand. Its
output is clearly marked as emulated and is never a measurement.

```bash
python probe_cli.py emulate --port 5000
python probe_cli.py read --host 127.0.0.1    # in a second terminal
```
