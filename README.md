# Twin

Aplikasi clone / dual app Android yang **minimalis, tanpa iklan, tanpa pelacak** — jalankan banyak akun dari satu aplikasi.

> Status: **Tahap 0 — Uji Kelayakan.** Belum ada aplikasi Twin. Saat ini repo hanya membangun APK uji coba dari 2 engine virtual kandidat untuk memastikan engine jalan di Android 16.

## Tahap 0: Cara Uji

1. Buka halaman **[Releases → Tahap 0 - Uji Kelayakan](../../releases/tag/uji-kelayakan)** dari HP.
2. Unduh & install kedua APK:
   - `Engine-A-NewBlackbox.apk` — [NewBlackbox](https://github.com/ALEX5402/NewBlackbox)
   - `Engine-B-BlacksBlackBox.apk` — [Blacks-BlackBox](https://github.com/Black00Z/Blacks-BlackBox) (fork dengan perbaikan Android 16)
   - Jika muncul peringatan Play Protect → **Tetap install**.
3. Di **masing-masing** aplikasi:
   1. Tambahkan aplikasi target (mis. **Muse**) dari daftar aplikasi terpasang.
   2. Buka → apakah terbuka? Bisa login?
   3. Buat clone **kedua** (pindah ke user/ruang lain di menu aplikasi, lalu tambahkan lagi) → login akun berbeda.
   4. Tutup aplikasi, tunggu beberapa menit → apakah notifikasi masuk?
4. Laporkan hasil (screenshot jika error).

| Cek | Engine A | Engine B |
|---|---|---|
| Install berhasil | | |
| Muse terbuka | | |
| Login berhasil | | |
| Clone ke-2 berhasil | | |
| Notifikasi masuk | | |

## Build

Semua build dilakukan otomatis oleh **GitHub Actions** — tidak perlu install apa pun di PC.

## Lisensi & Kredit

Engine kandidat berlisensi Apache 2.0: NewBlackbox (ALEX5402), Blacks-BlackBox (Black00Z), berbasis BlackBox / VirtualApp.
