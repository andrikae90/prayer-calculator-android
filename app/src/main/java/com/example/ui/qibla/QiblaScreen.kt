package com.example.ui.qibla

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel,
    modifier: Modifier = Modifier
) {
    val compassState by viewModel.compassState.collectAsState()
    val context = LocalContext.current
    var permissionStatusMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            try {
                val locManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastLoc = locManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastLoc != null) {
                    viewModel.onLocationUpdated(
                        lastLoc.latitude,
                        lastLoc.longitude,
                        "Lokasi GPS Saat Ini"
                    )
                    permissionStatusMessage = "Lokasi GPS berhasil diperbarui."
                } else {
                    permissionStatusMessage = "Izin lokasi aktif. Menggunakan titik acuan saat ini."
                }
            } catch (e: SecurityException) {
                permissionStatusMessage = "Tidak dapat mengakses GPS."
            }
        } else {
            permissionStatusMessage = "Izin lokasi ditolak. Arah kiblat menggunakan kota bawaan."
        }
    }

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose {
            viewModel.stopListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Location Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Arah Kiblat",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Arah Ka'bah: ${String.format("%.1f", compassState.qiblaBearing)}° dari Utara",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButtonCustom(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                }

                if (permissionStatusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = permissionStatusMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sensor Status Banner
        if (!compassState.isSensorAvailable) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sensor Kompas Tidak Terdeteksi",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Perangkat ini tidak memiliki sensor geomagnetik/rotasi fisik. Kompas komputasi arah Ka'bah tetap dihitung (${String.format("%.1f", compassState.qiblaBearing)}°), namun rotasi otomatis memerlukan sensor perangkat nyata.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (compassState.isFacingQibla) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (compassState.isFacingQibla) Icons.Filled.CheckCircle else Icons.Filled.CompassCalibration,
                        contentDescription = null,
                        tint = if (compassState.isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (compassState.isFacingQibla) "Tepat Menghadap Kiblat!" else "Sensor Aktif: ${compassState.sensorName}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (compassState.isFacingQibla) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Compass Visualizer
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            CompassDial(
                headingDegrees = compassState.headingDegrees,
                qiblaBearing = compassState.qiblaBearing,
                isFacingQibla = compassState.isFacingQibla,
                isSensorAvailable = compassState.isSensorAvailable
            )
        }

        // Status Details Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Sudut Perangkat",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${compassState.headingDegrees.roundToInt()}°",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Derajat Ka'bah",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${compassState.qiblaBearing.roundToInt()}° (BL)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tips Kalibrasi: Letakkan ponsel di bidang datar dan jauhkan dari benda logam/magnet. Putar perlahan membentuk angka delapan jika jarum terasa kurang stabil.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun IconButtonCustom(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.MyLocation,
            contentDescription = "Perbarui GPS",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "GPS", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun CompassDial(
    headingDegrees: Float,
    qiblaBearing: Float,
    isFacingQibla: Boolean,
    isSensorAvailable: Boolean
) {
    val animatedHeading by animateFloatAsState(
        targetValue = headingDegrees,
        animationSpec = tween(durationMillis = 250),
        label = "HeadingAnim"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 16.dp.toPx()

        // Outer ring
        drawCircle(
            color = if (isFacingQibla) primaryColor else surfaceVariant,
            radius = radius,
            center = center,
            style = Stroke(width = 6.dp.toPx())
        )

        // Inner subtle disc
        drawCircle(
            color = surfaceVariant.copy(alpha = 0.35f),
            radius = radius - 10.dp.toPx(),
            center = center
        )

        // Rotate the dial based on device heading
        rotate(-animatedHeading, pivot = center) {
            // Draw Cardinal Directions (N, E, S, W)
            val textPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = 36f
                color = android.graphics.Color.GRAY
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val northPaint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                textSize = 42f
                isFakeBoldText = true
                color = android.graphics.Color.RED
                textAlign = android.graphics.Paint.Align.CENTER
            }

            // North
            drawContext.canvas.nativeCanvas.drawText("U", center.x, center.y - radius + 32.dp.toPx(), northPaint)
            // East
            drawContext.canvas.nativeCanvas.drawText("T", center.x + radius - 20.dp.toPx(), center.y + 12f, textPaint)
            // South
            drawContext.canvas.nativeCanvas.drawText("S", center.x, center.y + radius - 14.dp.toPx(), textPaint)
            // West
            drawContext.canvas.nativeCanvas.drawText("B", center.x - radius + 20.dp.toPx(), center.y + 12f, textPaint)

            // Tick marks
            for (angle in 0 until 360 step 30) {
                val rad = Math.toRadians(angle.toDouble())
                val startX = center.x + (radius - 12.dp.toPx()) * sin(rad).toFloat()
                val startY = center.y - (radius - 12.dp.toPx()) * cos(rad).toFloat()
                val endX = center.x + radius * sin(rad).toFloat()
                val endY = center.y - radius * cos(rad).toFloat()
                drawLine(
                    color = onSurface.copy(alpha = 0.4f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Draw Qibla marker on the compass ring
            rotate(qiblaBearing, pivot = center) {
                // Kaaba needle
                val needlePath = Path().apply {
                    moveTo(center.x, center.y - radius + 10.dp.toPx())
                    lineTo(center.x - 14.dp.toPx(), center.y - 20.dp.toPx())
                    lineTo(center.x + 14.dp.toPx(), center.y - 20.dp.toPx())
                    close()
                }
                drawPath(needlePath, color = secondaryColor)

                // Ka'bah indicator icon
                drawCircle(
                    color = primaryColor,
                    radius = 12.dp.toPx(),
                    center = Offset(center.x, center.y - radius + 36.dp.toPx())
                )
            }
        }

        // Center hub
        drawCircle(
            color = if (isFacingQibla) primaryColor else onSurface,
            radius = 10.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = center
        )
    }
}
