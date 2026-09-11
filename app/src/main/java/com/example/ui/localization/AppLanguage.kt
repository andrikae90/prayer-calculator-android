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
        "Pengaturan" to "Settings", "Waktu Sholat & Lokasi" to "Prayer Times & Location",
        "Notifikasi & Pengingat" to "Notifications & Reminders", "Tampilan & Bahasa" to "Appearance & Language",
        "Lainnya" to "More", "Lokasi Saat Ini" to "Current Location", "Metode Perhitungan" to "Calculation Method",
        "Koreksi Waktu Sholat" to "Prayer Time Adjustment", "Notifikasi Waktu Sholat" to "Prayer Time Notifications",
        "Suara Adzan" to "Adhan Sound", "Tema Aplikasi" to "App Theme", "Bahasa Antarmuka" to "Interface Language",
        "Tentang Aplikasi" to "About App", "Kebijakan Privasi" to "Privacy Policy", "Tutup" to "Close",
        "Pilih Kota / Lokasi" to "Choose City / Location", "Pilih Tema Tampilan" to "Choose Appearance Theme",
        "Pilih Lokasi Manual" to "Choose Manual Location", "Atur Notifikasi" to "Notification Settings",
        "Perbarui Lokasi" to "Refresh Location", "Mencari..." to "Searching...", "Kembali" to "Back"
    )
    private val malay = mapOf("Pengaturan" to "Tetapan", "Waktu Sholat & Lokasi" to "Waktu Solat & Lokasi", "Notifikasi & Pengingat" to "Pemberitahuan & Peringatan", "Tampilan & Bahasa" to "Paparan & Bahasa", "Lainnya" to "Lain-lain", "Bahasa Antarmuka" to "Bahasa Antara Muka", "Tutup" to "Tutup")
    private val turkish = mapOf("Pengaturan" to "Ayarlar", "Waktu Sholat & Lokasi" to "Namaz Vakitleri ve Konum", "Notifikasi & Pengingat" to "Bildirimler ve Hatırlatıcılar", "Tampilan & Bahasa" to "Görünüm ve Dil", "Lainnya" to "Diğer", "Bahasa Antarmuka" to "Arayüz Dili", "Tutup" to "Kapat")
    private val french = mapOf("Pengaturan" to "Paramètres", "Waktu Sholat & Lokasi" to "Horaires de prière et localisation", "Notifikasi & Pengingat" to "Notifications et rappels", "Tampilan & Bahasa" to "Apparence et langue", "Lainnya" to "Autres", "Bahasa Antarmuka" to "Langue de l’interface", "Tutup" to "Fermer")
    private val dutch = mapOf("Pengaturan" to "Instellingen", "Waktu Sholat & Lokasi" to "Gebedstijden & locatie", "Notifikasi & Pengingat" to "Meldingen & herinneringen", "Tampilan & Bahasa" to "Weergave & taal", "Lainnya" to "Overig", "Bahasa Antarmuka" to "Interfacetaal", "Tutup" to "Sluiten")
    private val arabic = mapOf("Pengaturan" to "الإعدادات", "Waktu Sholat & Lokasi" to "أوقات الصلاة والموقع", "Notifikasi & Pengingat" to "الإشعارات والتذكيرات", "Tampilan & Bahasa" to "المظهر واللغة", "Lainnya" to "المزيد", "Lokasi Saat Ini" to "الموقع الحالي", "Metode Perhitungan" to "طريقة الحساب", "Koreksi Waktu Sholat" to "تصحيح أوقات الصلاة", "Notifikasi Waktu Sholat" to "إشعارات أوقات الصلاة", "Suara Adzan" to "صوت الأذان", "Tema Aplikasi" to "مظهر التطبيق", "Bahasa Antarmuka" to "لغة الواجهة", "Tentang Aplikasi" to "حول التطبيق", "Kebijakan Privasi" to "سياسة الخصوصية", "Tutup" to "إغلاق", "Pilih Kota / Lokasi" to "اختر المدينة / الموقع", "Pilih Tema Tampilan" to "اختر مظهر التطبيق", "Pilih Lokasi Manual" to "اختر الموقع يدويًا", "Atur Notifikasi" to "إعدادات الإشعارات", "Perbarui Lokasi" to "تحديث الموقع", "Mencari..." to "جارٍ البحث...", "Kembali" to "رجوع")
}

@Composable
fun localizedLayoutDirection(language: String): LayoutDirection = if (AppLanguage.isArabic(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
