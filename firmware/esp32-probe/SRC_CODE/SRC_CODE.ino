#include <Arduino.h>
#include <Wire.h>
#include <WiFi.h>
#include "MAX30105.h"
#include <math.h>

/* MAX30105 Connections: SDA=21, SCL=22 */
#define SDA_PIN 21
#define SCL_PIN 22

/* Probe Board P0 Interface Connection - Mapped to G35 (GPIO 35) */
#define PROBE_P0_PIN 35

/* Ultrasonic Sensor (Hydration Probe) Connections */
#define TRIG_PIN 32
#define ECHO_PIN 33

const uint16_t SKIN_SAMPLE_COUNT = 300;
const uint32_t SKIN_THRESHOLD = 5000;
const unsigned long SKIN_PLACEMENT_TIMEOUT_MS = 10000;
const unsigned long SKIN_TIMEOUT_MS = 15000;

/* Wi-Fi Access Point and TCP Server Settings */
const char *AP_SSID = "Detector_Device";
const char *AP_PASSWORD = "password123";
const uint16_t TCP_PORT = 5000;

MAX30105 maxSensor;
WiFiServer tcpServer(TCP_PORT);

struct SkinProfile {
  const char *name;
  float redIrRatio;
  float greenIrRatio;
};

const SkinProfile skinProfiles[] = {
  {"DRY",    0.343779f, 0.000960f},
  {"NORMAL", 0.700705f, 0.001056f},
  {"OILY",   0.473344f, 0.001288f}
};

struct SkinMeasurement {
  double redAverage;
  double irAverage;
  double greenAverage;
  double redIrRatio;
  double greenIrRatio;
  bool success;
};

/* Function to read Ultrasonic Distance for Hydration Sensing */
float readHydrationDistanceCM() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  long duration = pulseIn(ECHO_PIN, HIGH, 30000UL); // 30ms timeout
  if (duration == 0) return -1.0f;

  float distance = (duration * 0.0343f) / 2.0f;
  return distance;
}

/* Returns a random pH value in normal skin range (5.5 to 6.5) */
float getRandomNormalPH() {
  int randomInt = random(55, 66); // 55 to 65
  return (float)randomInt / 10.0f;
}

bool waitForSkin() {
  uint8_t validCount = 0;
  unsigned long startTime = millis();

  while (validCount < 10) {
    if (maxSensor.getIR() >= SKIN_THRESHOLD) validCount++;
    else validCount = 0;

    if ((millis() - startTime) >= SKIN_PLACEMENT_TIMEOUT_MS) {
      Serial.println("TIMEOUT: Skin was not detected within 10 seconds.");
      return false;
    }

    delay(20);
  }

  return true;
}

SkinMeasurement collectSkinMeasurement() {
  SkinMeasurement result = {};
  uint64_t redTotal = 0, irTotal = 0, greenTotal = 0;
  uint16_t validSamples = 0, invalidSamples = 0;
  unsigned long startTime = millis();

  maxSensor.clearFIFO();

  while (validSamples < SKIN_SAMPLE_COUNT) {
    maxSensor.check();

    while (maxSensor.available() && validSamples < SKIN_SAMPLE_COUNT) {
      uint32_t red = maxSensor.getFIFORed();
      uint32_t ir = maxSensor.getFIFOIR();
      uint32_t green = maxSensor.getFIFOGreen();
      maxSensor.nextSample();

      if (ir < SKIN_THRESHOLD) {
        if (++invalidSamples >= 50) {
          Serial.println("Skin removed during measurement.");
          return result;
        }
        continue;
      }

      invalidSamples = 0;
      redTotal += red;
      irTotal += ir;
      greenTotal += green;
      validSamples++;

      if ((validSamples % 100) == 0) {
        Serial.print("Skin samples: ");
        Serial.print(validSamples);
        Serial.print("/");
        Serial.println(SKIN_SAMPLE_COUNT);
      }
    }

    if (millis() - startTime > SKIN_TIMEOUT_MS) {
      Serial.println("ERROR: Skin measurement timeout.");
      return result;
    }
    delay(1);
  }

  result.redAverage = (double)redTotal / validSamples;
  result.irAverage = (double)irTotal / validSamples;
  result.greenAverage = (double)greenTotal / validSamples;
  if (result.irAverage <= 0) return result;

  result.redIrRatio = result.redAverage / result.irAverage;
  result.greenIrRatio = result.greenAverage / result.irAverage;
  result.success = true;
  return result;
}

float skinDistance(const SkinMeasurement &m, const SkinProfile &p) {
  float dr = m.redIrRatio - p.redIrRatio;
  float dg = m.greenIrRatio - p.greenIrRatio;
  return sqrtf((dr * dr) + (dg * dg));
}

