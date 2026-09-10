package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.util.Locale
import kotlin.coroutines.resume

data class UserLocation(
    val cityName: String,
    val provinceOrCountry: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: ZoneId,
    val timezoneOffsetHours: Double,
    val elevationMeters: Double = 0.0,
    val isFromGps: Boolean = false
) {
    val timezoneName: String
        get() = when (timezoneOffsetHours) {
            7.0 -> "WIB (UTC+7)"
            8.0 -> "WITA (UTC+8)"
            9.0 -> "WIT (UTC+9)"
            else -> "UTC" + if (timezoneOffsetHours >= 0) "+$timezoneOffsetHours" else "$timezoneOffsetHours"
        }
}

interface LocationProvider {
    suspend fun getCurrentLocation(): UserLocation?
    fun getManuallySelectedLocation(): UserLocation
    fun setManualLocation(location: UserLocation)
    fun getActiveLocation(): UserLocation
    fun getPredefinedCities(): List<UserLocation>
    suspend fun searchLocations(query: String): List<UserLocation>
}

class DefaultLocationProvider(
    private val context: Context
) : LocationProvider {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val predefinedCities = listOf(
        UserLocation("Jakarta", "DKI Jakarta", -6.2088, 106.8456, ZoneId.of("Asia/Jakarta"), 7.0),
        UserLocation("Bandung", "Jawa Barat", -6.9175, 107.6191, ZoneId.of("Asia/Jakarta"), 7.0),
        UserLocation("Surabaya", "Jawa Timur", -7.2575, 112.7521, ZoneId.of("Asia/Jakarta"), 7.0),
        UserLocation("Semarang", "Jawa Tengah", -6.9667, 110.4167, ZoneId.of("Asia/Jakarta"), 7.0),
        UserLocation("Yogyakarta", "D.I. Yogyakarta", -7.7956, 110.3695, ZoneId.of("Asia/Jakarta"), 7.0),
        UserLocation("Medan", "Sumatera Utara", 3.5952, 98.6722, ZoneId.of("Asia/Jakarta"), 7.0),
        UserLocation("Palembang", "Sumatera Selatan", -2.9909, 104.7565, ZoneId.of("Asia/Jakarta"), 7.0),
        UserLocation("Makassar", "Sulawesi Selatan", -5.1477, 119.4327, ZoneId.of("Asia/Makassar"), 8.0),
        UserLocation("Denpasar", "Bali", -8.6705, 115.2126, ZoneId.of("Asia/Makassar"), 8.0),
        UserLocation("Balikpapan", "Kalimantan Timur", -1.2379, 116.8529, ZoneId.of("Asia/Makassar"), 8.0),
        UserLocation("Banjarmasin", "Kalimantan Selatan", -3.3167, 114.5900, ZoneId.of("Asia/Makassar"), 8.0),
        UserLocation("Manado", "Sulawesi Utara", 1.4748, 124.8428, ZoneId.of("Asia/Makassar"), 8.0),
        UserLocation("Jayapura", "Papua", -2.5337, 140.7181, ZoneId.of("Asia/Jayapura"), 9.0),
        UserLocation("Ambon", "Maluku", -3.6547, 128.1906, ZoneId.of("Asia/Jayapura"), 9.0),
        UserLocation("Sorong", "Papua Barat Daya", -0.8762, 131.2558, ZoneId.of("Asia/Jayapura"), 9.0)
    )

    private var manualLocation: UserLocation = predefinedCities.first()
    override fun getPredefinedCities(): List<UserLocation> = predefinedCities
    override fun getManuallySelectedLocation(): UserLocation = manualLocation
    override fun setManualLocation(location: UserLocation) { manualLocation = location }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocation? {
        val hasFine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return null
        return suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationTokenSource()
            val task = fusedLocationClient.getCurrentLocation(
                if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellation.token
            )
            task.addOnSuccessListener { location: Location? -> if (continuation.isActive) continuation.resume(location?.toUserLocation()) }
            task.addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            task.addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
            continuation.invokeOnCancellation { cancellation.cancel() }
        }
    }

    override fun getActiveLocation(): UserLocation = manualLocation

    override suspend fun searchLocations(query: String): List<UserLocation> = withContext(Dispatchers.IO) {
        if (query.trim().length < 3 || !Geocoder.isPresent()) return@withContext emptyList()
        val addresses = try {
            Geocoder(context, Locale("id", "ID")).getFromLocationName(query.trim(), 8).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        addresses.mapNotNull { it.toUserLocationOrNull() }.distinctBy { "${it.cityName}|${it.latitude}|${it.longitude}" }
    }

    private fun Address.toUserLocationOrNull(): UserLocation? {
        val lat = latitude
        val lon = longitude
        if (lat.isNaN() || lon.isNaN()) return null
        val subLocality = subLocality?.trim().orEmpty()
        val locality = locality?.trim().orEmpty()
        val subAdmin = subAdminArea?.trim().orEmpty()
        val admin = adminArea?.trim().orEmpty()
        val village = if (subLocality.isNotBlank()) subLocality else locality
        val main = if (village.isNotBlank()) village else subAdmin
        val hierarchy = listOfNotNull(
            main.takeIf { it.isNotBlank() },
            subAdmin.takeIf { it.isNotBlank() && it != main },
            admin.takeIf { it.isNotBlank() }
        ).joinToString(", ")
        val tz = inferIndonesianTimezone(lon)
        return UserLocation(
            cityName = hierarchy.ifBlank { featureName ?: "Lokasi Manual" },
            provinceOrCountry = admin.ifBlank { "Indonesia" },
            latitude = lat,
            longitude = lon,
            zoneId = tz.first,
            timezoneOffsetHours = tz.second,
            elevationMeters = 0.0,
            isFromGps = false
        )
    }

    private fun Location.toUserLocation(): UserLocation {
        val tzInfo = inferIndonesianTimezone(longitude)
        val geocoderName = try {
            Geocoder(context, Locale("id", "ID")).getFromLocation(latitude, longitude, 1)?.firstOrNull()?.let { address ->
                val village = address.subLocality?.trim().orEmpty().ifBlank { address.locality?.trim().orEmpty() }
                val kecamatan = address.subAdminArea?.trim().orEmpty()
                val kabupaten = address.adminArea?.trim().orEmpty()
                listOf(village, kecamatan, kabupaten).filter { it.isNotBlank() }.distinct().joinToString(", ")
            }
        } catch (_: Exception) { null }
        return UserLocation(
            cityName = geocoderName?.ifBlank { null } ?: "Lokasi Saya (${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)})",
            provinceOrCountry = tzInfo.third,
            latitude = latitude,
            longitude = longitude,
            zoneId = tzInfo.first,
            timezoneOffsetHours = tzInfo.second,
            elevationMeters = if (hasAltitude()) altitude else 0.0,
            isFromGps = true
        )
    }

    private fun inferIndonesianTimezone(longitude: Double): Triple<ZoneId, Double, String> = when {
        longitude >= 125.0 -> Triple(ZoneId.of("Asia/Jayapura"), 9.0, "WIT")
        longitude >= 115.0 -> Triple(ZoneId.of("Asia/Makassar"), 8.0, "WITA")
        else -> Triple(ZoneId.of("Asia/Jakarta"), 7.0, "WIB")
    }
}
