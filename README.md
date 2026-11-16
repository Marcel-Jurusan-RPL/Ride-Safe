# 🏍️ RideSafe - Smart Cycling & Driving Logger (Asisten Berkendara Aman)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange.svg)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Database-SQLite-lightgrey.svg)](https://developer.android.com/training/data-storage/sqlite)

> **RideSafe** adalah aplikasi asisten berkendara cerdas untuk pengendara sepeda motor, sepeda, dan mobil yang dibangun dengan **Kotlin Jetpack Compose** dan arsitektur **MVVM (Model-View-ViewModel)**. Aplikasi ini memanfaatkan 3 sensor utama Android (Motion, Position, Environment) secara real-time untuk memantau keselamatan perjalanan, mendeteksi lubang jalan/guncangan ekstrem, pengereman mendadak, menampilkan kompas arah mata angin, serta otomatis beradaptasi dengan pencahayaan sekitar (Auto Dark Mode saat masuk terowongan). Seluruh rekaman perjalanan dan log insiden disimpan secara persisten di database lokal **SQLite**.

---

## 📱 Daftar Fitur Utama

1. **Kompas Digital Real-Time (Position Sensor)**
   - Menampilkan visual kompas analog-digital interaktif dengan jarum rotasi halus (*smooth circular low-pass filter*).
   - Menghitung azimuth derajat (0° - 360°) dan arah mata angin 8 penjuru (Utara, Timur Laut, Timur, Tenggara, Selatan, Barat Daya, Barat, Barat Laut).

2. **Deteksi Guncangan & Jalan Berlubang (Motion Sensor)**
   - Mengukur vektor akselerasi total $g = \sqrt{x^2 + y^2 + z^2}$ dan deviasi terhadap gravitasi bumi ($9.81\text{ m/s}^2$).
   - Otomatis mencatat insiden jalan berlubang (*pothole bump*) dan memberikan umpan balik getar (*haptic feedback*).

3. **Deteksi Pengereman Kasar / Mendadak (Motion Sensor)**
   - Menganalisis deselerasi tajam pada sumbu laju kendaraan untuk membedakan pengereman normal dan pengereman darurat/kasar.

4. **Adaptasi Cahaya Lingkungan & Terowongan (Environment Sensor)**
   - Membaca intensitas cahaya sekitar dalam satuan **Lux**.
   - Otomatis mengubah antarmuka menjadi **Mode Gelap (Dark Mode)** saat pengendara memasuki terowongan, underpass, atau saat hari mulai malam (< 50 Lux) guna mencegah silau mata pengendara.

5. **Kalkulator Skor Keselamatan Berkendara (Driving Safety Score)**
   - Memberikan skor dinamis (0–100) berbasis algoritma evaluasi keselamatan:
     - 90 – 100: *Sangat Aman*
     - 75 – 89: *Aman*
     - 60 – 74: *Cukup Berhati-hati*
     - < 60: *Perlu Perbaikan*

6. **Penyimpanan Log Perjalanan Permanen (SQLite Database)**
   - Menyimpan seluruh sesi perjalanan: Waktu mulai, waktu selesai, durasi, total guncangan, total rem mendadak, skor keselamatan, evaluasi, dan catatan kustom.
   - Menyimpan daftar peristiwa/kejadian sensor berbobot magnitudo ($m/s^2$) dengan relasi *Foreign Key* dan *Cascade Delete*.

7. **Panel Simulasi Sensor (Emulator & Testing Mode)**
   - Tombol pengujian cepat untuk menguji respons aplikasi di Android Emulator tanpa perlu menggerakkan perangkat fisik secara nyata:
     - Simulasi Guncangan Jalan Berlubang ($+9.2\text{ m/s}^2$)
     - Simulasi Rem Mendadak
     - Simulasi Masuk Terowongan Gelap ($10\text{ Lux}$)
     - Simulasi Keluar ke Siang Terang ($3500\text{ Lux}$)

8. **Diagnostik & Kalibrasi Hardware Sensor**
   - Layar khusus untuk memantau ketersediaan perangkat keras sensor, nilai mentah real-time, dan pengaturan tema manual vs otomatis.

