package com.example.data.repository

import com.example.domain.model.RevelationType
import com.example.domain.model.Surah
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface QuranRepository {
    fun getAllSurahs(): Flow<List<Surah>>
    fun searchSurahs(query: String): Flow<List<Surah>>
    fun getSurahByNumber(number: Int): Surah?
}

class LocalQuranRepository : QuranRepository {

    private val surahs: List<Surah> = listOf(
        Surah(1, "الفاتحة", "Al-Fatihah", "Pembukaan", 7, RevelationType.MAKKIYAH),
        Surah(2, "البقرة", "Al-Baqarah", "Sapi Betina", 286, RevelationType.MADANIYAH),
        Surah(3, "آل عمران", "Ali 'Imran", "Keluarga Imran", 200, RevelationType.MADANIYAH),
        Surah(4, "النساء", "An-Nisa'", "Wanita", 176, RevelationType.MADANIYAH),
        Surah(5, "المائدة", "Al-Ma'idah", "Jamuan", 120, RevelationType.MADANIYAH),
        Surah(6, "الأنعام", "Al-An'am", "Binatang Ternak", 165, RevelationType.MAKKIYAH),
        Surah(7, "الأعراف", "Al-A'raf", "Tempat yang Tertinggi", 206, RevelationType.MAKKIYAH),
        Surah(8, "الأنفال", "Al-Anfal", "Rampasan Perang", 75, RevelationType.MADANIYAH),
        Surah(9, "التوبة", "At-Taubah", "Pengampunan", 129, RevelationType.MADANIYAH),
        Surah(10, "يونس", "Yunus", "Nabi Yunus", 109, RevelationType.MAKKIYAH),
        Surah(11, "هود", "Hud", "Nabi Hud", 123, RevelationType.MAKKIYAH),
        Surah(12, "يوسف", "Yusuf", "Nabi Yusuf", 111, RevelationType.MAKKIYAH),
        Surah(13, "الرعد", "Ar-Ra'd", "Guruh", 43, RevelationType.MADANIYAH),
        Surah(14, "إبراهيم", "Ibrahim", "Nabi Ibrahim", 52, RevelationType.MAKKIYAH),
        Surah(15, "الحجر", "Al-Hijr", "Gunung Al-Hijr", 99, RevelationType.MAKKIYAH),
        Surah(16, "النحل", "An-Nahl", "Lebah", 128, RevelationType.MAKKIYAH),
        Surah(17, "الإسراء", "Al-Isra'", "Perjalanan Malam", 111, RevelationType.MAKKIYAH),
        Surah(18, "الكهف", "Al-Kahf", "Gua", 110, RevelationType.MAKKIYAH),
        Surah(19, "مريم", "Maryam", "Maryam", 98, RevelationType.MAKKIYAH),
        Surah(20, "طه", "Taha", "Taha", 135, RevelationType.MAKKIYAH),
        Surah(21, "الأنبياء", "Al-Anbiya'", "Para Nabi", 112, RevelationType.MAKKIYAH),
        Surah(22, "الحج", "Al-Hajj", "Haji", 78, RevelationType.MADANIYAH),
        Surah(23, "المؤمنون", "Al-Mu'minun", "Orang-Orang Mukmin", 118, RevelationType.MAKKIYAH),
        Surah(24, "النور", "An-Nur", "Cahaya", 64, RevelationType.MADANIYAH),
        Surah(25, "الفرقان", "Al-Furqan", "Pembeda", 77, RevelationType.MAKKIYAH),
        Surah(26, "الشعراء", "Asy-Syu'ara'", "Para Penyair", 227, RevelationType.MAKKIYAH),
        Surah(27, "النمل", "An-Naml", "Semut", 93, RevelationType.MAKKIYAH),
        Surah(28, "القصص", "Al-Qasas", "Kisah-Kisah", 88, RevelationType.MAKKIYAH),
        Surah(29, "العنكبوت", "Al-'Ankabut", "Laba-Laba", 69, RevelationType.MAKKIYAH),
        Surah(30, "الروم", "Ar-Rum", "Bangsa Romawi", 60, RevelationType.MAKKIYAH),
        Surah(31, "لقمان", "Luqman", "Keluarga Luqman", 34, RevelationType.MAKKIYAH),
        Surah(32, "السجدة", "As-Sajdah", "Sujud", 30, RevelationType.MAKKIYAH),
        Surah(33, "الأحزاب", "Al-Ahzab", "Golongan yang Bersekutu", 73, RevelationType.MADANIYAH),
        Surah(34, "سبأ", "Saba'", "Kaum Saba'", 54, RevelationType.MAKKIYAH),
        Surah(35, "فاطر", "Fatir", "Pencipta", 45, RevelationType.MAKKIYAH),
        Surah(36, "يس", "Yasin", "Yasin", 83, RevelationType.MAKKIYAH),
        Surah(37, "الصافات", "As-Saffat", "Barisan-Barisan", 182, RevelationType.MAKKIYAH),
        Surah(38, "ص", "Sad", "Sad", 88, RevelationType.MAKKIYAH),
        Surah(39, "الزمر", "Az-Zumar", "Rombongan", 75, RevelationType.MAKKIYAH),
        Surah(40, "غافر", "Ghafir", "Yang Mengampuni", 85, RevelationType.MAKKIYAH),
        Surah(41, "فصلت", "Fussilat", "Yang Dijelaskan", 54, RevelationType.MAKKIYAH),
        Surah(42, "الشورى", "Asy-Syura", "Musyawarah", 53, RevelationType.MAKKIYAH),
        Surah(43, "الزخرف", "Az-Zukhruf", "Perhiasan", 89, RevelationType.MAKKIYAH),
        Surah(44, "الدخان", "Ad-Dukhan", "Kabut", 59, RevelationType.MAKKIYAH),
        Surah(45, "الجاثية", "Al-Jasiyah", "Yang Berlutut", 37, RevelationType.MAKKIYAH),
        Surah(46, "الأحقاف", "Al-Ahqaf", "Bukit Pasir", 35, RevelationType.MAKKIYAH),
        Surah(47, "محمد", "Muhammad", "Nabi Muhammad", 38, RevelationType.MADANIYAH),
        Surah(48, "الفتح", "Al-Fath", "Kemenangan", 29, RevelationType.MADANIYAH),
        Surah(49, "الحجرات", "Al-Hujurat", "Kamar-Kamar", 18, RevelationType.MADANIYAH),
        Surah(50, "ق", "Qaf", "Qaf", 45, RevelationType.MAKKIYAH),
        Surah(51, "الذاريات", "Az-Zariyat", "Angin yang Menerbangkan", 60, RevelationType.MAKKIYAH),
        Surah(52, "الطور", "At-Tur", "Bukit Tursina", 49, RevelationType.MAKKIYAH),
        Surah(53, "النجم", "An-Najm", "Bintang", 62, RevelationType.MAKKIYAH),
        Surah(54, "القمر", "Al-Qamar", "Bulan", 55, RevelationType.MAKKIYAH),
        Surah(55, "الرحمن", "Ar-Rahman", "Maha Pengasih", 78, RevelationType.MADANIYAH),
        Surah(56, "الواقعة", "Al-Waqi'ah", "Hari Kiamat", 96, RevelationType.MAKKIYAH),
        Surah(57, "الحديد", "Al-Hadid", "Besi", 29, RevelationType.MADANIYAH),
        Surah(58, "المجادلة", "Al-Mujadilah", "Gugatan", 22, RevelationType.MADANIYAH),
        Surah(59, "الحشر", "Al-Hasyr", "Pengusiran", 24, RevelationType.MADANIYAH),
        Surah(60, "الممتحنة", "Al-Mumtahanah", "Wanita yang Diuji", 13, RevelationType.MADANIYAH),
        Surah(61, "الصف", "As-Saff", "Barisan", 14, RevelationType.MADANIYAH),
        Surah(62, "الجمعة", "Al-Jumu'ah", "Hari Jumat", 11, RevelationType.MADANIYAH),
        Surah(63, "المنافقون", "Al-Munafiqun", "Orang-Orang Munafik", 11, RevelationType.MADANIYAH),
        Surah(64, "التغابن", "At-Taghabun", "Hari Dinampakkan Kesalahan", 18, RevelationType.MADANIYAH),
        Surah(65, "الطلاق", "At-Talaq", "Perceraian", 12, RevelationType.MADANIYAH),
        Surah(66, "التحريم", "At-Tahrim", "Pengharaman", 12, RevelationType.MADANIYAH),
        Surah(67, "الملك", "Al-Mulk", "Kerajaan", 30, RevelationType.MAKKIYAH),
        Surah(68, "القلم", "Al-Qalam", "Pena", 52, RevelationType.MAKKIYAH),
        Surah(69, "الحاقة", "Al-Haqqah", "Hari Kiamat yang Pasti", 52, RevelationType.MAKKIYAH),
        Surah(70, "المعارج", "Al-Ma'arij", "Tempat Naik", 44, RevelationType.MAKKIYAH),
        Surah(71, "نوح", "Nuh", "Nabi Nuh", 28, RevelationType.MAKKIYAH),
        Surah(72, "الجن", "Al-Jinn", "Jin", 28, RevelationType.MAKKIYAH),
        Surah(73, "المزمل", "Al-Muzzammil", "Orang yang Berselimut", 20, RevelationType.MAKKIYAH),
        Surah(74, "المدثر", "Al-Muddassir", "Orang yang Berkemul", 56, RevelationType.MAKKIYAH),
        Surah(75, "القيامة", "Al-Qiyamah", "Hari Kiamat", 40, RevelationType.MAKKIYAH),
        Surah(76, "الإنسان", "Al-Insan", "Manusia", 31, RevelationType.MADANIYAH),
        Surah(77, "المرسلات", "Al-Mursalat", "Malaikat yang Diutus", 50, RevelationType.MAKKIYAH),
        Surah(78, "النبأ", "An-Naba'", "Berita Besar", 40, RevelationType.MAKKIYAH),
        Surah(79, "النازعات", "An-Nazi'at", "Malaikat Pencabut", 46, RevelationType.MAKKIYAH),
        Surah(80, "عبس", "'Abasa", "Ia Bermuka Masam", 42, RevelationType.MAKKIYAH),
        Surah(81, "التكوير", "At-Takwir", "Menggulung", 29, RevelationType.MAKKIYAH),
        Surah(82, "الانفطار", "Al-Infitar", "Terbelah", 19, RevelationType.MAKKIYAH),
        Surah(83, "المطففين", "Al-Mutaffifin", "Orang-Orang Curang", 36, RevelationType.MAKKIYAH),
        Surah(84, "الانشقاق", "Al-Insyiqaq", "Terbelah", 25, RevelationType.MAKKIYAH),
        Surah(85, "البروج", "Al-Buruj", "Gugusan Bintang", 22, RevelationType.MAKKIYAH),
        Surah(86, "الطارق", "At-Tariq", "Yang Datang di Malam Hari", 17, RevelationType.MAKKIYAH),
        Surah(87, "الأعلى", "Al-A'la", "Maha Tinggi", 19, RevelationType.MAKKIYAH),
        Surah(88, "الغاشية", "Al-Ghasyiyah", "Hari Pembalasan", 26, RevelationType.MAKKIYAH),
        Surah(89, "الفجر", "Al-Fajr", "Fajar", 30, RevelationType.MAKKIYAH),
        Surah(90, "البلد", "Al-Balad", "Negeri", 20, RevelationType.MAKKIYAH),
        Surah(91, "الشمس", "Asy-Syams", "Matahari", 15, RevelationType.MAKKIYAH),
        Surah(92, "الليل", "Al-Lail", "Malam", 21, RevelationType.MAKKIYAH),
        Surah(93, "الضحى", "Ad-Duha", "Waktu Duha", 11, RevelationType.MAKKIYAH),
        Surah(94, "الشرح", "Al-Insyirah", "Melapangkan", 8, RevelationType.MAKKIYAH),
        Surah(95, "التين", "At-Tin", "Buah Tin", 8, RevelationType.MAKKIYAH),
        Surah(96, "العلق", "Al-'Alaq", "Segumpal Darah", 19, RevelationType.MAKKIYAH),
        Surah(97, "القدر", "Al-Qadr", "Kemuliaan", 5, RevelationType.MAKKIYAH),
        Surah(98, "البينة", "Al-Bayyinah", "Bukti Nyata", 8, RevelationType.MADANIYAH),
        Surah(99, "الزلزلة", "Az-Zalzalah", "Kegoncangan", 8, RevelationType.MADANIYAH),
        Surah(100, "العاديات", "Al-'Adiyat", "Kuda Perang", 11, RevelationType.MAKKIYAH),
        Surah(101, "القارعة", "Al-Qari'ah", "Hari Kiamat", 11, RevelationType.MAKKIYAH),
        Surah(102, "التكاثر", "At-Takasur", "Bermegah-Megahan", 8, RevelationType.MAKKIYAH),
        Surah(103, "العصر", "Al-'Asr", "Masa/Waktu", 3, RevelationType.MAKKIYAH),
        Surah(104, "الهمزة", "Al-Humazah", "Pengumpat", 9, RevelationType.MAKKIYAH),
        Surah(105, "الفيل", "Al-Fil", "Gajah", 5, RevelationType.MAKKIYAH),
        Surah(106, "قريش", "Quraisy", "Suku Quraisy", 4, RevelationType.MAKKIYAH),
        Surah(107, "الماعون", "Al-Ma'un", "Barang-Barang Berguna", 7, RevelationType.MAKKIYAH),
        Surah(108, "الكوثر", "Al-Kausar", "Nikmat Berlimpah", 3, RevelationType.MAKKIYAH),
        Surah(109, "الكافرون", "Al-Kafirun", "Orang-Orang Kafir", 6, RevelationType.MAKKIYAH),
        Surah(110, "النصر", "An-Nasr", "Pertolongan", 3, RevelationType.MADANIYAH),
        Surah(111, "اللهب", "Al-Lahab", "Gejolak Api", 5, RevelationType.MAKKIYAH),
        Surah(112, "الإخلاص", "Al-Ikhlas", "Ikhlas/Keesaan Allah", 4, RevelationType.MAKKIYAH),
        Surah(113, "الفلق", "Al-Falaq", "Waktu Subuh", 5, RevelationType.MAKKIYAH),
        Surah(114, "الناس", "An-Nas", "Manusia", 6, RevelationType.MAKKIYAH)
    )

    override fun getAllSurahs(): Flow<List<Surah>> = flowOf(surahs)

    override fun searchSurahs(query: String): Flow<List<Surah>> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return flowOf(surahs)
        val filtered = surahs.filter { surah ->
            surah.nameLatin.lowercase().contains(trimmed) ||
            surah.translation.lowercase().contains(trimmed) ||
            surah.number.toString() == trimmed
        }
        return flowOf(filtered)
    }

    override fun getSurahByNumber(number: Int): Surah? = surahs.find { it.number == number }
}
