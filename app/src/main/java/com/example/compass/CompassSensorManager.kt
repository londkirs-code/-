package com.example.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

data class CompassState(
    val deviceAzimuthDegrees: Float = 0f, // 0..360, 0 = North
    val bearingToJerusalemDegrees: Float = 0f, // Direction to Jerusalem from current location
    val relativeAngleToJerusalemDegrees: Float = 0f, // Angle relative to device heading (-180..+180, 0 = facing Jerusalem)
    val isFacingJerusalem: Boolean = false, // within +- 5 degrees
    val distanceToJerusalemKm: Int = 0,
    val hasCompassSensor: Boolean = true
)

class CompassSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    // Temple Mount / Western Wall coordinates in Jerusalem
    private val jerusalemLat = 31.7780
    private val jerusalemLon = 35.2354

    private var currentLat: Double = 31.7683
    private var currentLon: Double = 35.2137

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var hasAccel = false
    private var hasMag = false

    private var smoothedAzimuth = 0f

    init {
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
    }

    fun updateCoordinates(latitude: Double, longitude: Double) {
        currentLat = latitude
        currentLon = longitude
        recalculateCompass()
    }

    fun startListening() {
        val hasSensor = rotationVectorSensor != null || (accelerometerSensor != null && magnetometerSensor != null)
        _compassState.value = _compassState.value.copy(hasCompassSensor = hasSensor)

        if (rotationVectorSensor != null) {
            sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometerSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var azimuthDeg = smoothedAzimuth

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            hasAccel = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            hasMag = true
        }

        if (rotationVectorSensor == null && hasAccel && hasMag) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
            }
        }

        // Apply low-pass exponential smoothing to prevent needle jitter
        val diff = ((azimuthDeg - smoothedAzimuth + 540) % 360) - 180
        smoothedAzimuth = (smoothedAzimuth + diff * 0.15f + 360) % 360

        recalculateCompass()
    }

    private fun recalculateCompass() {
        // Calculate Great Circle bearing to Jerusalem
        val phi1 = Math.toRadians(currentLat)
        val phi2 = Math.toRadians(jerusalemLat)
        val deltaLambda = Math.toRadians(jerusalemLon - currentLon)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val bearing = (Math.toDegrees(atan2(y, x)) + 360) % 360

        // Calculate Haversine distance
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(jerusalemLat - currentLat)
        val dLon = Math.toRadians(jerusalemLon - currentLon)
        val a = sin(dLat / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distKm = (r * c).roundToInt()

        // Angle relative to device heading: 0 means device is pointing directly towards Jerusalem
        val relativeAngle = ((bearing.toFloat() - smoothedAzimuth + 540) % 360) - 180
        val isFacing = abs(relativeAngle) <= 5.0f

        _compassState.value = CompassState(
            deviceAzimuthDegrees = smoothedAzimuth,
            bearingToJerusalemDegrees = bearing.toFloat(),
            relativeAngleToJerusalemDegrees = relativeAngle,
            isFacingJerusalem = isFacing,
            distanceToJerusalemKm = distKm,
            hasCompassSensor = rotationVectorSensor != null || (accelerometerSensor != null && magnetometerSensor != null)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
