package com.example.domain.model

enum class AppThemeSetting(val title: String) {
    SYSTEM("Ikuti Sistem"),
    LIGHT("Mode Terang"),
    DARK("Mode Gelap")
}

enum class CalculationMethod(val title: String, val description: String) {
    KEMENAG("Kemenag RI", "Kementerian Agama Republik Indonesia (Fajr 20°, Isha 18°)"),
    MWL("Muslim World League", "Liga Muslim Dunia (Fajr 18°, Isha 17°)"),
    EGYPT("Egyptian General Authority", "Otoritas Umum Mesir (Fajr 19.5°, Isha 17.5°)"),
    UMM_AL_QURA("Umm Al-Qura, Makkah", "Universitas Umm Al-Qura (Fajr 18.5°, Isha 90 min)")
}

data class AppSettings(
    val cityName: String = "Jakarta, Indonesia",
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val calculationMethod: CalculationMethod = CalculationMethod.KEMENAG,
    val imsakOffsetMinutes: Int = 0,
    val subuhOffsetMinutes: Int = 0,
    val dzuhurOffsetMinutes: Int = 0,
    val asharOffsetMinutes: Int = 0,
    val maghribOffsetMinutes: Int = 0,
    val isyaOffsetMinutes: Int = 0,
    val prayerNotificationEnabled: Boolean = true,
    val adzanSoundEnabled: Boolean = true,
    val selectedAdzanVoice: String = "Adzan Makkah",
    val themeSetting: AppThemeSetting = AppThemeSetting.DARK,
    val appLanguage: String = "Bahasa Indonesia"
)
