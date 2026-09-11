package com.example.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection

object AppLanguage {
    const val INDONESIAN = "Bahasa Indonesia"
    const val ENGLISH = "English"
    const val MALAY = "Bahasa Melayu"
    const val TURKISH = "Türkçe"
    const val FRENCH = "Français"
    const val DUTCH = "Nederlands"
    const val ARABIC = "العربية"

    fun isArabic(language: String): Boolean = language == ARABIC

    fun localeTag(language: String): String = when (language) {
        ENGLISH -> "en"
        MALAY -> "ms"
        TURKISH -> "tr"
        FRENCH -> "fr"
        DUTCH -> "nl"
        ARABIC -> "ar"
        else -> "id"
    }

    fun text(language: String, key: String): String = when (language) {
        ARABIC -> arabic[key] ?: key
        ENGLISH -> english[key] ?: key
        MALAY -> malay[key] ?: key
        TURKISH -> turkish[key] ?: key
        FRENCH -> french[key] ?: key
        DUTCH -> dutch[key] ?: key
        else -> key
    }

    private val english = mapOf(
        "Pengaturan" to "Settings", "Waktu Sholat & Lokasi" to "Prayer Times & Location", "Notifikasi & Pengingat" to "Notifications & Reminders", "Tampilan & Bahasa" to "Appearance & Language", "Lainnya" to "More", "Lokasi Saat Ini" to "Current Location", "Metode Perhitungan" to "Calculation Method", "Koreksi Waktu Sholat" to "Prayer Time Adjustment", "Notifikasi Waktu Sholat" to "Prayer Time Notifications", "Suara Adzan" to "Adhan Sound", "Tema Aplikasi" to "App Theme", "Bahasa Antarmuka" to "Interface Language", "Tentang Aplikasi" to "About App", "Kebijakan Privasi" to "Privacy Policy", "Tutup" to "Close", "Pilih Kota / Lokasi" to "Choose City / Location", "Pilih Tema Tampilan" to "Choose Appearance Theme", "Pilih Lokasi Manual" to "Choose Manual Location", "Atur Notifikasi" to "Notification Settings", "Perbarui Lokasi" to "Refresh Location", "Mencari..." to "Searching...", "Kembali" to "Back", "Home" to "Home", "Salat" to "Prayer", "Al-Qur'an" to "Qur'an", "Lainnya" to "More", "Arah Kiblat" to "Qibla Direction", "Kumpulan Doa" to "Dua Collection", "Dzikir & Tasbih" to "Dhikr & Tasbih", "Kalender Hijriah" to "Hijri Calendar", "Ubah lokasi" to "Change location"
    )
    private val malay = mapOf("Pengaturan" to "Tetapan", "Waktu Sholat & Lokasi" to "Waktu Solat & Lokasi", "Notifikasi & Pengingat" to "Pemberitahuan & Peringatan", "Tampilan & Bahasa" to "Paparan & Bahasa", "Lainnya" to "Lain-lain", "Lokasi Saat Ini" to "Lokasi Semasa", "Metode Perhitungan" to "Kaedah Pengiraan", "Koreksi Waktu Sholat" to "Pelarasan Waktu Solat", "Notifikasi Waktu Sholat" to "Pemberitahuan Waktu Solat", "Suara Adzan" to "Bunyi Azan", "Tema Aplikasi" to "Tema Aplikasi", "Bahasa Antarmuka" to "Bahasa Antara Muka", "Tentang Aplikasi" to "Tentang Aplikasi", "Kebijakan Privasi" to "Dasar Privasi", "Tutup" to "Tutup", "Pilih Kota / Lokasi" to "Pilih Bandar / Lokasi", "Pilih Tema Tampilan" to "Pilih Tema Paparan", "Pilih Lokasi Manual" to "Pilih Lokasi Manual", "Atur Notifikasi" to "Tetapan Pemberitahuan", "Perbarui Lokasi" to "Kemas Kini Lokasi", "Mencari..." to "Mencari...", "Kembali" to "Kembali", "Home" to "Utama", "Salat" to "Solat", "Al-Qur'an" to "Al-Quran", "Lainnya" to "Lain-lain", "Arah Kiblat" to "Arah Kiblat", "Kumpulan Doa" to "Koleksi Doa", "Dzikir & Tasbih" to "Zikir & Tasbih", "Kalender Hijriah" to "Kalendar Hijrah", "Ubah lokasi" to "Tukar lokasi")
    private val turkish = mapOf("Pengaturan" to "Ayarlar", "Waktu Sholat & Lokasi" to "Namaz Vakitleri ve Konum", "Notifikasi & Pengingat" to "Bildirimler ve Hatırlatıcılar", "Tampilan & Bahasa" to "Görünüm ve Dil", "Lainnya" to "Diğer", "Lokasi Saat Ini" to "Mevcut Konum", "Metode Perhitungan" to "Hesaplama Yöntemi", "Koreksi Waktu Sholat" to "Namaz Vakti Ayarı", "Notifikasi Waktu Sholat" to "Namaz Vakti Bildirimleri", "Suara Adzan" to "Ezan Sesi", "Tema Aplikasi" to "Uygulama Teması", "Bahasa Antarmuka" to "Arayüz Dili", "Tentang Aplikasi" to "Uygulama Hakkında", "Kebijakan Privasi" to "Gizlilik Politikası", "Tutup" to "Kapat", "Pilih Kota / Lokasi" to "Şehir / Konum Seç", "Pilih Tema Tampilan" to "Görünüm Teması Seç", "Pilih Lokasi Manual" to "Manuel Konum Seç", "Atur Notifikasi" to "Bildirim Ayarları", "Perbarui Lokasi" to "Konumu Yenile", "Mencari..." to "Aranıyor...", "Kembali" to "Geri", "Home" to "Ana Sayfa", "Salat" to "Namaz", "Al-Qur'an" to "Kur'an", "Lainnya" to "Diğer", "Arah Kiblat" to "Kıble Yönü", "Kumpulan Doa" to "Dua Koleksiyonu", "Dzikir & Tasbih" to "Zikir & Tesbih", "Kalender Hijriah" to "Hicri Takvim", "Ubah lokasi" to "Konumu değiştir")
    private val french = mapOf("Pengaturan" to "Paramètres", "Waktu Sholat & Lokasi" to "Horaires de prière et localisation", "Notifikasi & Pengingat" to "Notifications et rappels", "Tampilan & Bahasa" to "Apparence et langue", "Lainnya" to "Autres", "Lokasi Saat Ini" to "Position actuelle", "Metode Perhitungan" to "Méthode de calcul", "Koreksi Waktu Sholat" to "Ajustement des horaires de prière", "Notifikasi Waktu Sholat" to "Notifications des horaires de prière", "Suara Adzan" to "Son de l’adhan", "Tema Aplikasi" to "Thème de l’application", "Bahasa Antarmuka" to "Langue de l’interface", "Tentang Aplikasi" to "À propos", "Kebijakan Privasi" to "Politique de confidentialité", "Tutup" to "Fermer", "Pilih Kota / Lokasi" to "Choisir ville / position", "Pilih Tema Tampilan" to "Choisir le thème", "Pilih Lokasi Manual" to "Choisir une position manuelle", "Atur Notifikasi" to "Réglages des notifications", "Perbarui Lokasi" to "Actualiser la position", "Mencari..." to "Recherche...", "Kembali" to "Retour", "Home" to "Accueil", "Salat" to "Prière", "Al-Qur'an" to "Coran", "Lainnya" to "Autres", "Arah Kiblat" to "Direction de la Qibla", "Kumpulan Doa" to "Collection de douas", "Dzikir & Tasbih" to "Dhikr et tasbih", "Kalender Hijriah" to "Calendrier hégirien", "Ubah lokasi" to "Changer de position")
    private val dutch = mapOf("Pengaturan" to "Instellingen", "Waktu Sholat & Lokasi" to "Gebedstijden & locatie", "Notifikasi & Pengingat" to "Meldingen & herinneringen", "Tampilan & Bahasa" to "Weergave & taal", "Lainnya" to "Overig", "Lokasi Saat Ini" to "Huidige locatie", "Metode Perhitungan" to "Berekeningsmethode", "Koreksi Waktu Sholat" to "Aanpassing gebedstijden", "Notifikasi Waktu Sholat" to "Meldingen voor gebedstijden", "Suara Adzan" to "Adhan-geluid", "Tema Aplikasi" to "Appthema", "Bahasa Antarmuka" to "Interfacetaal", "Tentang Aplikasi" to "Over de app", "Kebijakan Privasi" to "Privacybeleid", "Tutup" to "Sluiten", "Pilih Kota / Lokasi" to "Kies stad / locatie", "Pilih Tema Tampilan" to "Kies weergavethema", "Pilih Lokasi Manual" to "Kies handmatige locatie", "Atur Notifikasi" to "Meldingsinstellingen", "Perbarui Lokasi" to "Locatie vernieuwen", "Mencari..." to "Zoeken...", "Kembali" to "Terug", "Home" to "Start", "Salat" to "Gebed", "Al-Qur'an" to "Koran", "Lainnya" to "Meer", "Arah Kiblat" to "Qibla-richting", "Kumpulan Doa" to "Dua-collectie", "Dzikir & Tasbih" to "Dhikr & Tasbih", "Kalender Hijriah" to "Hijri-kalender", "Ubah lokasi" to "Locatie wijzigen")
    private val arabic = mapOf("Pengaturan" to "الإعدادات", "Waktu Sholat & Lokasi" to "أوقات الصلاة والموقع", "Notifikasi & Pengingat" to "الإشعارات والتذكيرات", "Tampilan & Bahasa" to "المظهر واللغة", "Lainnya" to "المزيد", "Lokasi Saat Ini" to "الموقع الحالي", "Metode Perhitungan" to "طريقة الحساب", "Koreksi Waktu Sholat" to "تصحيح أوقات الصلاة", "Notifikasi Waktu Sholat" to "إشعارات أوقات الصلاة", "Suara Adzan" to "صوت الأذان", "Tema Aplikasi" to "مظهر التطبيق", "Bahasa Antarmuka" to "لغة الواجهة", "Tentang Aplikasi" to "حول التطبيق", "Kebijakan Privasi" to "سياسة الخصوصية", "Tutup" to "إغلاق", "Pilih Kota / Lokasi" to "اختر المدينة / الموقع", "Pilih Tema Tampilan" to "اختر مظهر التطبيق", "Pilih Lokasi Manual" to "اختر الموقع يدويًا", "Atur Notifikasi" to "إعدادات الإشعارات", "Perbarui Lokasi" to "تحديث الموقع", "Mencari..." to "جارٍ البحث...", "Kembali" to "رجوع", "Home" to "الرئيسية", "Salat" to "الصلاة", "Al-Qur'an" to "القرآن", "Lainnya" to "المزيد", "Arah Kiblat" to "اتجاه القبلة", "Kumpulan Doa" to "مجموعة الأدعية", "Dzikir & Tasbih" to "الذكر والتسبيح", "Kalender Hijriah" to "التقويم الهجري", "Ubah lokasi" to "تغيير الموقع")
}

@Composable
fun localizedLayoutDirection(language: String): LayoutDirection = if (AppLanguage.isArabic(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
