# ⛺ Scoutify - Project Guide (CLAUDE.md)

## 📌 Ringkasan Proyek
Scoutify adalah aplikasi Sistem Informasi dan Absensi Pramuka berbasis GPS dan *Selfie Verification*. Proyek ini ditargetkan memiliki kualitas setara aplikasi startup modern tahun 2026 untuk kebutuhan Sidang/Project Akhir.
- **Target Pengguna:** Anggota Pramuka, Pembina, Dewan Ambalan, Pengurus Gugus Depan.
- **Teknologi:** Android (Native) & Backend (Node.js REST API).

---

## 🏗️ Standar Arsitektur & Teknologi

### 🤖 Android Architecture (MVVM)
- **Pattern:** MVVM (Model-View-ViewModel) + Repository Pattern.
- **Async & State:** Kotlin Coroutines + StateFlow / LiveData.
- **Networking:** Retrofit + OkHttp.
- **Navigation:** Jetpack Navigation Component.
- **DI & Structure:** Dependency Injection (Hilt/Koin) + Modular Structure.

### 🟢 Backend Architecture (Node.js)
- **Framework & ORM:** Express.js + Prisma ORM.
- **Auth:** JWT Authentication + Role-Based Access Control (RBAC).
- **Security:** Bcrypt (Password Hashing), Rate Limiter, Helmet, CORS, Middleware Validation.

---

## 🎨 Sistem Desain & UI/UX (Material Design 3)
Aplikasi wajib menggunakan konsep *Modern Minimalist*, *Clean UI*, *Card-Based Layout*, *Soft Shadow*, dan *Smooth Animation*. **Dilarang keras menggunakan komponen bawaan/jadul Android Studio.**

### 🎨 Palet Warna (Identitas Pramuka Modern)
- `Primary`: `#3E5F44` (Hijau Pramuka Modern)
- `Secondary`: `#5E936C` | `Accent`: `#FFD54F` (Kuning Tunas)
- `Status`: `#4CAF50` (Success), `#FFB300` (Warning), `#E53935` (Danger)
- `Background`: `#F5F7F8` (Light), `#121212` (Dark)
- `Text`: `#1E1E1E` (Primary), `#757575` (Secondary)

### 📐 Aturan Visual
- **Tipografi:** Font *Poppins* atau *Inter*.
- **Border Radius:** `16dp` hingga `24dp` (Konsisten).
- **Elevasi:** Ringan, halus, dan elegan.
- **Elemen Grafis:** Integrasikan aset visual modern bertema kepanduan (Kompas, Tenda, Api Unggun, Peta Lokasi, Alam Terbuka).
- **Pengganti Komponen Lama:** Gunakan *Material Symbols Rounded* / *Phosphor Icons*, *Custom Bottom Sheet*, dan *Custom Snackbar*. Jangan gunakan emoji bawaan, alert dialog jadul, `ListView`, atau `ProgressBar` default.

---

## ⚙️ Fitur Utama & Peningkatan Wajib

### 🔑 Authentication & Profile
- Registrasi, Login, Auto Login, Remember Session, Logout.
- JWT & Refresh Token handling (termasuk penanganan *Session Expired*).
- Profile: Foto profil, ubah password, nomor induk anggota, gugus depan, jabatan.

### 📊 Dashboard Baru (Komponen Wajib)
- **Header:** Foto, nama, salam dinamis, info gugus depan.
- **Statistik:** Total hadir, izin, alpha, jumlah kegiatan, persentase bulanan.
- **Quick Action:** Tombol cepat Absen Masuk, Absen Pulang, Kegiatan, Riwayat.
- **Aktivitas & Status:** Jadwal hari ini, kegiatan mendatang, status absensi hari ini.

### 📍 Absensi Berbasis Geofencing & Biometrik
- Integrasi Google Maps + Marker + Radius Geofencing.
- Validasi GPS, status radius, *Selfie Verification* (Upload foto kehadiran).
- Manajemen kehadiran: Sakit, Izin, Riwayat, dan animasi sukses absen.

### 📅 Kegiatan & Notifikasi
- Daftar, detail, jadwal, status, lokasi, dan dokumentasi kegiatan.
- Push Notification untuk kehadiran, kegiatan, sistem, dan pengumuman.

### 🛠️ UX Enhancement (Wajib di Setiap Halaman)
- Pull to Refresh, Search, & Filter Data.
- State Handling: *Skeleton/Shimmer Loading*, *Empty State*, *Error State* dengan *Retry Button*.
- *Offline Handling* + *Internet Connection Checker*.

---

## 📝 Perintah & Fokus Pengembangan Claude
Saat berinteraksi dalam sesi ini, fokuslah pada 10 tujuan utama berikut secara bertahap:
1. **Audit & Debugging:** Audit seluruh *source code* (Android & Node.js) dan temukan *bug* serta celah keamanan (SQL Injection protection, input/upload file validation).
2. **Refactoring:** Ubah struktur kode lama ke *best practice* MVVM dan struktur backend yang aman.
3. **Redesign:** Renovasi total XML/Jetpack Compose layout sesuai panduan sistem desain di atas.
4. **Penyelesaian Fitur:** Lengkapi fitur yang *missing* (terutama Refresh Token, Offline Handling, Geofencing).

*Catatan: Selalu berikan output kode yang siap pakai, rapi, dan terdokumentasi dengan baik.*