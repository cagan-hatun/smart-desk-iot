# Akıllı Çalışma Masası - Dashboard

Spring Boot 4 (Java 21) + Thymeleaf + MySQL ile hazırlanmış IoT dashboard projesi.

## Gereksinimler

- **JDK 21+** (terminalde `java -version` ile kontrol et)
- **MySQL Server** (kurulu ve çalışır olmalı)
- **Maven** (terminalde `mvn -v` ile kontrol et — yoksa VSCode eklentisi kendi Maven'ını kullanır)

## MySQL Kurulumu

1. MySQL Server'ın çalıştığından emin ol (Windows'ta "Services" içinde MySQL80 servisi "Running" olmalı).
2. `src/main/resources/application.properties` dosyasını aç:
   - `spring.datasource.username` → kendi MySQL kullanıcı adın (genelde `root`)
   - `spring.datasource.password` → kendi MySQL şifren

> Veritabanını elle oluşturmana gerek yok — bağlantı URL'sindeki
> `createDatabaseIfNotExist=true` parametresi sayesinde `smartdesk` adlı
> veritabanı ilk çalıştırmada otomatik oluşturulur. Tablolar da
> `ddl-auto=update` sayesinde otomatik oluşur.

## VSCode Kurulumu

1. VSCode'da şu eklentileri yükle (Extensions sekmesi):
   - **Extension Pack for Java** (Microsoft)
   - **Spring Boot Extension Pack** (VMware/Microsoft)

2. Bu klasörü VSCode ile aç (`File > Open Folder`).

3. Birkaç saniye bekle, VSCode projeyi otomatik tanıyıp Maven bağımlılıklarını indirecek
   (sağ altta "Loading Java Projects..." göreceksin).

## Çalıştırma

**Yöntem 1 — VSCode üzerinden:**
`src/main/java/com/smartdesk/SmartDeskApplication.java` dosyasını aç,
`main` metodunun üstündeki **Run** yazısına tıkla.

**Yöntem 2 — Terminal üzerinden (Maven kuruluysa):**
```bash
mvn spring-boot:run
```

## Dashboard'u Görüntüleme

Uygulama başladıktan sonra tarayıcıda:

```
http://localhost:8080
```

İlk çalıştırmada veritabanı boş olduğu için **otomatik olarak örnek (sahte) veriler**
eklenir, böylece dashboard'u ESP32 bağlamadan da test edebilirsin.

## ESP32 Entegrasyonu

ESP32, ölçümleri şu adrese **POST** isteğiyle JSON olarak göndermeli:

```
POST http://<bilgisayar-ip>:8080/api/sensor
Content-Type: application/json

{
  "temperature": 23.4,
  "humidity": 45.0,
  "airQuality": 1450,
  "noise": 1200,
  "distance": 55.0
}
```

> `<bilgisayar-ip>` yerine bilgisayarının yerel ağ IP adresini yaz
> (Windows'ta `ipconfig` ile öğrenebilirsin, örn. `192.168.1.34`).
> ESP32 ile bilgisayar **aynı WiFi ağında** olmalı.

## Veritabanını İnceleme (opsiyonel)

MySQL Workbench veya benzer bir araçla `smartdesk` veritabanına bağlanıp
`sensor_readings` tablosunu görebilirsin. Bağlantı bilgileri
`application.properties` dosyasındakiyle aynı (host: `localhost`, port: `3306`).

## Proje Yapısı

```
src/main/java/com/smartdesk/
├── SmartDeskApplication.java   → Ana sınıf + örnek veri ekleme
├── controller/
│   ├── SensorApiController.java → /api/sensor endpoint'leri (ESP32 + dashboard)
│   └── DashboardController.java → Ana sayfa
├── model/
│   └── SensorReading.java       → Veritabanı tablosu (sensör kaydı)
├── repository/
│   └── SensorReadingRepository.java
└── service/
    └── ComfortService.java      → Konfor skoru hesaplama (kural tabanlı)

src/main/resources/
├── application.properties       → H2 veritabanı ayarları
└── templates/
    └── dashboard.html            → Dashboard arayüzü (Thymeleaf + Chart.js)
```

## Sonraki Adımlar

- [ ] ESP32 kodunu yaz, `/api/sensor` endpoint'ine POST gönder
- [ ] Kullanıcı kaydı + onboarding anketi
- [ ] Geri bildirim ekranı ("şu an verimli misin?")
- [ ] `ComfortService`'teki sabit eşikleri, kullanıcının geri bildirimlerinden
      öğrenilen kişisel "ideal profil" ile değiştir
