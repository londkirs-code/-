package com.example.calculator

import com.example.model.LocationData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

data class DayZmanim(
    val date: LocalDate,
    val location: LocationData,
    val alotHaShachar16Degrees: ZonedDateTime,
    val alotHaShachar72Min: ZonedDateTime,
    val misheyakir: ZonedDateTime,
    val netzPlain: ZonedDateTime,
    val netzVisible: ZonedDateTime,
    val sofZmanShemaMGA: ZonedDateTime,
    val sofZmanShemaGra: ZonedDateTime,
    val sofZmanTefillahMGA: ZonedDateTime,
    val sofZmanTefillahGra: ZonedDateTime,
    val chatzot: ZonedDateTime,
    val minchaGedola: ZonedDateTime,
    val plagHaMincha: ZonedDateTime,
    val shkia: ZonedDateTime,
    val tzeitHaKochavim: ZonedDateTime,
    val shaahZmanitGraMs: Long,
    val shaahZmanitMGAMs: Long
)

object AstronomicalCalculator {

    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI

    // Standard solar zenith for sunrise/sunset (90° + 50' refraction & semi-diameter = 90.833333°)
    private const val ZENITH_SUNRISE_SEA_LEVEL = 90.8333333

    /**
     * Calculates the Dip of the horizon in degrees due to observer elevation in meters.
     * Formula: dip = 0.0347 * sqrt(elevation)
     */
    fun calculateDipAngleDegrees(elevationMeters: Double): Double {
        return if (elevationMeters > 0) 0.0347 * sqrt(elevationMeters) else 0.0
    }

