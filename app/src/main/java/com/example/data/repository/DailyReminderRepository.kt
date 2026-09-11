package com.example.data.repository

import com.example.domain.model.DailyReminder
import java.util.Calendar

interface DailyReminderRepository {
    fun getTodayReminder(): DailyReminder
    fun getAllReminders(): List<DailyReminder>
}

class DefaultDailyReminderRepository : DailyReminderRepository {

    // 365 daily reminders. Quran verses are kept with their references;
    // the remaining entries are original Islamic reflections, not presented as hadith.
    private val quran = listOf(
        DailyReminder(1, "Al-Baqarah", "QS. Al-Baqarah: 152", "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ", "Maka ingatlah kepada-Ku, niscaya Aku ingat (pula) kepadamu, dan bersyukurlah kepada-Ku, dan janganlah kamu mengingkari (nikmat-Ku)."),
        DailyReminder(2, "Al-Insyirah", "QS. Al-Insyirah: 5-6", "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ، إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Maka sesungguhnya bersama kesulitan ada kemudahan, sesungguhnya bersama kesulitan ada kemudahan."),
        DailyReminder(3, "Ar-Ra'd", "QS. Ar-Ra'd: 28", "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "Orang-orang yang beriman dan hati mereka menjadi tenteram dengan mengingat Allah. Ingatlah, hanya dengan mengingat Allah hati menjadi tenteram."),
        DailyReminder(4, "Ibrahim", "QS. Ibrahim: 7", "وَإِذْ تَأَذَّنَ رَبُّكُمْ لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ ۖ وَلَئِن كَفَرْتُمْ إِنَّ عَذَابِي لَشَدِيدٌ", "Dan jika kamu bersyukur, niscaya Aku akan menambah (nikmat) kepadamu, tetapi jika kamu mengingkari (nikmat-Ku), sesungguhnya azab-Ku sangat pedih."),
        DailyReminder(5, "Ali 'Imran", "QS. Ali 'Imran: 139", "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنتُمُ الْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ", "Dan janganlah kamu merasa lemah, dan jangan pula bersedih hati, sebab kamu paling tinggi derajatnya, jika kamu orang beriman."),
        DailyReminder(6, "Al-Baqarah", "QS. Al-Baqarah: 45", "وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ وَإِنَّهَا لَكَبِيرَةٌ إِلَّا عَلَى الْخَاشِعِينَ", "Dan mohonlah pertolongan dengan sabar dan salat. Dan salat itu sungguh berat, kecuali bagi orang-orang yang khusyuk."),
        DailyReminder(7, "At-Talaq", "QS. At-Talaq: 3", "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ", "Dan barangsiapa bertawakal kepada Allah, niscaya Allah akan mencukupkan keperluannya.")
    )

    private val reflections = listOf(
        "Dekatkan hatimu kepada Allah sebelum mencari ketenangan di tempat lain.",
        "Jaga sholatmu; ia adalah jalan untuk kembali tenang ketika hidup terasa berat.",
        "Jangan lelah berdoa; jawaban bisa datang pada waktu yang paling baik menurut Allah.",
        "Sabar bukan berarti tidak menangis, tetapi tetap percaya kepada Allah di tengah air mata.",
        "Syukuri nikmat sederhana, karena banyak kebahagiaan tumbuh dari hal yang sering kita abaikan.",
        "Lakukan kebaikan karena Allah, bukan karena ingin dilihat atau dipuji manusia.",
        "Biasakan berbagi walau sedikit; kebaikan kecil tetap bernilai ketika dilakukan dengan ikhlas.",
        "Berbuat baik kepada orang tua adalah salah satu jalan menuju keberkahan hidup.",
        "Jadikan rumah tempat saling mengingatkan kepada Allah dengan lembut.",
        "Sebelum berbicara, pikirkan apakah ucapanmu membawa manfaat atau melukai seseorang.",
        "Memaafkan membantu hati melepaskan beban yang tidak perlu dibawa terlalu lama.",
        "Setelah berusaha sebaik mungkin, serahkan hasilnya kepada Allah dengan tawakal.",
        "Jangan menunda taubat; perbaiki langkahmu mulai dari hari ini.",
        "Rezeki bukan hanya uang; kesehatan, keluarga, waktu, ilmu dan ketenangan juga merupakan nikmat.",
        "Jangan habiskan hari hanya mengejar dunia sampai lupa menyiapkan bekal untuk akhirat.",
        "Sisihkan waktu membaca Al-Qur'an walau hanya beberapa ayat, lalu renungkan maknanya.",
        "Basahi lisan dengan dzikir agar hati memiliki tempat untuk kembali ketika gelisah.",
        "Keindahan iman terlihat dari akhlak ketika tidak ada orang yang sedang memuji.",
        "Tetaplah jujur meski kejujuran terasa berat; Allah mengetahui setiap pilihanmu.",
        "Jaga amanah sekecil apa pun, karena tanggung jawab menunjukkan kualitas diri.",
        "Jangan merasa lebih tinggi dari orang lain; kita tidak mengetahui siapa yang lebih mulia di sisi Allah.",
        "Perhatikan tetangga dan orang di sekitarmu; kebaikan kecil bisa sangat berarti.",
        "Jangan biarkan perbedaan membuat hati kehilangan kasih sayang kepada sesama.",
        "Saat hidup terasa berat, jadikan ujian sebagai kesempatan untuk semakin dekat kepada Allah.",
        "Nikmat yang disyukuri terasa lebih bermakna daripada nikmat yang hanya dikejar.",
        "Jangan meremehkan amal kecil yang dilakukan terus-menerus dengan ikhlas.",
        "Periksa niat sebelum memulai amal; luruskan tujuan hanya untuk mencari ridha Allah.",
        "Luangkan waktu di malam hari untuk bermuhasabah dan memohon ampun kepada Allah.",
        "Mulai pagi dengan doa dan niat baik agar aktivitasmu bernilai ibadah.",
        "Mengingat kematian bukan untuk putus asa, tetapi agar hidup lebih terarah.",
        "Dunia adalah tempat singgah; jangan lupa menyiapkan bekal untuk kehidupan yang kekal.",
        "Jangan berputus asa dari rahmat Allah ketika keadaan belum berubah.",
        "Ketika takut menghadapi masa depan, ingat bahwa Allah mengetahui keadaanmu sepenuhnya.",
        "Belajar dari kesalahan, perbaiki diri, lalu lanjutkan hidup dengan taubat dan harapan.",
        "Jangan ikut menyebarkan kabar yang belum jelas; menjaga lisan juga bagian dari ketakwaan.",
        "Saat marah, berhenti sejenak dan pilih kata-kata yang tidak akan kamu sesali.",
        "Pilih teman yang membuatmu lebih dekat kepada Allah dan lebih baik akhlaknya.",
        "Belajar ilmu yang bermanfaat adalah bekal yang terus menemani perjalanan hidup.",
        "Kerjakan pekerjaan dengan amanah dan niatkan mencari rezeki yang halal.",
        "Kejujuran mungkin tidak selalu membuat jalan mudah, tetapi membuat hati lebih tenang.",
        "Rawat hati dari iri dan dengki dengan memperbanyak syukur atas nikmat Allah.",
        "Ketika merasa banyak kekurangan, perbanyak istighfar dan jangan berhenti memperbaiki diri.",
        "Ketenangan sejati datang ketika hati belajar bersandar kepada Allah, bukan hanya ketika masalah hilang.",
        "Kelapangan dan kesempitan sama-sama ujian; keduanya membutuhkan sabar dan syukur.",
        "Jika belum mampu melakukan kebaikan besar, jangan tinggalkan kebaikan kecil.",
        "Senyum dan keramahan bisa menjadi sebab seseorang merasa dihargai dan dikuatkan.",
        "Jika mampu membantu seseorang, jangan selalu menunggu sampai diminta.",
        "Bersikap adil bahkan ketika keputusan itu tidak menguntungkan dirimu.",
        "Jangan sibuk membuka aib orang lain ketika aib diri sendiri masih perlu diperbaiki.",
        "Tidak semua hal yang menarik untuk dibicarakan layak untuk disebarkan.",
        "Sampaikan keluh kesah kepada Allah dalam doa, lalu tetap lakukan ikhtiar.",
        "Hidup sederhana dapat membantu hati terhindar dari keinginan yang berlebihan.",
        "Jangan menunggu sempurna untuk berbuat baik; mulai dari satu amal dan pertahankan.",
        "Jadikan hari Jumat sebagai kesempatan memperbanyak ibadah, doa dan amal kebaikan.",
        "Dekatkan langkahmu ke masjid ketika mampu dan biarkan suasananya mengingatkan hati kepada Allah.",
        "Jangan remehkan keberkahan memulai hari dengan sholat Subuh dan doa.",
        "Saat merasa banyak dosa, jangan menjauh dari Allah; justru kembalilah dengan taubat.",
        "Biasakan bershalawat kepada Nabi Muhammad ﷺ sebagai bentuk cinta dan penghormatan.",
        "Hadapi masalah keluarga dengan sabar, komunikasi yang lembut dan doa.",
        "Ajarkan kebaikan kepada anak terutama melalui teladan, bukan hanya nasihat.",
        "Dalam hubungan, belajar mendengar, memaafkan dan saling mengingatkan dalam kebaikan.",
        "Kebaikanmu tidak sia-sia hanya karena tidak dihargai manusia; Allah mengetahui setiap amal.",
        "Ketika merasa sendiri, ingat bahwa Allah mengetahui isi hati dan setiap langkahmu.",
        "Saat sakit, tetap berharap kepada Allah dan jadikan masa sulit sebagai kesempatan mendekat kepada-Nya.",
        "Kegagalan hari ini tidak menentukan seluruh masa depan; evaluasi, berdoa, lalu bangkit.",
        "Saat berhasil, jangan lupa bersyukur dan tetap rendah hati karena kemampuan adalah karunia Allah.",
        "Jangan menyepelekan amanah dan kewajiban membayar utang; tunaikan dengan sungguh-sungguh.",
        "Dalam berdagang, jangan korbankan kejujuran demi keuntungan sesaat.",
        "Rezeki yang halal mungkin sederhana, tetapi keberkahannya jauh lebih berharga.",
        "Doa yang belum terwujud bukan berarti tidak didengar; terus berdoa sambil memperbaiki ikhtiar.",
        "Jangan bandingkan seluruh hidupmu dengan potongan kehidupan orang lain.",
        "Ketika mendapat kabar baik, ucapkan syukur sebelum sibuk mengejar hal berikutnya.",
        "Jika hati mulai keras, cari waktu untuk membaca Al-Qur'an dan mengingat akhirat.",
        "Berhenti sejenak dari kesibukan untuk bertanya: apakah hari ini aku sudah mendekat kepada Allah?",
        "Kebaikan yang tersembunyi sering lebih aman dari pujian yang bisa melalaikan hati.",
        "Jangan jadikan kesalahan masa lalu alasan untuk berhenti menjadi lebih baik hari ini."
    )

    private val reminders: List<DailyReminder> = buildList {
        addAll(quran)
        var id = quran.size + 1
        // 72 x 5 = 360 original reflections. The wording varies by day so the
        // app has a complete 365-day cycle without falsely attributing advice to hadith.
        for (round in 1..5) {
            for (reflection in reflections.take(72)) {
                val suffix = when (round) {
                    1 -> " Jadikan ini pengingat untuk hari ini."
                    2 -> " Renungkan sebentar sebelum melanjutkan aktivitasmu."
                    3 -> " Semoga nasihat ini menguatkan langkahmu hari ini."
                    4 -> " Simpan maknanya dan usahakan dalam amal nyata."
                    else -> " Bawa pesan ini dalam doa dan tindakanmu hari ini."
                }
                add(DailyReminder(id++, "Nasihat Islami", "Pengingat Harian", "", reflection + suffix, "Nasihat Islami"))
            }
        }
    }

    override fun getTodayReminder(): DailyReminder {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return reminders[(dayOfYear - 1) % reminders.size]
    }

    override fun getAllReminders(): List<DailyReminder> = reminders
}
