package com.example.domain.model

enum class AppThemeSetting(val title: String) {
    SYSTEM("Ikuti Sistem"),
    LIGHT("Mode Terang"),
    DARK("Mode Gelap")
}

enum class CalculationMethod(val title: String, val description: String) {
    KEMENAG("Kemenag RI", "Kementerian Agama Republik Indonesia (Subuh 20°, Isya 18°)"),
    MWL("Muslim World League", "Liga Muslim Dunia (Subuh 18°, Isya 17°)"),
    EGYPT("Egyptian General Authority", "Otoritas Umum Mesir (Subuh 19.5°, Isya 17.5°)"),
    UMM_AL_QURA("Umm Al-Qura, Makkah", "Universitas Umm Al-Qura (Subuh 18.5°, Isya 90 min)")
}

enum class PrayerNotificationSound(val title: String) {
    ADZAN_LENGKAP("Adzan lengkap"),
    TAKBIR_SAJA("Takbir saja"),
    BIP_PANJANG("Bip panjang"),
    GETAR_SAJA("Getar saja"),
    TANPA_NOTIFIKASI("Tanpa notifikasi")
}

data class AppSettings(
    val cityName: String = "Jakarta, Indonesia",
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val elevationMeters: Double = 0.0,
    val calculationMethod: CalculationMethod = CalculationMethod.KEMENAG,
    val imsakOffsetMinutes: Int = 0,
    val subuhOffsetMinutes: Int = 0,
    val dzuhurOffsetMinutes: Int = 0,
    val asharOffsetMinutes: Int = 0,
    val maghribOffsetMinutes: Int = 0,
    val isyaOffsetMinutes: Int = 0,
    val prayerNotificationEnabled: Boolean = true,
    val subuhNotificationEnabled: Boolean = true,
    val dzuhurNotificationEnabled: Boolean = true,
    val asharNotificationEnabled: Boolean = true,
    val maghribNotificationEnabled: Boolean = true,
    val isyaNotificationEnabled: Boolean = true,
    val notificationSound: PrayerNotificationSound = PrayerNotificationSound.BIP_PANJANG,
    val adzanSoundEnabled: Boolean = true,
    val selectedAdzanVoice: String = "Adzan Makkah",
    val themeSetting: AppThemeSetting = AppThemeSetting.DARK,
    val appLanguage: String = "Bahasa Indonesia"
)
