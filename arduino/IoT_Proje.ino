#include <DHT.h>
#include <WiFi.h>
#include <HTTPClient.h>

#define DHTPIN 4
#define DHTTYPE DHT22
#define LIGHT_PIN 35
#define SOUND_PIN 32
#define MQ135_PIN 34
#define TRIG_PIN 5
#define ECHO_PIN 18

const char* ssid      = "YOUR_WIFI_SSID";
const char* password  = "YOUR_WIFI_PASSWORD";
const char* serverUrl = "http://YOUR_SERVER_IP:8080/api/sensor";
// WiFi ve sunucu ayarlarını kendi değerlerinizle değiştirin.
// serverUrl için bilgisayarınızın yerel IP adresini kullanın (ipconfig ile öğrenin).
DHT dht(DHTPIN, DHTTYPE);

const unsigned long SEND_INTERVAL = 5000;

int okuGurultu() {
  int minVal = 4095, maxVal = 0;
  unsigned long t0 = millis();
  while (millis() - t0 < 50) {
    int v = analogRead(SOUND_PIN);
    if (v < minVal) minVal = v;
    if (v > maxVal) maxVal = v;
  }
  return maxVal - minVal;
}

float okuMesafe() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  long sure = pulseIn(ECHO_PIN, HIGH, 30000);
  if (sure == 0) return -1;
  return sure * 0.0343 / 2.0;
}

void setup() {
  Serial.begin(115200);
  delay(1000);
  dht.begin();
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);

  Serial.print("WiFi baglaniyor");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nBaglandi! ESP32 IP: " + WiFi.localIP().toString());
}

void loop() {
  float sicaklik = dht.readTemperature();
  float nem = dht.readHumidity();
  int isik = analogRead(LIGHT_PIN);
  int gurultu = okuGurultu();
  int havaKalitesi = analogRead(MQ135_PIN);
  float mesafe = okuMesafe();

  if (isnan(sicaklik)) sicaklik = 0;
  if (isnan(nem)) nem = 0;
  if (mesafe < 0) mesafe = 0;

  Serial.println("-----------------------------");
  Serial.print("Sicaklik: "); Serial.println(sicaklik);
  Serial.print("Nem: ");      Serial.println(nem);
  Serial.print("Isik: ");     Serial.println(isik);
  Serial.print("Gurultu: ");  Serial.println(gurultu);
  Serial.print("Hava Kalitesi: "); Serial.println(havaKalitesi);
  Serial.print("Mesafe: ");   Serial.println(mesafe);

  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(serverUrl);
    http.addHeader("Content-Type", "application/json");

    String json = "{";
    json += "\"temperature\":" + String(sicaklik, 1) + ",";
    json += "\"humidity\":" + String(nem, 1) + ",";
    json += "\"airQuality\":" + String(havaKalitesi) + ",";
    json += "\"noise\":" + String(gurultu) + ",";
    json += "\"light\":" + String(isik) + ",";
    json += "\"distance\":" + String(mesafe, 1);
    json += "}";

    int code = http.POST(json);
    Serial.print("POST -> "); Serial.print(code);
    Serial.print(" | "); Serial.println(json);
    http.end();
  } else {
    Serial.println("WiFi baglantisi yok!");
  }

  delay(SEND_INTERVAL);
}