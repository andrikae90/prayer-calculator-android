package com.example.data.repository

import com.example.domain.model.DuaCategory
import com.example.domain.model.DuaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DuaRepository {
    fun getCategories(): List<DuaCategory>
    fun getDuasByCategory(category: DuaCategory): Flow<List<DuaItem>>
    fun searchDuas(query: String): Flow<List<DuaItem>>
}

class LocalDuaRepository : DuaRepository {

    private val allDuas: List<DuaItem> = listOf(
        // Doa Harian
        DuaItem(
            id = "dh_1",
            title = "Doa Kebaikan Dunia dan Akhirat (Sapu Jagat)",
            category = DuaCategory.HARIAN,
            arabic = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            latin = "Rabbana aatina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adzaban-nar.",
            translation = "Ya Tuhan kami, berilah kami kebaikan di dunia dan kebaikan di akhirat dan peliharalah kami dari siksa neraka.",
            reference = "QS. Al-Baqarah: 201"
        ),
        DuaItem(
            id = "dh_2",
            title = "Doa Keluar Rumah",
            category = DuaCategory.HARIAN,
            arabic = "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            latin = "Bismillahi tawakkaltu 'alallahi, la haula wa la quwwata illa billah.",
            translation = "Dengan nama Allah, aku bertawakal kepada Allah. Tiada daya dan kekuatan kecuali dengan pertolongan Allah.",
            reference = "HR. Abu Dawud no. 5095 & At-Tirmidzi no. 3426"
        ),
        DuaItem(
            id = "dh_3",
            title = "Doa Masuk Rumah",
            category = DuaCategory.HARIAN,
            arabic = "بِسْمِ اللَّهِ وَلَجْنَا وَبِسْمِ اللَّهِ خَرَجْنَا وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
            latin = "Bismillahi walajnaa, wa bismillahi kharajnaa, wa 'alallahi rabbinaa tawakkalnaa.",
            translation = "Dengan nama Allah kami masuk, dan dengan nama Allah kami keluar, dan kepada Allah Tuhan kami, kami bertawakkal.",
            reference = "HR. Abu Dawud no. 5096"
        ),

        // Doa Makan
        DuaItem(
            id = "dm_1",
            title = "Doa Sebelum Makan",
            category = DuaCategory.MAKAN,
            arabic = "بِسْمِ اللَّهِ",
            latin = "Bismillah.",
            translation = "Dengan menyebut nama Allah.",
            reference = "HR. Bukhari no. 5376 & Muslim no. 2017"
        ),
        DuaItem(
            id = "dm_2",
            title = "Doa Setelah Makan",
            category = DuaCategory.MAKAN,
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            latin = "Alhamdulillahilladzi ath'amani hadza wa razaqanihi min ghairi haulin minni wa la quwwah.",
            translation = "Segala puji bagi Allah yang telah memberiku makanan ini dan menjadikannya rezeki bagiku tanpa daya dan kekuatan dariku.",
            reference = "HR. Abu Dawud no. 4023 & At-Tirmidzi no. 3458"
        ),

        // Doa Tidur
        DuaItem(
            id = "dt_1",
            title = "Doa Sebelum Tidur",
            category = DuaCategory.TIDUR,
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            latin = "Bismika Allahumma amutu wa ahya.",
            translation = "Dengan nama-Mu ya Allah, aku mati dan aku hidup.",
            reference = "HR. Bukhari no. 6324 & Muslim no. 2711"
        ),
        DuaItem(
            id = "dt_2",
            title = "Doa Bangun Tidur",
            category = DuaCategory.TIDUR,
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            latin = "Alhamdulillahilladzi ahyana ba'da ma amatana wa ilaihin-nusyur.",
            translation = "Segala puji bagi Allah yang menghidupkan kami setelah mematikan kami dan kepada-Nya kami dibangkitkan.",
            reference = "HR. Bukhari no. 6312"
        ),

        // Doa Perjalanan
        DuaItem(
            id = "dp_1",
            title = "Doa Naik Kendaraan",
            category = DuaCategory.PERJALANAN,
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            latin = "Subhanalladzi sakh-khara lana hadza wa ma kunna lahu muqrinin, wa inna ila rabbina lamunqalibun.",
            translation = "Maha Suci Tuhan yang telah menundukkan semua ini bagi kami padahal kami sebelumnya tidak mampu menguasainya, dan sesungguhnya kami akan kembali kepada Tuhan kami.",
            reference = "HR. Abu Dawud no. 2602 & At-Tirmidzi no. 3446"
        ),
        DuaItem(
            id = "dp_2",
            title = "Doa Bepergian / Safar",
            category = DuaCategory.PERJALANAN,
            arabic = "اللَّهُمَّ إِنَّا نَسْأَلُكَ فِي سَفَرِنَا هَذَا الْبِرَّ وَالتَّقْوَى وَمِنَ الْعَمَلِ مَا تَرْضَى",
            latin = "Allahumma inna nas-aluka fi safarina hadzal-birra wat-taqwa, wa minal-'amali ma tardha.",
            translation = "Ya Allah, kami memohon kepada-Mu dalam perjalanan kami ini kebaikan dan ketakwaan serta amal perbuatan yang Engkau ridhai.",
            reference = "HR. Muslim no. 1342"
        ),

        // Doa Setelah Salat
        DuaItem(
            id = "ds_1",
            title = "Sayyidul Istighfar",
            category = DuaCategory.SETELAH_SALAT,
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            latin = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastatha'tu, a'udzu bika min syarri ma shana'tu, abu-u laka bini'matika 'alayya wa abu-u bi dzanbi, faghfirli fa innahu la yaghfirudz-dzunuba illa Anta.",
            translation = "Ya Allah, Engkaulah Tuhanku, tiada sesembahan yang berhak disembah selain Engkau. Engkau menciptakanku dan aku hamba-Mu. Aku berada di atas janji dan ikrar-Mu semampuku. Aku berlindung kepada-Mu dari keburukan yang kuperbuat. Aku mengakui nikmat-Mu atasku dan aku mengakui dosaku, maka ampunilah aku, sesungguhnya tiada yang mengampuni dosa selain Engkau.",
            reference = "HR. Bukhari no. 6306"
        ),
        DuaItem(
            id = "ds_2",
            title = "Doa Perlindungan Usai Salat",
            category = DuaCategory.SETELAH_SALAT,
            arabic = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            latin = "Allahumma a'inni 'ala dzikrika wa syukrika wa husni 'ibadatik.",
            translation = "Ya Allah, tolonglah aku untuk senantiasa mengingat-Mu, bersyukur kepada-Mu, dan memperbaiki ibadah kepada-Mu.",
            reference = "HR. Abu Dawud no. 1522 & An-Nasa'i no. 1303"
        ),

        // Doa Ramadan
        DuaItem(
            id = "dr_1",
            title = "Doa Berbuka Puasa",
            category = DuaCategory.RAMADAN,
            arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
            latin = "Dzahabazh-zhama'u wabtallatil-'uruqu wa tsabatal-ajru in sya Allah.",
            translation = "Telah hilang rasa haus, telah basah urat-urat, dan telah pasti pahala insya Allah.",
            reference = "HR. Abu Dawud no. 2357 (Shahih)"
        ),
        DuaItem(
            id = "dr_2",
            title = "Doa Malam Lailatul Qadar",
            category = DuaCategory.RAMADAN,
            arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            latin = "Allahumma innaka 'afuwwun tuhibbul-'afwa fa'fu 'anni.",
            translation = "Ya Allah, sesungguhnya Engkau Maha Pemaaf dan senang memaafkan, maka maafkanlah aku.",
            reference = "HR. At-Tirmidzi no. 3513 & Ibnu Majah no. 3850"
        )
    )

    override fun getCategories(): List<DuaCategory> = DuaCategory.values().toList()

    override fun getDuasByCategory(category: DuaCategory): Flow<List<DuaItem>> {
        return flowOf(allDuas.filter { it.category == category })
    }

    override fun searchDuas(query: String): Flow<List<DuaItem>> {
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) return flowOf(allDuas)
        return flowOf(allDuas.filter {
            it.title.lowercase().contains(trimmed) ||
            it.translation.lowercase().contains(trimmed) ||
            it.latin.lowercase().contains(trimmed)
        })
    }
}
