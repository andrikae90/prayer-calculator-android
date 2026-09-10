package com.example.data.repository

import com.example.domain.model.DailyReminder
import java.util.Calendar

interface DailyReminderRepository {
    fun getTodayReminder(): DailyReminder
    fun getAllReminders(): List<DailyReminder>
}

class DefaultDailyReminderRepository : DailyReminderRepository {

    // Authentic Quranic verses from Al-Qur'anul Karim with official Kemenag RI Indonesian translation
    private val reminders = listOf(
        DailyReminder(
            id = 1,
            surahName = "Al-Baqarah",
            reference = "QS. Al-Baqarah: 152",
            arabicText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
            translation = "Maka ingatlah kepada-Ku, niscaya Aku ingat (pula) kepadamu, dan bersyukurlah kepada-Ku, dan janganlah kamu mengingkari (nikmat-Ku)."
        ),
        DailyReminder(
            id = 2,
            surahName = "Al-Insyirah",
            reference = "QS. Al-Insyirah: 5-6",
            arabicText = "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ، إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            translation = "Maka sesungguhnya bersama kesulitan ada kemudahan, sesungguhnya bersama kesulitan ada kemudahan."
        ),
        DailyReminder(
            id = 3,
            surahName = "Ar-Ra'd",
            reference = "QS. Ar-Ra'd: 28",
            arabicText = "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            translation = "Orang-orang yang beriman dan hati mereka menjadi tenteram dengan mengingat Allah. Ingatlah, hanya dengan mengingat Allah hati menjadi tenteram."
        ),
        DailyReminder(
            id = 4,
            surahName = "Ibrahim",
            reference = "QS. Ibrahim: 7",
            arabicText = "وَإِذْ تَأَذَّنَ رَبُّكُمْ لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ ۖ وَلَئِن كَفَرْتُمْ إِنَّ عَذَابِي لَشَدِيدٌ",
            translation = "Dan (ingatlah) ketika Tuhanmu memaklumkan: 'Sesungguhnya jika kamu bersyukur, niscaya Aku akan menambah (nikmat) kepadamu, tetapi jika kamu mengingkari (nikmat-Ku), sesungguhnya azab-Ku sangat pedih.'"
        ),
        DailyReminder(
            id = 5,
            surahName = "Ali 'Imran",
            reference = "QS. Ali 'Imran: 139",
            arabicText = "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنتُمُ الْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ",
            translation = "Dan janganlah kamu (merasa) lemah, dan janganlah (pula) bersedih hati, sebab kamu paling tinggi (derajatnya), jika kamu orang beriman."
        ),
        DailyReminder(
            id = 6,
            surahName = "Al-Baqarah",
            reference = "QS. Al-Baqarah: 45",
            arabicText = "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ وَإِنَّهَا لَكَبِيرَةٌ إِلَّا عَلَى الْخَاشِعِينَ",
            translation = "Dan mohonlah pertolongan (kepada Allah) dengan sabar dan salat. Dan (salat) itu sungguh berat, kecuali bagi orang-orang yang khusyuk."
        ),
        DailyReminder(
            id = 7,
            surahName = "At-Talaq",
            reference = "QS. At-Talaq: 3",
            arabicText = "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ",
            translation = "Dan barangsiapa bertawakal kepada Allah, niscaya Allah akan mencukupkan (keperluan)nya."
        )
    )

    override fun getTodayReminder(): DailyReminder {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return reminders[dayOfYear % reminders.size]
    }

    override fun getAllReminders(): List<DailyReminder> = reminders
}