---

## 🛠️ Teknologi & Arsitektur

### 1. Pola Desain MVVM (Model-View-ViewModel)
Aplikasi memisahkan perhatian (*Separation of Concerns*) secara ketat:
- **Model**:
  - `TripSession.kt` & `RideEvent.kt`: Entitas data model.
  - `RideSafeDbHelper.kt`: Implementasi `SQLiteOpenHelper` untuk manajemen skema tabel, *foreign key constraints*, dan lifecycle database.
  - `TripDao.kt` & `RideEventDao.kt`: *Data Access Object* mengeksekusi kueri SQL murni (`INSERT`, `QUERY`, `DELETE`).
  - `RideRepository.kt`: Mengelola abstraksi data dan operasi I/O asinkron via *Kotlin Coroutines* (`Dispatchers.IO`).
- **ViewModel**:
  - `RideViewModel.kt`: Mengelola *state reactive* via `StateFlow`, timer perjalanan, kalkulasi skor, filter sensor, dan penghubung antarmuka.
  - `RideViewModelFactory.kt`: *Dependency injection provider* untuk ViewModel.
- **View (Jetpack Compose UI)**:
  - Antarmuka deklaratif modern berbasis Material 3 dengan *single source of truth*.

### 2. Stack Teknologi
- **Bahasa**: Kotlin 2.2+
- **UI Framework**: Android Jetpack Compose + Material 3 Design
- **Database**: SQLite Database Engine via `android.database.sqlite.SQLiteOpenHelper`
- **Asinkron & Reaktif**: Kotlin Coroutines & StateFlow
- **Sensor Framework**: `android.hardware.SensorManager` & `SensorEventListener`

---

## 📡 Sensor yang Digunakan & Fungsinya

| Kategori Sensor | Tipe Sensor Android | Fungsi dalam Aplikasi RideSafe |
| :--- | :--- | :--- |
| **Motion Sensor** | `Sensor.TYPE_ACCELEROMETER` | Mengukur percepatan 3 dimensi ($X, Y, Z$). Mendeteksi goncangan vertikal akibat jalan rusak/berlubang dan deselerasi horizontal akibat pengereman mendadak. |
| **Position Sensor** | `Sensor.TYPE_MAGNETIC_FIELD` + `Sensor.TYPE_ACCELEROMETER` | Menggabungkan medan magnet dan orientasi gravitasi via `SensorManager.getRotationMatrix()` dan `SensorManager.getOrientation()` untuk menggerakkan kompas digital real-time ke arah kutub utara bumi. |
| **Environment Sensor** | `Sensor.TYPE_LIGHT` | Mengukur iluminansi cahaya sekitar dalam satuan Lux. Mengaktifkan *Tunnel Mode* dan mengganti tema ke *Dark Mode* otomatis saat nilai lux turun drastis di bawah 50 Lux. |

---

## 📂 Struktur Direktori Proyek

```
RideSafe/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/ridesafe/
│   │   │   ├── MainActivity.kt               # Entry point & Navigasi Tab
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   │   ├── RideSafeDbHelper.kt   # SQLiteOpenHelper & Skema Tabel
│   │   │   │   │   ├── TripDao.kt            # Kueri SQLite untuk Trip
│   │   │   │   │   └── RideEventDao.kt       # Kueri SQLite untuk Insiden Sensor
│   │   │   │   ├── model/
│   │   │   │   │   ├── TripSession.kt        # Data class log perjalanan
│   │   │   │   │   └── RideEvent.kt          # Data class peristiwa sensor
│   │   │   │   └── repository/
│   │   │   │       └── RideRepository.kt     # Repository Coroutine IO
│   │   │   ├── sensor/
│   │   │   │   ├── MotionSensorTracker.kt    # Listener Akselerometer & Filter
│   │   │   │   ├── CompassSensorTracker.kt   # Listener Kompas Azimuth
│   │   │   │   └── EnvironmentLightTracker.kt# Listener Sensor Cahaya & Lux
│   │   │   ├── ui/
│   │   │   │   ├── components/               # Widget Kompas, G-Force, Lux Card
│   │   │   │   ├── screens/                  # Dashboard, History, Detail, Settings
│   │   │   │   └── theme/                    # Material 3 Theme & Palette
│   │   │   └── viewmodel/
│   │   │       ├── RideViewModel.kt          # State & Logika Bisnis
│   │   │       └── RideViewModelFactory.kt
│   │   └── res/                              # Ikon launcher & Nilai XML
├── gradle/
│   └── libs.versions.toml                    # Version Catalog
├── build.gradle.kts                          # Root build script
├── settings.gradle.kts
└── README.md
```

