package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.location.LocationManager
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.location.UserLocation

@Composable
fun EnhancedSettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    var showManualLocation by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize()) {
        SettingsScreen(viewModel = viewModel)
        ExtendedFloatingActionButton(
            onClick = { showManualLocation = true },
            icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
            text = { Text("Pilih Lokasi Manual") },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
    if (showManualLocation) {
        ManualLocationDialog(
            viewModel = viewModel,
            onDismiss = { showManualLocation = false }
        )
    }
}

@Composable
private fun ManualLocationDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.locationResults.collectAsState()
    val searching by viewModel.isSearchingLocation.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { viewModel.clearLocationResults(); onDismiss() },
        title = { Text("Pilih Lokasi Manual", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Cari nama desa/kelurahan atau kecamatan. Koordinat lokasi akan dipakai untuk menghitung waktu salat.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; viewModel.searchLocations(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Cari desa / kecamatan") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = { if (searching) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
                )
                if (results.isEmpty() && query.length >= 3 && !searching) {
                    Text("Lokasi tidak ditemukan. Coba nama desa atau kecamatan yang lebih spesifik.", style = MaterialTheme.typography.bodySmall)
                }
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(results) { location ->
                        LocationResultRow(location) {
                            viewModel.selectLocation(location)
                            onDismiss()
                        }
                    }
                }
                HorizontalDivider()
                TextButton(
                    onClick = { refreshGpsLocation(context, viewModel) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Perbarui Lokasi GPS")
                }
                Text("Jika GPS mati, tombol ini akan membuka Pengaturan Lokasi HP.", style = MaterialTheme.typography.labelSmall)
                Text("Lokasi aktif: ${settings.cityName}", style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { TextButton(onClick = { viewModel.clearLocationResults(); onDismiss() }) { Text("Tutup") } }
    )
}

private fun refreshGpsLocation(context: Context, viewModel: SettingsViewModel) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    val networkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

    if (!gpsEnabled && !networkEnabled) {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        return
    }

    viewModel.useGpsLocation()
}

@Composable
private fun LocationResultRow(location: UserLocation, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(location.cityName, fontWeight = FontWeight.SemiBold)
            Text(
                "${location.latitude.formatCoord()}, ${location.longitude.formatCoord()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Double.formatCoord(): String = String.format(java.util.Locale.US, "%.5f", this)