    /**
     * Calculates sun position (altitude in degrees) for a given instant.
     */
    fun getSolarAltitude(zonedDateTime: ZonedDateTime, latitude: Double, longitude: Double): Double {
        val utc = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))
        val jd = julianDay(utc.year, utc.monthValue, utc.dayOfMonth, utc.hour, utc.minute, utc.second)
        val t = (jd - 2451545.0) / 36525.0

        val l0 = (280.46646 + t * (36000.76983 + t * 0.0003032)) % 360.0
        val m = (357.52911 + t * (35999.05029 - 0.0001537 * t)) % 360.0
        val mRad = m * DEG_TO_RAD

        val c = (1.914602 - t * (0.004817 + 0.000014 * t)) * sin(mRad) +
                (0.019993 - 0.000101 * t) * sin(2.0 * mRad) +
                0.000289 * sin(3.0 * mRad)

        val sunTrueLong = l0 + c
        val omega = 125.04 - 1934.136 * t
        val lambda = (sunTrueLong - 0.00569 - 0.00478 * sin(omega * DEG_TO_RAD)) * DEG_TO_RAD

        val eps0 = 23.0 + (26.0 + ((21.448 - t * (46.815 + t * (0.00059 - t * 0.001813)))) / 60.0) / 60.0
        val eps = (eps0 + 0.00256 * cos(omega * DEG_TO_RAD)) * DEG_TO_RAD

        val declination = asin(sin(eps) * sin(lambda))
        val rightAscension = atan2(cos(eps) * sin(lambda), cos(lambda))

        // Greenwich Mean Sidereal Time
        val gmst0 = (280.46061837 + 360.98564736629 * (jd - 2451545.0)) % 360.0
        val localSiderealTime = (gmst0 + longitude) * DEG_TO_RAD
        val hourAngle = localSiderealTime - rightAscension

        val latRad = latitude * DEG_TO_RAD
        val sinAlt = sin(latRad) * sin(declination) + cos(latRad) * cos(declination) * cos(hourAngle)
        return asin(sinAlt.coerceIn(-1.0, 1.0)) * RAD_TO_DEG
    }

    private fun julianDay(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        val dayFraction = (hour + (minute + second / 60.0) / 60.0) / 24.0
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + dayFraction + b - 1524.5
    }

    /**
     * Compute UTC time in fraction of day [0, 1) for a specific solar zenith.
     * @param isSunrise true for sunrise/morning, false for sunset/evening
     */
    private fun calculateSunTimeUTC(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zenithDegrees: Double,
        isSunrise: Boolean
    ): Double? {
        val jd = julianDay(date.year, date.monthValue, date.dayOfMonth, 12, 0, 0)
        val t = (jd - 2451545.0) / 36525.0

        val l0 = (280.46646 + t * (36000.76983 + t * 0.0003032)) % 360.0
        val m = (357.52911 + t * (35999.05029 - 0.0001537 * t)) % 360.0
        val mRad = m * DEG_TO_RAD

        val c = (1.914602 - t * (0.004817 + 0.000014 * t)) * sin(mRad) +
                (0.019993 - 0.000101 * t) * sin(2.0 * mRad) +
                0.000289 * sin(3.0 * mRad)

        val sunTrueLong = l0 + c
        val omega = 125.04 - 1934.136 * t
        val lambda = (sunTrueLong - 0.00569 - 0.00478 * sin(omega * DEG_TO_RAD)) * DEG_TO_RAD

        val eps0 = 23.0 + (26.0 + ((21.448 - t * (46.815 + t * (0.00059 - t * 0.001813)))) / 60.0) / 60.0
        val eps = (eps0 + 0.00256 * cos(omega * DEG_TO_RAD)) * DEG_TO_RAD

        val declination = asin(sin(eps) * sin(lambda))
        val e = 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
        val y = tan(eps / 2.0).pow(2)

        // Equation of Time in minutes
        val l0Rad = l0 * DEG_TO_RAD
        val eqTimeMinutes = 4.0 * RAD_TO_DEG * (
                y * sin(2.0 * l0Rad) -
                2.0 * e * sin(mRad) +
                4.0 * e * y * sin(mRad) * cos(2.0 * l0Rad) -
                0.5 * y * y * sin(4.0 * l0Rad) -
                1.25 * e * e * sin(2.0 * mRad)
        )

        // Solar noon UTC in minutes
        val solarNoonMinutes = 720.0 - (4.0 * longitude) - eqTimeMinutes

        // Hour angle
        val latRad = latitude * DEG_TO_RAD
        val zenithRad = zenithDegrees * DEG_TO_RAD
        val cosHA = (cos(zenithRad) - sin(latRad) * sin(declination)) / (cos(latRad) * cos(declination))

        if (cosHA < -1.0 || cosHA > 1.0) {
            return null // Sun does not reach this zenith on this date (polar)
        }

        val haDegrees = acos(cosHA) * RAD_TO_DEG
        val deltaMinutes = haDegrees * 4.0

        val targetMinutes = if (isSunrise) {
            solarNoonMinutes - deltaMinutes
        } else {
            solarNoonMinutes + deltaMinutes
        }

        return targetMinutes / 1440.0
    }

    private fun fractionOfDayToZonedDateTime(
        fraction: Double,
        date: LocalDate,
        zoneId: ZoneId
    ): ZonedDateTime {
        var minutes = (fraction * 1440.0)
        while (minutes < 0) minutes += 1440.0
        while (minutes >= 1440.0) minutes -= 1440.0

        val hour = (minutes / 60.0).toInt().coerceIn(0, 23)
        val min = (minutes % 60.0).toInt().coerceIn(0, 59)
        val sec = (((minutes % 60.0) - min) * 60.0).toInt().coerceIn(0, 59)
        val nanos = (((((minutes % 60.0) - min) * 60.0) - sec) * 1_000_000_000).toInt().coerceIn(0, 999_999_999)

        val utcDateTime = LocalDateTime.of(date, LocalTime.of(hour, min, sec, nanos))
        val utcZoned = ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))
        return utcZoned.withZoneSameInstant(zoneId)
    }

    fun calculateZmanim(
        date: LocalDate,
        location: LocationData,
        customElevationDip: Boolean = true,
        useLocalOffsetForVisible: Boolean = true
    ): DayZmanim {
        val zoneId = ZoneId.of(location.timeZoneId)

        // 1. Plain astronomical netz (zenith 90.8333°)
        val plainNetzFrac = calculateSunTimeUTC(date, location.latitude, location.longitude, ZENITH_SUNRISE_SEA_LEVEL, true) ?: 0.25
        val netzPlain = fractionOfDayToZonedDateTime(plainNetzFrac, date, zoneId)

        // 2. Visible Netz (נץ נראה):
        // Combines elevation dip (which would advance sunrise) with local horizon mountains/hills offset
        val elevationDip = if (customElevationDip) calculateDipAngleDegrees(location.elevationMeters) else 0.0
        val elevationNetzFrac = calculateSunTimeUTC(date, location.latitude, location.longitude, ZENITH_SUNRISE_SEA_LEVEL + elevationDip, true) ?: plainNetzFrac
        val netzWithElevation = fractionOfDayToZonedDateTime(elevationNetzFrac, date, zoneId)

        val netzVisible = if (useLocalOffsetForVisible && location.visibleNetzOffsetMinutes != 0) {
            netzPlain.plusMinutes(location.visibleNetzOffsetMinutes.toLong())
        } else {
            netzWithElevation
        }

        // 3. Sunset (Shkia)
        val shkiaFrac = calculateSunTimeUTC(date, location.latitude, location.longitude, ZENITH_SUNRISE_SEA_LEVEL, false) ?: 0.75
        val shkia = fractionOfDayToZonedDateTime(shkiaFrac, date, zoneId)

        // 4. Alot HaShachar (16.1 degrees below horizon = zenith 106.1°)
        val alot16Frac = calculateSunTimeUTC(date, location.latitude, location.longitude, 106.1, true)
            ?: (plainNetzFrac - 72.0 / 1440.0)
        val alotHaShachar16 = fractionOfDayToZonedDateTime(alot16Frac, date, zoneId)
        val alotHaShachar72Min = netzPlain.minusMinutes(72)

        // 5. Misheyakir (Talit & Tefillin ~ 10.2 degrees below horizon = zenith 100.2°)
        val misheyakirFrac = calculateSunTimeUTC(date, location.latitude, location.longitude, 100.2, true)
            ?: (plainNetzFrac - 52.0 / 1440.0)
        val misheyakir = fractionOfDayToZonedDateTime(misheyakirFrac, date, zoneId)

        // 6. Chatzot (Solar Noon)
        val chatzotFrac = calculateSunTimeUTC(date, location.latitude, location.longitude, 90.0, true)
        val noonUtcMinutes = 720.0 - (4.0 * location.longitude)
        val chatzot = fractionOfDayToZonedDateTime(noonUtcMinutes / 1440.0, date, zoneId)

        // 7. Shaot Zmaniyot (Halachic Hours)
        // Gra: Sunrise to Sunset divided by 12
        val graDayMs = java.time.Duration.between(netzPlain, shkia).toMillis()
        val shaahGraMs = graDayMs / 12

        // MGA: Alot HaShachar (16.1°) to Tzeit HaKochavim (8.5°) divided by 12
        val tzeitFrac = calculateSunTimeUTC(date, location.latitude, location.longitude, 98.5, false)
            ?: (shkiaFrac + 25.0 / 1440.0)
        val tzeit = fractionOfDayToZonedDateTime(tzeitFrac, date, zoneId)
        val mgaDayMs = java.time.Duration.between(alotHaShachar16, tzeit).toMillis()
        val shaahMGAMs = mgaDayMs / 12

        // Sof Zman Shema
        val sofShemaGra = netzPlain.plus(java.time.Duration.ofMillis(shaahGraMs * 3))
        val sofShemaMGA = alotHaShachar16.plus(java.time.Duration.ofMillis(shaahMGAMs * 3))

        // Sof Zman Tefillah
        val sofTefillahGra = netzPlain.plus(java.time.Duration.ofMillis(shaahGraMs * 4))
        val sofTefillahMGA = alotHaShachar16.plus(java.time.Duration.ofMillis(shaahMGAMs * 4))

        // Mincha Gedola: Chatzot + 0.5 shaah zmanit
        val minchaGedola = chatzot.plus(java.time.Duration.ofMillis(shaahGraMs / 2))

        // Plag HaMincha: Shkia - 1.25 shaot zmaniyot (10.75 shaot from sunrise)
        val plag = shkia.minus(java.time.Duration.ofMillis((shaahGraMs * 1.25).toLong()))

        return DayZmanim(
            date = date,
            location = location,
            alotHaShachar16Degrees = alotHaShachar16,
            alotHaShachar72Min = alotHaShachar72Min,
            misheyakir = misheyakir,
            netzPlain = netzPlain,
            netzVisible = netzVisible,
            sofZmanShemaMGA = sofShemaMGA,
            sofZmanShemaGra = sofShemaGra,
            sofZmanTefillahMGA = sofTefillahMGA,
            sofZmanTefillahGra = sofTefillahGra,
            chatzot = chatzot,
            minchaGedola = minchaGedola,
            plagHaMincha = plag,
            shkia = shkia,
            tzeitHaKochavim = tzeit,
            shaahZmanitGraMs = shaahGraMs,
            shaahZmanitMGAMs = shaahMGAMs
        )
    }
}
