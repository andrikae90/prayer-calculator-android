package com.example.data.repository

import com.example.domain.model.DzikrItem
import com.example.domain.model.DzikrType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface DzikrRepository {
    fun getDzikrList(type: DzikrType): Flow<List<DzikrItem>>
}

class LocalDzikrRepository : DzikrRepository {

    private val allDzikr: List<DzikrItem> = listOf(
        // Dzikir Pagi
        DzikrItem(
            id = "dz_pagi_1",
            title = "Membaca Ayat Kursi",
            type = DzikrType.PAGI,
            arabic = "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            latin = "Allahu laa ilaaha illaa Huwal Hayyul Qayyum, laa ta'khudzuhuu sinatuw-wa laa nawm...",
            translation = "Allah, tidak ada Tuhan selain Dia Yang Maha Hidup kekal lagi terus menerus mengurus makhluk-Nya...",
            repeatTarget = 1,
            note = "Barangsiapa membacanya di waktu pagi maka terlindung dari gangguan jin hingga sore (HR. Al-Hakim 1:562)"
        ),
        DzikrItem(
            id = "dz_pagi_2",
            title = "Menyambut Pagi dalam Fitrah Islam",
            type = DzikrType.PAGI,
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            latin = "Ashbahnaa wa ashbahal mulku lillaah, walhamdulillaah, laa ilaaha illallaahu wahdahu laa syariika lah, lahul mulku wa lahul hamdu wa Huwa 'alaa kulli syai-in qadiir.",
            translation = "Kami telah memasuki waktu pagi dan kerajaan hanya milik Allah. Segala puji bagi Allah, tiada sesembahan yang berhak disembah selain Allah semata...",
            repeatTarget = 1,
            note = "HR. Muslim no. 2723"
        ),
        DzikrItem(
            id = "dz_pagi_3",
            title = "Perlindungan dengan Nama Allah (3x)",
            type = DzikrType.PAGI,
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            latin = "Bismillaahilladzii laa yadhurru ma'as-mihii syai-un fil ardhi wa laa fis-samaa-i wa Huwas-Samii'ul 'Aliim.",
            translation = "Dengan nama Allah yang bila disebut, segala sesuatu di bumi dan langit tidak akan berbahaya, Dia-lah Yang Maha Mendengar lagi Maha Mengetahui.",
            repeatTarget = 3,
            note = "Dibaca 3x setiap pagi dan petang (HR. Abu Dawud no. 5088 & At-Tirmidzi no. 3388)"
        ),

        // Dzikir Petang
        DzikrItem(
            id = "dz_petang_1",
            title = "Menyambut Sore Hari",
            type = DzikrType.PETANG,
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            latin = "Amsaynaa wa amsal mulku lillaah, walhamdulillaah, laa ilaaha illallaahu wahdahu laa syariika lah.",
            translation = "Kami telah memasuki waktu sore dan kerajaan hanya milik Allah, segala puji bagi Allah...",
            repeatTarget = 1,
            note = "HR. Muslim no. 2723"
        ),
        DzikrItem(
            id = "dz_petang_2",
            title = "Memohon Perlindungan Kalimat Allah (3x)",
            type = DzikrType.PETANG,
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            latin = "A'uudzu bikalimaatillaahit-taammaati min syarri maa khalaq.",
            translation = "Aku berlindung dengan kalimat-kalimat Allah yang sempurna dari kejahatan makhluk yang Dia ciptakan.",
            repeatTarget = 3,
            note = "Barangsiapa mengucapkannya pada petang hari 3x, maka racun atau bahaya tidak akan memudharatkannya (HR. Muslim no. 2709)"
        ),

        // Dzikir Setelah Salat
        DzikrItem(
            id = "dz_salat_1",
            title = "Istighfar (3x)",
            type = DzikrType.SETELAH_SALAT,
            arabic = "أَسْتَغْفِرُ اللَّهَ",
            latin = "Astaghfirullah (3x)",
            translation = "Aku memohon ampunan kepada Allah.",
            repeatTarget = 3,
            note = "HR. Muslim no. 591"
        ),
        DzikrItem(
            id = "dz_salat_2",
            title = "Tasbih, Tahmid, Takbir (33x)",
            type = DzikrType.SETELAH_SALAT,
            arabic = "سُبْحَانَ اللَّهِ (٣٣x) ، الْحَمْدُ لِلَّهِ (٣٣x) ، اللَّهُ أَكْبَرُ (٣٣x)",
            latin = "Subhanallah (33x), Alhamdulillah (33x), Allahu Akbar (33x)",
            translation = "Maha Suci Allah (33x), Segala puji bagi Allah (33x), Allah Maha Besar (33x)",
            repeatTarget = 33,
            note = "Disempurnakan dengan Laa ilaha illallah wahdahu laa syarika lah (HR. Muslim no. 597)"
        )
    )

    override fun getDzikrList(type: DzikrType): Flow<List<DzikrItem>> {
        return flowOf(allDzikr.filter { it.type == type })
    }
}
