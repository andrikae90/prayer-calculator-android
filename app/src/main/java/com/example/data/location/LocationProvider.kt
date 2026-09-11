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
            val priority = if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
            val task = fusedLocationClient.getCurrentLocation(priority, cancellation.token)
            task.addOnSuccessListener { location: Location? ->
                if (continuation.isActive) continuation.resume(location?.toUserLocation())
            }
            task.addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            task.addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
            continuation.invokeOnCancellation { cancellation.cancel() }
        }
    }

    override fun getActiveLocation(): UserLocation = manualLocation

    override suspend fun searchLocations(query: String): List<UserLocation> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.length < 3 || !Geocoder.isPresent()) return@withContext emptyList()
        val addresses = try {
            Geocoder(context, Locale("id", "ID")).getFromLocationName(cleanQuery, 12).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }
        addresses.mapNotNull { it.toUserLocationOrNull() }
            .distinctBy { "${it.cityName}|${it.latitude}|${it.longitude}" }
    }

    private fun Address.toUserLocationOrNull(): UserLocation? {
        if (latitude.isNaN() || longitude.isNaN()) return null
        val label = buildLocationLabel(this)
        if (label.isBlank()) return null
        val tz = inferIndonesianTimezone(longitude)
        return UserLocation(
            cityName = label,
            provinceOrCountry = adminArea?.trim().orEmpty().ifBlank { "Indonesia" },
            latitude = latitude,
            longitude = longitude,
            zoneId = tz.first,
            timezoneOffsetHours = tz.second,
            elevationMeters = 0.0,
            isFromGps = false
        )
    }

    private fun Location.toUserLocation(): UserLocation {
        val tzInfo = inferIndonesianTimezone(longitude)
        val address = try {
            Geocoder(context, Locale("id", "ID"))
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
        } catch (_: Exception) {
            null
        }
        val label = address?.let { buildLocationLabel(it) }
        return UserLocation(
            cityName = label?.ifBlank { null }
                ?: "Lokasi Saya (${String.format(Locale.US, "%.5f, %.5f", latitude, longitude)})",
            provinceOrCountry = address?.adminArea?.trim().orEmpty().ifBlank { tzInfo.third },
            latitude = latitude,
            longitude = longitude,
            zoneId = tzInfo.first,
            timezoneOffsetHours = tzInfo.second,
            elevationMeters = if (hasAltitude()) altitude else 0.0,
            isFromGps = true
        )
    }

    /**
     * Build an Indonesian location label in the user-friendly order:
     * village, Kecamatan, Kabupaten/Kota. Android Geocoder implementations
     * can put these administrative levels in different Address fields, so
     * explicit Kecamatan/Kabupaten labels are prioritized before fallbacks.
     */
    private fun buildLocationLabel(address: Address): String {
        val raw = listOf(
            address.subLocality,
            address.locality,
            address.subAdminArea,
            address.adminArea,
            address.featureName
        ).map { it?.trim().orEmpty() }.filter { it.isNotBlank() }

        val kecamatan = raw.firstOrNull { it.contains("kecamatan", ignoreCase = true) }
            ?: address.subAdminArea?.trim().takeUnless { it.isNullOrBlank() }
        val kabupaten = raw.firstOrNull {
            it.contains("kabupaten", ignoreCase = true) || it.contains("kota", ignoreCase = true)
        } ?: address.locality?.trim().takeUnless { it.isNullOrBlank() && kecamatan == null }

        val village = address.subLocality?.trim().takeUnless { it.isNullOrBlank() }
            ?: address.featureName?.trim().takeUnless { it.isNullOrBlank() }
            ?: raw.firstOrNull { candidate ->
                candidate != kecamatan && candidate != kabupaten &&
                    !candidate.contains("provinsi", ignoreCase = true)
            }

        val parts = listOf(village, kecamatan, kabupaten)
            .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
            .distinctBy { it.lowercase(Locale.ROOT) }
        return parts.joinToString(", ")
    }

    private fun inferIndonesianTimezone(longitude: Double): Triple<ZoneId, Double, String> = when {
        longitude >= 125.0 -> Triple(ZoneId.of("Asia/Jayapura"), 9.0, "WIT")
        longitude >= 115.0 -> Triple(ZoneId.of("Asia/Makassar"), 8.0, "WITA")
        else -> Triple(ZoneId.of("Asia/Jakarta"), 7.0, "WIB")
    }
}
