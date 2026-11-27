# Twin

Aplikasi **clone / dual app** Android yang **minimalis, tanpa iklan, tanpa pelacak** — jalankan banyak akun dari aplikasi yang sama dalam ruang terpisah.

## Fitur Utama (v1.0)
- **Clone tanpa batas** — buat akun ke-1, ke-2, ke-3, dst. hanya dengan satu ketukan (otomatis diberi lencana angka `1`, `2`, `3`…).
- **Desain minimalis** — Material 3 bersih, mendukung Mode Gelap & Terang otomatis, tanpa iklan dan tanpa telemetri.
- **Kompatibilitas Google Play & Meta otomatis** — jembatan *Host GMS & Signing* aktif secara bawaan untuk aplikasi seperti **Muse from Meta** (`com.facebook.aura`), Instagram, dll. di Android 8 – 16.
- **Kelola mudah** — tahan ikon clone untuk **ganti nama**, **tambah pintasan ke layar utama**, **hentikan paksa**, **reset data**, atau **hapus clone**.
- **Pasang dari APK** — mendukung kloning dari aplikasi terpasang maupun langsung dari file `.apk` / `.apks`.

## Cara Install
1. Buka halaman **[Releases](../../releases/latest)** dari HP Android kamu.
2. Unduh file **`Twin-v1.0.0.apk`** lalu pasang (jika muncul peringatan Play Protect, pilih **Tetap install**).
3. Buka **Twin** → ketuk **Mulai** → ketuk **Tambah aplikasi (+)** → pilih aplikasi yang ingin digandakan (mis. **Muse**).

> **Tips:** Jika sebelumnya masih terpasang APK uji kelayakan (`Engine-A` / `Engine-B`), kamu boleh menghapusnya agar tidak membingungkan.

## Build Otomatis (GitHub Actions)
Seluruh proses kompilasi SDK + NDK dan penandatanganan rilis (*APK signing*) berjalan otomatis di server **GitHub Actions** melalui `.github/workflows/build.yml` — tidak perlu meng-install Android Studio di PC.

## Lisensi & Atribusi
Kode aplikasi Twin dan modul engine virtual berlisensi **Apache License 2.0**. Engine virtual diadaptasi dari proyek open-source [Blacks-BlackBox](https://github.com/Black00Z/Blacks-BlackBox) & [NewBlackbox](https://github.com/ALEX5402/NewBlackbox) (berbasis BlackBox / VirtualApp) dengan penyesuaian kompatibilitas GMS/Meta untuk Android 16 serta pembersihan telemetri.
