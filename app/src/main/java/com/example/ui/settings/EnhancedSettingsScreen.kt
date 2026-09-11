package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.location.UserLocation
import com.example.domain.model.PrayerNotificationSound

@Composable
fun EnhancedSettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    var showManualLocation by remember { mutableStateOf(false) }
    var showNotificationSettings by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize()) {
        SettingsScreen(viewModel = viewModel)
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExtendedFloatingActionButton(onClick = { showNotificationSettings = true }, icon = { Icon(Icons.Filled.Notifications, contentDescription = null) }, text = { Text("Atur Notifikasi") })
            ExtendedFloatingActionButton(onClick = { showManualLocation = true }, icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) }, text = { Text("Pilih Lokasi Manual") })
        }
    }
    if (showManualLocation) ManualLocationDialog(viewModel = viewModel, onDismiss = { showManualLocation = false })
    if (showNotificationSettings) NotificationSettingsDialog(viewModel = viewModel, onDismiss = { showNotificationSettings = false })
}

@Composable
private fun NotificationSettingsDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.let { runCatching { if (it.isPlaying) it.stop() }; it.release() }
            previewPlayer = null
        }
    }

    fun preview(sound: PrayerNotificationSound) {
        previewPlayer?.let { runCatching { if (it.isPlaying) it.stop() }; it.release() }
        previewPlayer = null
        when (sound) {
            PrayerNotificationSound.ADZAN_LENGKAP -> previewPlayer = playAudioPreview(context, R.raw.adzan_lengkap) { previewPlayer = null }
            PrayerNotificationSound.TAKBIR_SAJA -> previewPlayer = playAudioPreview(context, R.raw.takbir_saja) { previewPlayer = null }
            PrayerNotificationSound.BIP_PANJANG -> playBeepPreview(context)
            PrayerNotificationSound.GETAR_SAJA -> vibratePreview(context)
            PrayerNotificationSound.TANPA_NOTIFIKASI -> Unit
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan Notifikasi", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                item {
                    Text("Pilihan suara", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Tekan pilihan suara untuk mendengarkan contoh audio. Pilihan tetap tersimpan sampai Anda menekan Selesai.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(PrayerNotificationSound.values().toList()) { sound ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.selectNotificationSound(sound); preview(sound) }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = settings.notificationSound == sound, onClick = { viewModel.selectNotificationSound(sound); preview(sound) })
                        Spacer(Modifier.width(8.dp))
                        Text(sound.title)
                    }
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Aktif per waktu sholat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                item { NotificationToggle("Subuh", settings.subuhNotificationEnabled) { viewModel.toggleSubuhNotification(it) } }
                item { NotificationToggle("Dzuhur", settings.dzuhurNotificationEnabled) { viewModel.toggleDzuhurNotification(it) } }
                item { NotificationToggle("Ashar", settings.asharNotificationEnabled) { viewModel.toggleAsharNotification(it) } }
                item { NotificationToggle("Maghrib", settings.maghribNotificationEnabled) { viewModel.toggleMaghribNotification(it) } }
                item { NotificationToggle("Isya", settings.isyaNotificationEnabled) { viewModel.toggleIsyaNotification(it) } }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                previewPlayer?.let { runCatching { if (it.isPlaying) it.stop() }; it.release() }
                previewPlayer = null
                onDismiss()
            }) { Text("Selesai") }
        }
    )
}

private fun playAudioPreview(context: Context, resourceId: Int, onFinished: () -> Unit): MediaPlayer? = runCatching {
    MediaPlayer.create(context, resourceId)?.apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        setOnCompletionListener { release(); onFinished() }
        start()
    }
}.getOrNull()

private fun playBeepPreview(context: Context) {
    val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    tone.startTone(ToneGenerator.TONE_PROP_BEEP, 1200)
    android.os.Handler(context.mainLooper).postDelayed({ tone.release() }, 1300)
}

private fun vibratePreview(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 250, 500), -1))
    else { @Suppress("DEPRECATION") vibrator.vibrate(longArrayOf(0, 500, 250, 500), -1) }
}

@Composable
private fun NotificationToggle(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ManualLocationDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.locationResults.collectAsState()
    val searching by viewModel.isSearchingLocation.collectAsState()
    val updatingGps by viewModel.isUpdatingGps.collectAsState()
    val error by viewModel.locationError.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { viewModel.clearLocationResults(); onDismiss() },
        title = { Text("Pilih Lokasi Manual", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                item {
                    Text(
                        "Cari nama desa/kelurahan atau kecamatan. Pilih hasil yang sesuai; koordinatnya akan dipakai untuk menghitung waktu sholat.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it; viewModel.searchLocations(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Cari desa / kecamatan") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = { if (searching) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                    )
                }
                if (error != null) {
                    item { Text(error.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                }
                if (results.isEmpty() && query.length >= 3 && !searching && error == null) {
                    item { Text("Lokasi tidak ditemukan. Coba nama desa atau kecamatan yang lebih spesifik.", style = MaterialTheme.typography.bodySmall) }
                }
                items(results) { location ->
                    LocationResultRow(location) {
                        viewModel.selectLocation(location)
                        onDismiss()
                    }
                }
                item { HorizontalDivider() }
                item {
                    TextButton(
                        onClick = { refreshGpsLocation(context, viewModel) },
                        enabled = !updatingGps,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (updatingGps) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Filled.MyLocation, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (updatingGps) "Mengambil lokasi GPS..." else "Gunakan Lokasi GPS")
                    }
                }
                item { Text("Jika GPS mati, tombol akan membuka Pengaturan Lokasi HP.", style = MaterialTheme.typography.labelSmall) }
                item { Text("Lokasi aktif: ${settings.cityName}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold) }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.clearLocationResults(); onDismiss() }) { Text("Tutup") }
        }
    )
}

private fun refreshGpsLocation(context: Context, viewModel: SettingsViewModel) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    val networkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    if (!gpsEnabled && !networkEnabled) { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); return }
    viewModel.useGpsLocation()
}

@Composable
private fun LocationResultRow(location: UserLocation, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(10.dp)) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(location.cityName, fontWeight = FontWeight.SemiBold)
            Text(
                "${location.provinceOrCountry} • ${location.latitude.formatCoord()}, ${location.longitude.formatCoord()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Double.formatCoord(): String = String.format(java.util.Locale.US, "%.5f", this)