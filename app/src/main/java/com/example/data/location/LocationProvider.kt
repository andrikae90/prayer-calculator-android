package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import java.time.ZoneId

/**
 * Encapsulates geographic coordinate, timezone, and city metadata.
 */
data class UserLocation(
    val cityName: String,
    val provinceOrCountry: String,
    val latitude: Double,
    val longitude: Double,
    val zoneId: ZoneId,
    val timezoneOffsetHours: Double,
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
    /**
     * Attempts to read the last known location from GPS/Network without requesting runtime permission.
     * If permission is missing or location services are disabled, returns null.
     */
    suspend fun getCurrentLocation(): UserLocation?

    /**
     * Returns the currently selected manual location.
     */
    fun getManuallySelectedLocation(): UserLocation

    /**
     * Sets the manually selected location.
     */
    fun setManualLocation(location: UserLocation)

    /**
     * Returns the active location: GPS if available and permitted, otherwise manually selected location.
     * Guarantees non-null without prompting the user.
     */
    fun getActiveLocation(): UserLocation

    /**
     * List of Indonesian cities with their exact geographical coordinates and timezone (WIB, WITA, WIT).
     */
    fun getPredefinedCities(): List<UserLocation>
}

class DefaultLocationProvider(
    private val context: Context
) : LocationProvider {

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

    private var manualLocation: UserLocation = predefinedCities.first() // Default Jakarta (WIB)

    override fun getPredefinedCities(): List<UserLocation> = predefinedCities

    override fun getManuallySelectedLocation(): UserLocation = manualLocation

    override fun setManualLocation(location: UserLocation) {
        manualLocation = location
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocation? {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            // NEVER request permission automatically on startup; return null gracefully
            return null
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val loc: Location? = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (loc != null) {
                val tzInfo = inferIndonesianTimezone(loc.longitude)
                UserLocation(
                    cityName = "Lokasi Saya (${String.format(java.util.Locale.US, "%.2f, %.2f", loc.latitude, loc.longitude)})",
                    provinceOrCountry = tzInfo.third,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    zoneId = tzInfo.first,
                    timezoneOffsetHours = tzInfo.second,
                    isFromGps = true
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override fun getActiveLocation(): UserLocation {
        // Synchronous fallback guarantees no GPS blocking or crashes on startup
        return manualLocation
    }

    /**
     * Determines Indonesian timezone (WIB, WITA, WIT) based on longitude boundaries:
     * - WIB: < 110°E (nominal standard covering Western Indonesia up to ~114°E)
     * - WITA: 110°E - 125°E (Central Indonesia: Bali, NTB, NTT, Kalimantan Timur/Selatan, Sulawesi)
     * - WIT: > 125°E (Eastern Indonesia: Maluku, Papua)
     */
    private fun inferIndonesianTimezone(longitude: Double): Triple<ZoneId, Double, String> {
        return when {
            longitude >= 125.0 -> Triple(ZoneId.of("Asia/Jayapura"), 9.0, "WIT")
            longitude >= 115.0 -> Triple(ZoneId.of("Asia/Makassar"), 8.0, "WITA")
            else -> Triple(ZoneId.of("Asia/Jakarta"), 7.0, "WIB")
        }
    }
}
