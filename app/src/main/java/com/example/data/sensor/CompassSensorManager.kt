package com.example.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

data class CompassState(
    val isSensorAvailable: Boolean = false,
    val sensorName: String = "",
    val headingDegrees: Float = 0f, // 0..360 device orientation relative to North
    val qiblaBearing: Float = 295.2f, // Calculated direction from user coordinates to Ka'bah
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
    val isCalibrated: Boolean = true
) {
    // Relative difference between device heading and Ka'bah direction (-180..180)
    val relativeQiblaAngle: Float
        get() {
            var diff = qiblaBearing - headingDegrees
            while (diff < -180f) diff += 360f
            while (diff > 180f) diff -= 360f
            return diff
        }

    val isFacingQibla: Boolean
        get() = kotlin.math.abs(relativeQiblaAngle) <= 3f // within 3 degrees tolerance
}

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassState = MutableStateFlow(
        CompassState(
            isSensorAvailable = rotationSensor != null || (accelerometer != null && magnetometer != null),
            sensorName = rotationSensor?.name ?: (magnetometer?.name ?: "Sensor Tidak Ditemukan")
        )
    )
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var lastAccelerometer = FloatArray(3)
    private var lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    fun startListening() {
        if (sensorManager == null) return

        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun updateCoordinates(latitude: Double, longitude: Double) {
        val calculatedBearing = calculateQiblaBearing(latitude, longitude)
        _compassState.value = _compassState.value.copy(qiblaBearing = calculatedBearing)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()) + 360) % 360).toFloat()
            _compassState.value = _compassState.value.copy(
                headingDegrees = azimuthDeg,
                isSensorAvailable = true
            )
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet && rotationSensor == null) {
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetometer
            )
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                val azimuthDeg = ((Math.toDegrees(azimuthRad.toDouble()) + 360) % 360).toFloat()
                _compassState.value = _compassState.value.copy(
                    headingDegrees = azimuthDeg,
                    isSensorAvailable = true
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _compassState.value = _compassState.value.copy(accuracy = accuracy)
    }

    companion object {
        // Kaaba Location Coordinates (Makkah al-Mukarramah)
        private const val KAABA_LAT = 21.422487
        private const val KAABA_LNG = 39.826206

        /**
         * Computes accurate Qibla bearing from user latitude and longitude
         * using the Great Circle Forward Azimuth Spherical Trigonometry formula.
         */
        fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
            val userLatRad = Math.toRadians(userLat)
            val userLngRad = Math.toRadians(userLng)
            val kaabaLatRad = Math.toRadians(KAABA_LAT)
            val kaabaLngRad = Math.toRadians(KAABA_LNG)

            val deltaLng = kaabaLngRad - userLngRad
            val y = sin(deltaLng)
            val x = cos(userLatRad) * tan(kaabaLatRad) - sin(userLatRad) * cos(deltaLng)

            val bearingRad = atan2(y, x)
            val bearingDeg = ((Math.toDegrees(bearingRad) + 360) % 360).toFloat()
            return bearingDeg
        }
    }
}
