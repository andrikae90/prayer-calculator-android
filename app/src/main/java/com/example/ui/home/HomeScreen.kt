package com.example.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DailyReminder
import com.example.domain.model.PrayerScheduleItem
import com.example.domain.model.TodaySchedule
import com.example.ui.theme.HeroBorder
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientMid
import com.example.ui.theme.HeroGradientStart

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToPrayer: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToDua: () -> Unit,
    onNavigateToDzikr: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("home_loading")
                )
            }
        }
        is HomeUiState.Success -> {
            HomeContent(
                schedule = state.schedule,
                onNavigateToPrayer = onNavigateToPrayer,
                onNavigateToQibla = onNavigateToQibla,
                onNavigateToQuran = onNavigateToQuran,
                onNavigateToDua = onNavigateToDua,
                onNavigateToDzikr = onNavigateToDzikr,
                onNavigateToCalendar = onNavigateToCalendar,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HomeContent(
    schedule: TodaySchedule,
    onNavigateToPrayer: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToDua: () -> Unit,
    onNavigateToDzikr: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Bagian Atas: Salam, Judul, Chip Lokasi, Tanggal Masehi & Hijriah
        item {
            HeaderSection(
                cityName = schedule.locationName,
                dateMasehi = schedule.dateMasehiFormatted,
                dateHijri = schedule.dateHijriFormatted
            )
        }

        // 2. Kartu Utama "Salat Berikutnya" (Elemen Visual Utama)
        item {
            NextPrayerHeroCard(
                nextPrayer = schedule.nextPrayer,
                countdown = schedule.countdownText,
                onClick = onNavigateToPrayer
            )
        }

        // 3. Bagian "Fitur Utama" (Grid 3 Kolom Responsif)
        item {
            Column {
                Text(
                    text = "Fitur Utama",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                QuickActionsGrid(
                    onNavigateToPrayer = onNavigateToPrayer,
                    onNavigateToQibla = onNavigateToQibla,
                    onNavigateToQuran = onNavigateToQuran,
                    onNavigateToDua = onNavigateToDua,
                    onNavigateToDzikr = onNavigateToDzikr,
                    onNavigateToCalendar = onNavigateToCalendar
                )
            }
        }

        // 4. Bagian "Jadwal Salat Hari Ini" dengan Tombol "Lihat Semua"
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jadwal Salat Hari Ini",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToPrayer() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("see_all_prayers_button")
                    ) {
                        Text(
                            text = "Lihat Semua",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Lihat Semua",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(schedule.prayers) { item ->
                        PrayerCompactCard(item = item, onClick = onNavigateToPrayer)
                    }
                }
            }
        }

        // 5. Kartu "Pengingat Harian" (Ayat Al-Qur'an Valid & Terpercaya)
        item {
            DailyReminderCard(reminder = schedule.dailyReminder)
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Bagian Atas Home Screen
 * - Teks "Assalamu'alaikum,"
 * - Judul "Selamat Beraktivitas"
 * - Chip lokasi dengan ikon lokasi
 * - Tanggal Masehi & Tanggal Hijriah
 */
@Composable
private fun HeaderSection(
    cityName: String,
    dateMasehi: String,
    dateHijri: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Assalamu'alaikum,",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Selamat Beraktivitas",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.3).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Chip Lokasi dengan Ikon Lokasi
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .testTag("location_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Lokasi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cityName.split(",").firstOrNull()?.trim() ?: cityName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Baris Tanggal Masehi dan Tanggal Hijriah
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dateMasehi,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
            ) {
                Text(
                    text = dateHijri,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/**
 * Kartu Utama "Salat Berikutnya"
 * - Desain kartu besar dengan aksen hijau Islami
 * - Nama salat
 * - Waktu salat
 * - Countdown menuju waktu salat
 * - Ikon masjid
 * - Elemen visual utama pada halaman Home
 */
@Composable
private fun NextPrayerHeroCard(
    nextPrayer: PrayerScheduleItem?,
    countdown: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, HeroBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("hero_next_prayer_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            HeroGradientStart,
                            HeroGradientMid,
                            HeroGradientEnd
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Ikon Masjid dalam Kontainer Membulat
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mosque,
                                contentDescription = "Ikon Masjid",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Salat Berikutnya",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextPrayer?.type?.displayName ?: "Subuh",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Waktu Salat
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = nextPrayer?.time ?: "--:--",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "WIB",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bar Countdown Menuju Waktu Salat
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.28f),
                    border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Menuju waktu salat",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Countdown Timer Display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = countdown,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bagian "Fitur Utama"
 * - Jadwal Salat
 * - Kiblat
 * - Al-Qur'an
 * - Kumpulan Doa
 * - Dzikir & Tasbih
 * - Kalender
 * Responsif dalam grid 3 kolom
 */
@Composable
private fun QuickActionsGrid(
    onNavigateToPrayer: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToDua: () -> Unit,
    onNavigateToDzikr: () -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val items = listOf(
        QuickActionItem("Jadwal Salat", Icons.Filled.Mosque, onNavigateToPrayer, "quick_salat"),
        QuickActionItem("Kiblat", Icons.Filled.CompassCalibration, onNavigateToQibla, "quick_qibla"),
        QuickActionItem("Al-Qur'an", Icons.Filled.MenuBook, onNavigateToQuran, "quick_quran"),
        QuickActionItem("Kumpulan Doa", Icons.Filled.Favorite, onNavigateToDua, "quick_dua"),
        QuickActionItem("Dzikir & Tasbih", Icons.Filled.Timer, onNavigateToDzikr, "quick_dzikr"),
        QuickActionItem("Kalender", Icons.Filled.CalendarMonth, onNavigateToCalendar, "quick_calendar")
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Baris 1: 3 Kolom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.take(3).forEach { item ->
                    QuickActionCard(item = item, modifier = Modifier.weight(1f))
                }
            }
            // Baris 2: 3 Kolom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.drop(3).take(3).forEach { item ->
                    QuickActionCard(item = item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit,
    val testTag: String
)

@Composable
private fun QuickActionCard(
    item: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { item.action() }
            .testTag(item.testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(23.dp)
                )
            }
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Kartu Kompak Waktu Salat untuk Baris "Jadwal Salat Hari Ini"
 */
@Composable
private fun PrayerCompactCard(
    item: PrayerScheduleItem,
    onClick: () -> Unit
) {
    val isNext = item.isNext
    val isPassed = item.isPassed

    val cardBg = when {
        isNext -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isNext -> Color.White
        isPassed -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val border = if (isNext) {
        BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp),
        modifier = Modifier
            .width(92.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("compact_prayer_${item.type.name}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.type.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isNext) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.time,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                ),
                color = contentColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            when {
                isNext -> {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            text = "Berikutnya",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                isPassed -> {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Sudah Lewat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(13.dp)
                    )
                }
                else -> {
                    Box(modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

/**
 * Kartu "Pengingat Harian"
 * Menampilkan ayat Al-Qur'an terpercaya dari sumber data valid
 */
@Composable
private fun DailyReminderCard(
    reminder: DailyReminder?,
    modifier: Modifier = Modifier
) {
    val currentReminder = reminder ?: DailyReminder(
        id = 1,
        surahName = "Al-Baqarah",
        reference = "QS. Al-Baqarah: 152",
        arabicText = "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
        translation = "Maka ingatlah kepada-Ku, niscaya Aku ingat (pula) kepadamu, dan bersyukurlah kepada-Ku, dan janganlah kamu mengingkari (nikmat-Ku)."
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_reminder_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Pengingat Harian",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = currentReminder.category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teks Arab Al-Qur'an Valid
            Text(
                text = currentReminder.arabicText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 19.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Right
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Arti Terjemahan Indonesia
            Text(
                text = "\"${currentReminder.translation}\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sumber / Referensi Surat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = currentReminder.reference,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
