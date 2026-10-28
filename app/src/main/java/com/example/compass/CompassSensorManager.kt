package com.example.compass

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

data class CompassState(
    val deviceAzimuthDegrees: Float = 0f, // 0..360, 0 = True North
    val bearingToJerusalemDegrees: Float = 0f, // Direction to Jerusalem from current location
    val relativeAngleToJerusalemDegrees: Float = 0f, // Angle relative to device heading (-180..+180, 0 = facing Jerusalem)
    val isFacingJerusalem: Boolean = false, // within +- 6 degrees
    val distanceToJerusalemKm: Int = 0,
    val hasCompassSensor: Boolean = true,
    val isGpsLive: Boolean = false,
    val gpsAccuracyMeters: Float = 0f,
    val currentLat: Double = 31.7683,
    val currentLon: Double = 35.2137,
    val headingSource: String = "חיישן מגנטי"
)

class CompassSensorManager(private val context: Context) : SensorEventListener, LocationListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    // Temple Mount / Western Wall coordinates in Jerusalem
    private val jerusalemLat = 31.7780
    private val jerusalemLon = 35.2354

    private var currentLat: Double = 31.7683
    private var currentLon: Double = 35.2137
    private var currentAltitude: Double = 750.0

    private var rotationVectorSensor: Sensor? = null
    private var geomagneticVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null
    private var orientationSensor: Sensor? = null

    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var hasAccel = false
    private var hasMag = false

    private var smoothedAzimuth = 0f
    private var magneticDeclination = 0f
    private var isGpsActive = false
    private var lastGpsAccuracy = 0f

    init {
        rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        geomagneticVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        @Suppress("DEPRECATION")
        orientationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        updateDeclination()
    }

    private fun updateDeclination() {
        try {
            val geoField = GeomagneticField(
                currentLat.toFloat(),
                currentLon.toFloat(),
                currentAltitude.toFloat(),
                System.currentTimeMillis()
            )
            magneticDeclination = geoField.declination
        } catch (_: Exception) {
            magneticDeclination = 4.5f // Default declination for Israel region
        }
    }

    fun updateCoordinates(latitude: Double, longitude: Double) {
        currentLat = latitude
        currentLon = longitude
        updateDeclination()
        recalculateCompass("מיקום מוגדר")
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        val hasSensor = rotationVectorSensor != null ||
                geomagneticVectorSensor != null ||
                (accelerometerSensor != null && magnetometerSensor != null) ||
                orientationSensor != null

        _compassState.value = _compassState.value.copy(hasCompassSensor = hasSensor)

        // 1. Register rotation/compass sensors (FASTEST / UI delay for live movement)
        when {
            rotationVectorSensor != null -> {
                sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
            }
            geomagneticVectorSensor != null -> {
                sensorManager?.registerListener(this, geomagneticVectorSensor, SensorManager.SENSOR_DELAY_UI)
            }
            accelerometerSensor != null && magnetometerSensor != null -> {
                sensorManager?.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
                sensorManager?.registerListener(this, magnetometerSensor, SensorManager.SENSOR_DELAY_UI)
            }
            orientationSensor != null -> {
                sensorManager?.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        // 2. Start Live GPS listener for real user location and bearing
        startGpsListening()
    }

    @SuppressLint("MissingPermission")
    fun startGpsListening() {
        val hasFine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                // Check last known location first for immediate orientation
                val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val best = lastGps ?: lastNet
                if (best != null) {
                    onLocationChanged(best)
                }

                // Register real-time GPS updates (interval 800ms)
                if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        800L,
                        0.5f,
                        this
                    )
                    isGpsActive = true
                }
                if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1500L,
                        1.0f,
                        this
                    )
                }
            } catch (_: Exception) {
                isGpsActive = false
            }
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {}
        isGpsActive = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var azimuthDeg = smoothedAzimuth
        var detected = false
        var sourceName = "חיישן מגנטי"

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                // Remap coordinates so the compass works whether phone is flat or held upright in hand
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedMatrix
                )
                SensorManager.getOrientation(remappedMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
                detected = true
                sourceName = "חיישן תנועה (3D)"
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
                hasAccel = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
                hasMag = true
            }
            @Suppress("DEPRECATION")
            Sensor.TYPE_ORIENTATION -> {
                azimuthDeg = (event.values[0] + 360f) % 360f
                detected = true
                sourceName = "מצפן פנימי"
            }
        }

        if (!detected && hasAccel && hasMag) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedMatrix
                )
                SensorManager.getOrientation(remappedMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
                detected = true
                sourceName = "חיישן מגנטי + תאוצה"
            }
        }

        if (detected) {
            // Adjust magnetic heading with local declination to yield True North
            azimuthDeg = (azimuthDeg + magneticDeclination + 360f) % 360f

            // Smooth needle animation with responsive filter (0.3f for live feel)
            val diff = ((azimuthDeg - smoothedAzimuth + 540) % 360) - 180
            smoothedAzimuth = (smoothedAzimuth + diff * 0.30f + 360) % 360

            recalculateCompass(sourceName)
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLat = location.latitude
        currentLon = location.longitude
        currentAltitude = location.altitude
        lastGpsAccuracy = location.accuracy
        isGpsActive = true
        updateDeclination()

        // If user is walking/moving and GPS provides accurate bearing, use live GPS bearing
        if (location.hasBearing() && location.hasSpeed() && location.speed > 0.4f) {
            val diff = ((location.bearing - smoothedAzimuth + 540) % 360) - 180
            smoothedAzimuth = (smoothedAzimuth + diff * 0.40f + 360) % 360
            recalculateCompass("GPS בתנועה")
        } else {
            recalculateCompass("GPS בלייב")
        }
    }

    private fun recalculateCompass(source: String) {
        // Calculate Great Circle bearing to Jerusalem (Temple Mount)
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
        val isFacing = abs(relativeAngle) <= 6.0f

        _compassState.value = CompassState(
            deviceAzimuthDegrees = smoothedAzimuth,
            bearingToJerusalemDegrees = bearing.toFloat(),
            relativeAngleToJerusalemDegrees = relativeAngle,
            isFacingJerusalem = isFacing,
            distanceToJerusalemKm = distKm,
            hasCompassSensor = rotationVectorSensor != null || geomagneticVectorSensor != null || (accelerometerSensor != null && magnetometerSensor != null) || orientationSensor != null,
            isGpsLive = isGpsActive,
            gpsAccuracyMeters = lastGpsAccuracy,
            currentLat = currentLat,
            currentLon = currentLon,
            headingSource = source
        )
    }

    /**
     * Manual heading rotation for testing or manual alignment
     */
    fun rotateHeadingManually(degreesDelta: Float) {
        smoothedAzimuth = (smoothedAzimuth + degreesDelta + 360f) % 360f
        recalculateCompass("כיוונון ידני")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {
        isGpsActive = true
    }
    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) {
            isGpsActive = false
        }
    }
}