---

## 🚀 Cara Menjalankan Aplikasi

### Opsi A: Menggunakan Android Studio (Disarankan)
1. Buka software **Android Studio**.
2. Pilih menu **File > Open**, lalu arahkan ke direktori proyek:
   `C:\Users\Pongo\AndroidStudioProjects\RideSafe`
   *(atau path tempat Anda meng-clone repositori ini)*.
3. Tunggu proses **Gradle Sync** selesai.
4. Hubungkan perangkat Android fisik via USB Debugging, ATAU pilih salah satu perangkat **Android Virtual Device (AVD / Emulator)**.
5. Klik tombol hijau **Run 'app'** (`Shift + F10`).
6. Aplikasi akan terpasang dan siap digunakan!

### Opsi B: Build via Command Line (Terminal / PowerShell)
1. Buka Terminal / PowerShell di folder proyek.
2. Jalankan perintah kompilasi Gradle:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
3. File APK yang siap dipasang akan dibuat di:
   `app\build\outputs\apk\debug\app-debug.apk`
4. Pasang ke perangkat melalui ADB:
   ```powershell
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```

---

## 🧪 Panduan Demonstrasi & Pengujian di Emulator

Bagi penguji atau dosen penilai yang menguji pada **Android Emulator**:
1. Buka aplikasi **RideSafe**.
2. Pada tab **Dasbor**, tekan tombol **"Mulai Rekam Perjalanan"**.
3. Gulir ke bawah hingga bagian **"Panel Uji Coba Sensor (Demo / Emulator)"**:
   - Tekan **"Jalan Berlubang"**: Perhatikan *bump counter* bertambah, skor keselamatan berkurang dinamis, dan banner peringatan muncul.
   - Tekan **"Rem Mendadak"**: Perhatikan *harsh brake counter* bertambah dan skor berkurang.
   - Tekan **"Terowongan (10 lx)"**: Perhatikan tema aplikasi secara seketika beralih ke **Dark Mode** dan label *Tunnel Active* menyala!
   - Tekan **"Siang Terang (3500 lx)"**: Tema kembali terang secara otomatis.
4. Tekan **"Selesai & Simpan"**, beri nama perjalanan (misal: *Rute Uji Coba Kampus*), lalu tekan **Simpan**.
5. Buka tab **Riwayat**: Perjalanan Anda tersimpan rapi di SQLite!
6. Ketuk kartu perjalanan untuk membuka layar **Detail Perjalanan** dan melihat setiap log insiden sensor yang tercatat beserta magnitudo getarannya.

---

## 📤 Langkah Upload ke GitHub (Ketentuan Pengumpulan)

Untuk mengunggah kode ke GitHub publik Anda:

1. Buat repositori baru di [GitHub](https://github.com/new) dengan nama `RideSafe` dan setel visibilitas ke **Public**.
2. Buka terminal di folder proyek ini dan jalankan perintah berikut:
   ```bash
   git add .
   git commit -m "Initial commit: RideSafe - Smart Cycling & Driving Logger"
   git branch -M main
   git remote add origin https://github.com/Marcel-Jurusan-RPL/RideSafe.git
   git push -u origin main
   ```
3. Salin URL repositori publik tersebut (misal: `https://github.com/Marcel-Jurusan-RPL/RideSafe`).
4. Kumpulkan tautan repositori tersebut melalui form Google Classroom tugas Anda.

---

*Dikembangkan dengan penuh dedikasi untuk mata kuliah Pemrograman Perangkat Bergerak / Mobile Sensor Programming.*
