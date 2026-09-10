package com.example.ui.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.QuranAyah
import com.example.domain.model.Surah

@Composable
fun QuranScreen(viewModel: QuranViewModel, modifier: Modifier = Modifier) {
    val surahs by viewModel.surahs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSurah by viewModel.selectedSurah.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val loading by viewModel.isLoadingAyahs.collectAsState()
    val error by viewModel.ayahError.collectAsState()

    Column(modifier.fillMaxSize().testTag("quran_screen")) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Al-Qur'an Al-Karim", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Text("114 surat • baca ayat Arab dan terjemahan Indonesia", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Cari surat atau arti...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Cari") },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { viewModel.onSearchQueryChanged("") }) { Icon(Icons.Filled.Clear, contentDescription = "Hapus") } },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("quran_search_field")
            )
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(surahs, key = { it.number }) { surah -> SurahItemCard(surah, onClick = { viewModel.selectSurah(surah) }) }
        }
    }

    selectedSurah?.let { surah ->
        QuranReaderDialog(surah, ayahs, loading, error, viewModel::retryAyahs, viewModel::clearSelectedSurah)
    }
}

@Composable
private fun SurahItemCard(surah: Surah, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).testTag("surah_item_${surah.number}")
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(surah.number.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(surah.nameLatin, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("${surah.translation} • ${surah.ayahCount} Ayat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(surah.nameArabic, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text(surah.revelationType.displayName, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun QuranReaderDialog(
    surah: Surah,
    ayahs: List<QuranAyah>,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val audioController = remember { QuranAudioController() }
    DisposableEffect(Unit) {
        onDispose { audioController.release() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("${surah.nameLatin} • ${surah.nameArabic}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("${surah.translation} • ${surah.ayahCount} ayat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            when {
                loading -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(error, textAlign = TextAlign.Center)
                    Button(onClick = onRetry) { Text("Coba lagi") }
                }
                ayahs.isEmpty() -> Text("Belum ada ayat yang dapat ditampilkan.")
                else -> LazyColumn(modifier = Modifier.fillMaxWidth().height(430.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ayahs, key = { it.number }) { ayah ->
                        AyahCard(
                            surahNumber = surah.number,
                            ayah = ayah,
                            isCurrent = audioController.currentKey == "${surah.number}:${ayah.number}",
                            isPlaying = audioController.isPlaying,
                            isLoadingAudio = audioController.isLoading,
                            onAudioClick = { audioController.toggle(surah.number, ayah.number) }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup", fontWeight = FontWeight.Bold) } }
    )
}

@Composable
private fun AyahCard(
    surahNumber: Int,
    ayah: QuranAyah,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isLoadingAudio: Boolean,
    onAudioClick: () -> Unit
) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(ayah.number.toString(), modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                IconButton(
                    onClick = onAudioClick,
                    enabled = !isLoadingAudio || isCurrent,
                    modifier = Modifier.testTag("ayah_audio_${surahNumber}_${ayah.number}")
                ) {
                    when {
                        isCurrent && isLoadingAudio -> CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        isCurrent && isPlaying -> Icon(Icons.Filled.Pause, contentDescription = "Jeda audio ayat ${ayah.number}")
                        else -> Icon(Icons.Filled.PlayArrow, contentDescription = "Putar audio ayat ${ayah.number}")
                    }
                }
            }
            Text(ayah.textArabic, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal, lineHeight = 36.sp))
            if (ayah.translation.isNotBlank()) {
                Text(ayah.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
