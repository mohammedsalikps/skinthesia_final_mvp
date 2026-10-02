# Skinthesia Probe Firmware (ESP32)

Firmware for the Skinthesia handheld skin probe. It runs on an ESP32, reads
the probe's sensors, and serves results to the Skinthesia app over the
probe's own Wi-Fi network.

```
firmware/esp32-probe/
└── SRC_CODE/
    └── SRC_CODE.ino      Arduino sketch (folder name must match the sketch name)
```

## Sensors

| Function              | Hardware                                 | Interface           | Output                         |
|-----------------------|------------------------------------------|---------------------|--------------------------------|
| Sebum and oiliness    | MAX30105 optical sensor (red, IR, green) | I²C, 400 kHz        | `DRY` / `NORMAL` / `OILY`      |
| Hydration             | Ultrasonic transducer                    | GPIO trigger + echo | Echo distance (cm)             |
| pH                    | Pending electrode integration            | n/a                 | pH value (see status below)    |
| Probe board P0        | Probe board interface, reserved          | GPIO 35 (input)     | Configured, not yet sampled    |

### Pin map

| Signal              | ESP32 pin |
|---------------------|-----------|
| MAX30105 SDA        | GPIO 21   |
| MAX30105 SCL        | GPIO 22   |
| Ultrasonic TRIG     | GPIO 32   |
| Ultrasonic ECHO     | GPIO 33   |
| Probe board P0      | GPIO 35 (input only) |

### MAX30105 configuration

`setup(ledBrightness=31, sampleAverage=1, ledMode=3, sampleRate=100, pulseWidth=411, adcRange=16384)`:
three LEDs (red, IR, green), 100 samples per second, 18-bit resolution.

Skin contact is detected when the IR reading stays at or above `5000` for 10
consecutive samples. A measurement averages 300 valid samples and classifies
skin type by nearest distance in (Red/IR, Green/IR) space to three calibrated
profiles:

| Profile  | Red/IR    | Green/IR  |
|----------|-----------|-----------|
| DRY      | 0.343779  | 0.000960  |
| NORMAL   | 0.700705  | 0.001056  |
| OILY     | 0.473344  | 0.001288  |

## Building and flashing

**Arduino IDE 2.x**

1. Install the ESP32 board package: *Boards Manager*, search **esp32 by Espressif Systems**.
2. Install the sensor library: *Library Manager*, search
   **SparkFun MAX3010x Pulse and Proximity Sensor Library**.
3. Open `SRC_CODE/SRC_CODE.ino`, select your ESP32 board (for example *ESP32 Dev Module*) and port.
4. Click **Upload**.

**arduino-cli**

```bash
arduino-cli core install esp32:esp32
arduino-cli lib install "SparkFun MAX3010x Pulse and Proximity Sensor Library"
arduino-cli compile --fqbn esp32:esp32:esp32 firmware/esp32-probe/SRC_CODE
arduino-cli upload  --fqbn esp32:esp32:esp32 -p <PORT> firmware/esp32-probe/SRC_CODE
```

## Verifying a probe

Open the serial monitor at **115200 baud**. A healthy boot prints the access
point name, the address `192.168.4.1` and TCP port `5000`, then
`Waiting for TCP Connection...`.

| Serial message                          | Meaning                                  |
|-----------------------------------------|------------------------------------------|
| `ERROR: MAX30105 not detected`          | Check SDA/SCL wiring and sensor power    |
| `ERROR: Unable to start Wi-Fi Access Point` | Radio failed to start; power-cycle   |
| `TIMEOUT: Skin was not detected...`     | No skin contact within 10 s of a sebum test |

Then join the `Detector_Device` network from a laptop and run the bench tool:

```bash
python tools/probe-cli/probe_cli.py read --sebum
```

## Development status

| Channel   | Status |
|-----------|--------|
| Sebum     | Live: optical measurement and classification on device. |
| Hydration | Live: ultrasonic echo distance; a calibrated hydration index is pending. |
| pH        | Protocol and app pipeline complete; the firmware currently returns a value in the normal skin range (`getRandomNormalPH()`) until a pH electrode is integrated. |

Before production units ship, give each device a unique Wi-Fi passphrase
(`AP_PASSWORD`) rather than the shared development value.

The wire protocol is specified in [docs/PROBE_PROTOCOL.md](../../docs/PROBE_PROTOCOL.md).
