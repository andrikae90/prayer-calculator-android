package com.example.ui.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
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
        ENGLISH -> "en"; MALAY -> "ms"; TURKISH -> "tr"; FRENCH -> "fr"; DUTCH -> "nl"; ARABIC -> "ar"; else -> "id"
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

    private val common = mapOf(
        "Fitur Utama" to mapOf(ENGLISH to "Main Features", MALAY to "Ciri Utama", TURKISH to "Ana Özellikler", FRENCH to "Fonctionnalités principales", DUTCH to "Belangrijkste functies", ARABIC to "الميزات الرئيسية"),
        "Jadwal Salat Hari Ini" to mapOf(ENGLISH to "Today's Prayer Times", MALAY to "Waktu Solat Hari Ini", TURKISH to "Bugünün Namaz Vakitleri", FRENCH to "Horaires de prière aujourd'hui", DUTCH to "Gebedstijden vandaag", ARABIC to "أوقات الصلاة اليوم"),
        "Lihat Semua" to mapOf(ENGLISH to "View All", MALAY to "Lihat Semua", TURKISH to "Tümünü Gör", FRENCH to "Voir tout", DUTCH to "Alles bekijken", ARABIC to "عرض الكل"),
        "Assalamu'alaikum," to mapOf(ENGLISH to "Assalamu'alaikum,", MALAY to "Assalamu'alaikum,", TURKISH to "Esselamu aleyküm,", FRENCH to "Assalamu'alaikum,", DUTCH to "Assalamu'alaikum,", ARABIC to "السلام عليكم،"),
        "Selamat Beraktivitas" to mapOf(ENGLISH to "Have a blessed day", MALAY to "Selamat beraktiviti", TURKISH to "Hayırlı günler", FRENCH to "Bonne journée", DUTCH to "Een gezegende dag", ARABIC to "يومًا مباركًا"),
        "Lokasi" to mapOf(ENGLISH to "Location", MALAY to "Lokasi", TURKISH to "Konum", FRENCH to "Position", DUTCH to "Locatie", ARABIC to "الموقع"),
        "Salat Berikutnya" to mapOf(ENGLISH to "Next Prayer", MALAY to "Solat Seterusnya", TURKISH to "Sonraki Namaz", FRENCH to "Prochaine prière", DUTCH to "Volgend gebed", ARABIC to "الصلاة التالية"),
        "Menuju waktu salat" to mapOf(ENGLISH to "Until prayer time", MALAY to "Menuju waktu solat", TURKISH to "Namaz vaktine kalan", FRENCH to "Jusqu'à la prière", DUTCH to "Tot gebedstijd", ARABIC to "حتى وقت الصلاة"),
        "Jadwal Salat" to mapOf(ENGLISH to "Prayer Times", MALAY to "Waktu Solat", TURKISH to "Namaz Vakitleri", FRENCH to "Horaires de prière", DUTCH to "Gebedstijden", ARABIC to "أوقات الصلاة"),
        "Kiblat" to mapOf(ENGLISH to "Qibla", MALAY to "Kiblat", TURKISH to "Kıble", FRENCH to "Qibla", DUTCH to "Qibla", ARABIC to "القبلة"),
        "Kumpulan Doa" to mapOf(ENGLISH to "Dua Collection", MALAY to "Koleksi Doa", TURKISH to "Dua Koleksiyonu", FRENCH to "Collection de douas", DUTCH to "Dua-collectie", ARABIC to "مجموعة الأدعية"),
        "Dzikir & Tasbih" to mapOf(ENGLISH to "Dhikr & Tasbih", MALAY to "Zikir & Tasbih", TURKISH to "Zikir ve Tesbih", FRENCH to "Dhikr et tasbih", DUTCH to "Dhikr & Tasbih", ARABIC to "الذكر والتسبيح"),
        "Kalender" to mapOf(ENGLISH to "Calendar", MALAY to "Kalendar", TURKISH to "Takvim", FRENCH to "Calendrier", DUTCH to "Kalender", ARABIC to "التقويم"),
        "Berikutnya" to mapOf(ENGLISH to "Next", MALAY to "Seterusnya", TURKISH to "Sıradaki", FRENCH to "Suivante", DUTCH to "Volgende", ARABIC to "التالية"),
        "Sudah Lewat" to mapOf(ENGLISH to "Passed", MALAY to "Telah Berlalu", TURKISH to "Geçti", FRENCH to "Passée", DUTCH to "Voorbij", ARABIC to "انقضت"),
        "Pengingat Harian" to mapOf(ENGLISH to "Daily Reminder", MALAY to "Peringatan Harian", TURKISH to "Günlük Hatırlatma", FRENCH to "Rappel quotidien", DUTCH to "Dagelijkse herinnering", ARABIC to "تذكير يومي"),
        "Ikon Masjid" to mapOf(ENGLISH to "Mosque icon", MALAY to "Ikon masjid", TURKISH to "Cami simgesi", FRENCH to "Icône de mosquée", DUTCH to "Moskee-icoon", ARABIC to "أيقونة المسجد"),
        "Subuh" to mapOf(ENGLISH to "Fajr", MALAY to "Subuh", TURKISH to "Sabah", FRENCH to "Fajr", DUTCH to "Fajr", ARABIC to "الفجر"),
        "Dzuhur" to mapOf(ENGLISH to "Dhuhr", MALAY to "Zohor", TURKISH to "Öğle", FRENCH to "Dhuhr", DUTCH to "Dhuhr", ARABIC to "الظهر"),
        "Ashar" to mapOf(ENGLISH to "Asr", MALAY to "Asar", TURKISH to "İkindi", FRENCH to "Asr", DUTCH to "Asr", ARABIC to "العصر"),
        "Maghrib" to mapOf(ENGLISH to "Maghrib", MALAY to "Maghrib", TURKISH to "Akşam", FRENCH to "Maghrib", DUTCH to "Maghrib", ARABIC to "المغرب"),
        "Isya" to mapOf(ENGLISH to "Isha", MALAY to "Isyak", TURKISH to "Yatsı", FRENCH to "Isha", DUTCH to "Isha", ARABIC to "العشاء")
    )

    private val extra = mapOf(
        "Pengaturan" to mapOf(ENGLISH to "Settings", MALAY to "Tetapan", TURKISH to "Ayarlar", FRENCH to "Paramètres", DUTCH to "Instellingen", ARABIC to "الإعدادات"),
        "Waktu Sholat & Lokasi" to mapOf(ENGLISH to "Prayer Times & Location", MALAY to "Waktu Solat & Lokasi", TURKISH to "Namaz Vakitleri ve Konum", FRENCH to "Horaires de prière et localisation", DUTCH to "Gebedstijden & locatie", ARABIC to "أوقات الصلاة والموقع"),
        "Notifikasi & Pengingat" to mapOf(ENGLISH to "Notifications & Reminders", MALAY to "Pemberitahuan & Peringatan", TURKISH to "Bildirimler ve Hatırlatıcılar", FRENCH to "Notifications et rappels", DUTCH to "Meldingen & herinneringen", ARABIC to "الإشعارات والتذكيرات"),
        "Tampilan & Bahasa" to mapOf(ENGLISH to "Appearance & Language", MALAY to "Paparan & Bahasa", TURKISH to "Görünüm ve Dil", FRENCH to "Apparence et langue", DUTCH to "Weergave & taal", ARABIC to "المظهر واللغة"),
        "Lainnya" to mapOf(ENGLISH to "More", MALAY to "Lain-lain", TURKISH to "Diğer", FRENCH to "Autres", DUTCH to "Meer", ARABIC to "المزيد"),
        "Lokasi Saat Ini" to mapOf(ENGLISH to "Current Location", MALAY to "Lokasi Semasa", TURKISH to "Mevcut Konum", FRENCH to "Position actuelle", DUTCH to "Huidige locatie", ARABIC to "الموقع الحالي"),
        "Metode Perhitungan" to mapOf(ENGLISH to "Calculation Method", MALAY to "Kaedah Pengiraan", TURKISH to "Hesaplama Yöntemi", FRENCH to "Méthode de calcul", DUTCH to "Berekeningsmethode", ARABIC to "طريقة الحساب"),
        "Koreksi Waktu Sholat" to mapOf(ENGLISH to "Prayer Time Adjustment", MALAY to "Pelarasan Waktu Solat", TURKISH to "Namaz Vakti Ayarı", FRENCH to "Ajustement des horaires de prière", DUTCH to "Aanpassing gebedstijden", ARABIC to "تصحيح أوقات الصلاة"),
        "Notifikasi Waktu Sholat" to mapOf(ENGLISH to "Prayer Time Notifications", MALAY to "Pemberitahuan Waktu Solat", TURKISH to "Namaz Vakti Bildirimleri", FRENCH to "Notifications des horaires de prière", DUTCH to "Meldingen voor gebedstijden", ARABIC to "إشعارات أوقات الصلاة"),
        "Suara Adzan" to mapOf(ENGLISH to "Adhan Sound", MALAY to "Bunyi Azan", TURKISH to "Ezan Sesi", FRENCH to "Son de l’adhan", DUTCH to "Adhan-geluid", ARABIC to "صوت الأذان"),
        "Tema Aplikasi" to mapOf(ENGLISH to "App Theme", MALAY to "Tema Aplikasi", TURKISH to "Uygulama Teması", FRENCH to "Thème de l’application", DUTCH to "Appthema", ARABIC to "مظهر التطبيق"),
        "Bahasa Antarmuka" to mapOf(ENGLISH to "Interface Language", MALAY to "Bahasa Antara Muka", TURKISH to "Arayüz Dili", FRENCH to "Langue de l’interface", DUTCH to "Interfacetaal", ARABIC to "لغة الواجهة"),
        "Tentang Aplikasi" to mapOf(ENGLISH to "About App", MALAY to "Tentang Aplikasi", TURKISH to "Uygulama Hakkında", FRENCH to "À propos", DUTCH to "Over de app", ARABIC to "حول التطبيق"),
        "Kebijakan Privasi" to mapOf(ENGLISH to "Privacy Policy", MALAY to "Dasar Privasi", TURKISH to "Gizlilik Politikası", FRENCH to "Politique de confidentialité", DUTCH to "Privacybeleid", ARABIC to "سياسة الخصوصية"),
        "Tutup" to mapOf(ENGLISH to "Close", MALAY to "Tutup", TURKISH to "Kapat", FRENCH to "Fermer", DUTCH to "Sluiten", ARABIC to "إغلاق"),
        "Pilih Kota / Lokasi" to mapOf(ENGLISH to "Choose City / Location", MALAY to "Pilih Bandar / Lokasi", TURKISH to "Şehir / Konum Seç", FRENCH to "Choisir ville / position", DUTCH to "Kies stad / locatie", ARABIC to "اختر المدينة / الموقع"),
        "Pilih Tema Tampilan" to mapOf(ENGLISH to "Choose Appearance Theme", MALAY to "Pilih Tema Paparan", TURKISH to "Görünüm Teması Seç", FRENCH to "Choisir le thème", DUTCH to "Kies weergavethema", ARABIC to "اختر مظهر التطبيق"),
        "Pilih Lokasi Manual" to mapOf(ENGLISH to "Choose Manual Location", MALAY to "Pilih Lokasi Manual", TURKISH to "Manuel Konum Seç", FRENCH to "Choisir une position manuelle", DUTCH to "Kies handmatige locatie", ARABIC to "اختر الموقع يدويًا"),
        "Atur Notifikasi" to mapOf(ENGLISH to "Notification Settings", MALAY to "Tetapan Pemberitahuan", TURKISH to "Bildirim Ayarları", FRENCH to "Réglages des notifications", DUTCH to "Meldingsinstellingen", ARABIC to "إعدادات الإشعارات"),
        "Perbarui Lokasi" to mapOf(ENGLISH to "Refresh Location", MALAY to "Kemas Kini Lokasi", TURKISH to "Konumu Yenile", FRENCH to "Actualiser la position", DUTCH to "Locatie vernieuwen", ARABIC to "تحديث الموقع"),
        "Mencari..." to mapOf(ENGLISH to "Searching...", MALAY to "Mencari...", TURKISH to "Aranıyor...", FRENCH to "Recherche...", DUTCH to "Zoeken...", ARABIC to "جارٍ البحث..."),
        "Kembali" to mapOf(ENGLISH to "Back", MALAY to "Kembali", TURKISH to "Geri", FRENCH to "Retour", DUTCH to "Terug", ARABIC to "رجوع"),
        "Home" to mapOf(ENGLISH to "Home", MALAY to "Utama", TURKISH to "Ana Sayfa", FRENCH to "Accueil", DUTCH to "Start", ARABIC to "الرئيسية"),
        "Salat" to mapOf(ENGLISH to "Prayer", MALAY to "Solat", TURKISH to "Namaz", FRENCH to "Prière", DUTCH to "Gebed", ARABIC to "الصلاة"),
        "Al-Qur'an" to mapOf(ENGLISH to "Qur'an", MALAY to "Al-Quran", TURKISH to "Kur'an", FRENCH to "Coran", DUTCH to "Koran", ARABIC to "القرآن"),
        "Arah Kiblat" to mapOf(ENGLISH to "Qibla Direction", MALAY to "Arah Kiblat", TURKISH to "Kıble Yönü", FRENCH to "Direction de la Qibla", DUTCH to "Qibla-richting", ARABIC to "اتجاه القبلة"),
        "Kalender Hijriah" to mapOf(ENGLISH to "Hijri Calendar", MALAY to "Kalendar Hijrah", TURKISH to "Hicri Takvim", FRENCH to "Calendrier hégirien", DUTCH to "Hijri-kalender", ARABIC to "التقويم الهجري"),
        "Ubah lokasi" to mapOf(ENGLISH to "Change location", MALAY to "Tukar lokasi", TURKISH to "Konumu değiştir", FRENCH to "Changer de position", DUTCH to "Locatie wijzigen", ARABIC to "تغيير الموقع")
    )

    private fun baseMap(language: String): Map<String, String> = (common + extra).mapValues { (_, values) -> values[language] ?: "" }
    private val english = baseMap(ENGLISH)
    private val malay = baseMap(MALAY)
    private val turkish = baseMap(TURKISH)
    private val french = baseMap(FRENCH)
    private val dutch = baseMap(DUTCH)
    private val arabic = baseMap(ARABIC)
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.INDONESIAN }

@Composable
fun localizedLayoutDirection(language: String): LayoutDirection = if (AppLanguage.isArabic(language)) LayoutDirection.Rtl else LayoutDirection.Ltr