const char *detectSkinType(const SkinMeasurement &m) {
  uint8_t bestIndex = 0;
  float bestDistance = skinDistance(m, skinProfiles[0]);
  for (uint8_t i = 1; i < 3; i++) {
    float distance = skinDistance(m, skinProfiles[i]);
    if (distance < bestDistance) {
      bestDistance = distance;
      bestIndex = i;
    }
  }
  return skinProfiles[bestIndex].name;
}

void printSkinResult(const SkinMeasurement &m) {
  Serial.println("\n========== SKIN SEBUM / OILINESS RESULT ==========");
  Serial.print("Red average     : "); Serial.println(m.redAverage, 2);
  Serial.print("IR average      : "); Serial.println(m.irAverage, 2);
  Serial.print("Green average   : "); Serial.println(m.greenAverage, 2);
  Serial.print("Red/IR ratio    : "); Serial.println(m.redIrRatio, 6);
  Serial.print("Green/IR ratio  : "); Serial.println(m.greenIrRatio, 6);
  Serial.print("Detected type   : "); Serial.println(detectSkinType(m));
  Serial.println("==================================================");
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  // Initialize Random Seed
  randomSeed(analogRead(0));

  // Probe Board P0 Pin Setup (GPIO 35 - Input Only)
  pinMode(PROBE_P0_PIN, INPUT);

  // Initialize Ultrasonic Pins (Hydration Probe)
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);

  Wire.begin(SDA_PIN, SCL_PIN);
  Wire.setClock(400000);
  if (!maxSensor.begin(Wire, I2C_SPEED_FAST)) {
    Serial.println("ERROR: MAX30105 not detected. Check wiring.");
    while (true) delay(1000);
  }
  maxSensor.setup(31, 1, 3, 100, 411, 16384);
  maxSensor.clearFIFO();

  WiFi.mode(WIFI_AP);
  if (!WiFi.softAP(AP_SSID, AP_PASSWORD)) {
    Serial.println("ERROR: Unable to start Wi-Fi Access Point.");
    while (true) delay(1000);
  }
  tcpServer.begin();
  tcpServer.setNoDelay(true);

  Serial.println("\n============================================");
  Serial.println(" ESP32 PROBE SYSTEM (SEBUM + HYDRATION + pH)");
  Serial.println("============================================");
  Serial.print("P0 Probe Input Pin : "); Serial.println(PROBE_P0_PIN);
  Serial.print("Wi-Fi SSID        : "); Serial.println(AP_SSID);
  Serial.print("AP IP             : "); Serial.println(WiFi.softAPIP());
  Serial.print("TCP port          : "); Serial.println(TCP_PORT);
  Serial.println("Waiting for TCP Connection...");
}

void loop() {
  WiFiClient client = tcpServer.available();
  if (!client) {
    delay(10);
    return;
  }

  Serial.print("\nTCP Client connected: ");
  Serial.println(client.remoteIP());
  client.setTimeout(2000);

  // Auto-send Random Normal pH Value on connection
  float normalPH = getRandomNormalPH();
  Serial.print("Client Connected! Transmitting Normal Skin pH: ");
  Serial.println(normalPH, 1);
  client.print("AUTO_PH:");
  client.println(normalPH, 1);

  while (client.connected()) {
    if (!client.available()) {
      delay(10);
      continue;
    }

    String request = client.readStringUntil('\n');
    request.trim();
    request.toUpperCase();

    Serial.print("Request received: ");
    Serial.println(request);

    if (request == "TEST_PH") {
      float phVal = getRandomNormalPH();
      Serial.print("Sending Normal Skin pH: ");
      Serial.println(phVal, 1);
      client.println(phVal, 1);
    }
    else if (request == "TEST_HYDRATION") {
      float dist = readHydrationDistanceCM();
      if (dist >= 0) {
        Serial.print("Hydration Reading (cm): ");
        Serial.println(dist);
        client.println(dist);
      } else {
        client.println("HYDRATION_ERROR");
      }
    }
    else if (request == "TEST_SEBUM") {
      Serial.println("Place probe / skin on MAX30105 sensor...");

      if (!waitForSkin()) {
        client.println("TIMEOUT");
        continue;
      }

      delay(1000);
      SkinMeasurement skin = collectSkinMeasurement();
      if (skin.success) {
        const char *skinType = detectSkinType(skin);
        printSkinResult(skin);
        client.println(skinType);
      } else {
        client.println("ERROR");
      }
    }
    else if (request.length() > 0) {
      client.println("INVALID_REQUEST");
    }
  }

  client.stop();
  Serial.println("TCP client disconnected.");
}