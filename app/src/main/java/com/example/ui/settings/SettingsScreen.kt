package com.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppThemeSetting
import com.example.domain.model.CalculationMethod

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    var showLocationDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val popularCities = listOf(
        "Jakarta, Indonesia",
        "Surabaya, Jawa Timur",
        "Bandung, Jawa Barat",
        "Medan, Sumatera Utara",
        "Makassar, Sulawesi Selatan",
        "Semarang, Jawa Tengah",
        "Yogyakarta, D.I. Yogyakarta",
        "Palembang, Sumatera Selatan",
        "Banda Aceh, Aceh"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Sesuaikan preferensi waktu salat, tema, dan notifikasi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section: Waktu Salat & Lokasi
        item {
            SettingsGroupCard(title = "Waktu Salat & Lokasi") {
                SettingsItem(
                    icon = Icons.Filled.LocationOn,
                    title = "Lokasi Saat Ini",
                    subtitle = settings.cityName,
                    onClick = { showLocationDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsItem(
                    icon = Icons.Filled.Mosque,
                    title = "Metode Perhitungan",
                    subtitle = settings.calculationMethod.title,
                    onClick = { showMethodDialog = true }
                )
            }
        }

        // Section: Notifikasi & Adzan
        item {
            SettingsGroupCard(title = "Notifikasi & Pengingat") {
                SettingsSwitchItem(
                    icon = Icons.Filled.Notifications,
                    title = "Notifikasi Waktu Salat",
                    subtitle = "Tampilkan pengingat saat masuk waktu salat",
                    checked = settings.prayerNotificationEnabled,
                    onCheckedChange = { viewModel.togglePrayerNotification(it) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsSwitchItem(
                    icon = Icons.Filled.VolumeUp,
                    title = "Suara Adzan",
                    subtitle = "Kumandangkan suara adzan saat waktu tiba",
                    checked = settings.adzanSoundEnabled,
                    onCheckedChange = { viewModel.toggleAdzanSound(it) }
                )
            }
        }

        // Section: Tampilan & Bahasa
        item {
            SettingsGroupCard(title = "Tampilan & Bahasa") {
                SettingsItem(
                    icon = Icons.Filled.Brightness4,
                    title = "Tema Aplikasi",
                    subtitle = settings.themeSetting.title,
                    onClick = { showThemeDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = "Bahasa Antarmuka",
                    subtitle = settings.appLanguage,
                    onClick = { /* Bahasa Indonesia fixed */ }
                )
            }
        }

        // Section: Informasi & Privasi
        item {
            SettingsGroupCard(title = "Lainnya") {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "Tentang Aplikasi",
                    subtitle = "Versi 1.0 (Muslim Mas An)",
                    onClick = { showAboutDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsItem(
                    icon = Icons.Filled.Lock,
                    title = "Kebijakan Privasi",
                    subtitle = "Keamanan data & privasi pengguna",
                    onClick = { showPrivacyDialog = true }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Dialog: Pilih Kota
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Pilih Kota / Lokasi") },
            text = {
                Column {
                    popularCities.forEach { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateCity(city)
                                    showLocationDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.cityName == city,
                                onClick = {
                                    viewModel.updateCity(city)
                                    showLocationDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = city, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("Tutup") }
            }
        )
    }

    // Dialog: Metode Perhitungan
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = { Text("Metode Perhitungan") },
            text = {
                Column {
                    CalculationMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateCalculationMethod(method)
                                    showMethodDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.calculationMethod == method,
                                onClick = {
                                    viewModel.updateCalculationMethod(method)
                                    showMethodDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = method.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text(text = method.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) { Text("Tutup") }
            }
        )
    }

    // Dialog: Tema
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Pilih Tema Tampilan") },
            text = {
                Column {
                    AppThemeSetting.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.updateTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.themeSetting == theme,
                                onClick = {
                                    viewModel.updateTheme(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = theme.title, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Tutup") }
            }
        )
    }

    // Dialog: Tentang Aplikasi
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Muslim Mas An") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Versi: 1.0.0 (Tahap 1 Foundation)")
                    Text(text = "Aplikasi Muslim all-in-one yang dirancang bersih, tenang, tanpa iklan pelacak, dan ramah pengguna.")
                    Text(
                        text = "Dibangun menggunakan teknologi native Kotlin dan Jetpack Compose dengan Material 3 Design.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Tutup") }
            }
        )
    }

    // Dialog: Kebijakan Privasi
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Kebijakan Privasi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Muslim Mas An menghormati privasi penuh pengguna:")
                    Text(text = "• Izin lokasi hanya diakses saat pengguna meminta kalibrasi kompas kiblat atau perhitungan salat.")
                    Text(text = "• Aplikasi tidak mengumpulkan, menjual, atau melacak data pribadi pengguna.")
                    Text(text = "• Bekerja secara offline-first untuk menjaga ketenangan beribadah.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Tutup") }
            }
        )
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
