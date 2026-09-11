package com.example.ui.dzikr

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import com.example.domain.model.DzikrItem
import com.example.domain.model.TasbihState

@Composable
fun DzikrScreen(viewModel: DzikrViewModel, modifier: Modifier = Modifier) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val tasbihState by viewModel.tasbihState.collectAsState()
    val dzikrList by viewModel.currentDzikrList.collectAsState()

    Column(modifier.fillMaxSize().testTag("dzikr_screen")) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            DzikrTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = { Text(tab.displayName, fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("dzikr_tab_${tab.name}")
                )
            }
        }
        when (selectedTab) {
            DzikrTab.TASBIH -> TasbihDigitalContent(
                tasbihState = tasbihState,
                customPhrases = viewModel.customPhrases.collectAsState().value,
                onIncrement = viewModel::incrementTasbih,
                onReset = viewModel::resetTasbih,
                onSetTarget = viewModel::setTasbihTarget,
                onToggleVibration = viewModel::toggleVibration,
                onAddCustomPhrase = viewModel::addCustomPhrase
            )
            else -> DzikrReadingList(selectedTab.displayName, dzikrList)
        }
    }
}

@Composable
private fun TasbihDigitalContent(
    tasbihState: TasbihState,
    customPhrases: List<String>,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onSetTarget: (Int) -> Unit,
    onToggleVibration: () -> Unit,
    onAddCustomPhrase: (String) -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showCustomPhraseDialog by remember { mutableStateOf(false) }
    var targetInput by remember { mutableStateOf("") }
    var customPhraseInput by remember { mutableStateOf("") }
    var selectedPhrase by remember { mutableStateOf("Subhanallah") }
    val phrases = listOf("Subhanallah", "Alhamdulillah", "Allahu Akbar", "Astaghfirullah", "Laa Ilaha Illallah")

    val allPhrases = phrases + customPhrases.filterNot { it in phrases }
    val progress = if (tasbihState.target > 0) {
        (tasbihState.count.toFloat() / tasbihState.target.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(progress, tween(150), label = "TasbihProgress")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                items(allPhrases) { phrase ->
                    FilterChip(
                        selected = phrase == selectedPhrase,
                        onClick = { selectedPhrase = phrase },
                        label = { Text(phrase) }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = {
                            customPhraseInput = ""
                            showCustomPhraseDialog = true
                        },
                        label = { Text("Dzikir Sendiri") },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Target: ${tasbihState.target}x", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(33, 99, 100).forEach { targetValue ->
                        TargetChip(targetValue, tasbihState.target == targetValue) { onSetTarget(targetValue) }
                    }
                    TargetChip(null, false) {
                        targetInput = tasbihState.target.toString()
                        showTargetDialog = true
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Putaran Selesai: ${tasbihState.totalRounds}", style = MaterialTheme.typography.bodySmall)
        }

        Box(Modifier.size(240.dp).testTag("tasbih_counter_box"), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round
            )
            Box(
                modifier = Modifier.size(190.dp).clip(CircleShape).shadow(10.dp, CircleShape)
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)))
                    .clickable { onIncrement() }.testTag("tasbih_increment_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(selectedPhrase, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("${tasbihState.count}", style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp))
                    Text("/ ${tasbihState.target}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Ketuk untuk hitung", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = { showResetDialog = true }, modifier = Modifier.testTag("tasbih_reset_button")) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reset")
            }
            Button(
                onClick = onToggleVibration,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tasbihState.isVibrationEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (tasbihState.isVibrationEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Filled.Vibration, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (tasbihState.isVibrationEnabled) "Getar: Aktif" else "Getar: Nonaktif")
            }
        }
    }

    if (showCustomPhraseDialog) {
        AlertDialog(
            onDismissRequest = { showCustomPhraseDialog = false },
            title = { Text("Dzikir Sendiri") },
            text = {
                OutlinedTextField(
                    value = customPhraseInput,
                    onValueChange = { value -> if (value.length <= 100) customPhraseInput = value },
                    label = { Text("Tulis dzikir yang ingin dihitung") },
                    placeholder = { Text("Contoh: Laa ilaha illallah") },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val phrase = customPhraseInput.trim()
                    if (phrase.isNotEmpty()) {
                        onAddCustomPhrase(phrase)
                        selectedPhrase = phrase
                        showCustomPhraseDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showCustomPhraseDialog = false }) { Text("Batal") } }
        )
    }

    if (showTargetDialog) {
        AlertDialog(
            onDismissRequest = { showTargetDialog = false },
            title = { Text("Atur Target Bebas") },
            text = {
                Column {
                    Text("Masukkan jumlah hitungan sesuai kebutuhan. Tidak dibatasi 33, 99, atau 100.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { value -> if (value.all(Char::isDigit) && value.length <= 9) targetInput = value },
                        label = { Text("Jumlah target") },
                        suffix = { Text("x") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = targetInput.toLongOrNull()
                    if (value != null && value in 1..Int.MAX_VALUE) {
                        onSetTarget(value.toInt())
                        showTargetDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showTargetDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Hitungan?") },
            text = { Text("Hitungan tasbih saat ini (${tasbihState.count}) akan dikembalikan ke 0.") },
            confirmButton = {
                TextButton(onClick = { onReset(); showResetDialog = false }) { Text("Ya, Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun TargetChip(value: Int?, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick)
    ) {
        Text(
            text = value?.toString() ?: "Bebas",
            fontWeight = FontWeight.Bold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DzikrReadingList(title: String, items: List<DzikrItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items, key = { it.id }) { DzikrCard(it) }
    }
}

@Composable
private fun DzikrCard(item: DzikrItem) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("${item.repeatTarget}x", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(item.arabic, style = MaterialTheme.typography.headlineSmall.copy(lineHeight = 36.sp), textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Text(item.latin, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(item.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (item.note.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("Keutamaan: ${item.note}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
