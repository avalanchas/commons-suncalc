/*
 * Shredzone Commons - suncalc
 *
 * Copyright (C) 2017 Richard "Shred" Körber
 *   http://commons.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.commons.suncalc.util

import org.shredzone.commons.suncalc.util.JulianDate
import java.time.Instant
import java.time.ZonedDateTime
import kotlin.math.floor
import kotlin.math.roundToLong

/**
 * This class contains a Julian Date representation of a date.
 *
 *
 * Objects are immutable and threadsafe.
 */
class JulianDate(val dateTime: ZonedDateTime) {

    /**
     * Returns the Modified Julian Date.
     *
     * @return Modified Julian Date, UTC.
     */
    val modifiedJulianDate: Double

    /**
     * Creates a new [JulianDate].
     *
     * @param time
     * [ZonedDateTime] to use for the date.
     */
    init {
        modifiedJulianDate = dateTime.toInstant().toEpochMilli() / 86400000.0 + 40587.0
    }

    /**
     * Returns a [JulianDate] of the current date and the given hour.
     *
     * @param hour
     * Hour of this date. This is a floating point value. Fractions are used
     * for minutes and seconds.
     * @return [JulianDate] instance.
     */
    fun atHour(hour: Double): JulianDate {
        return JulianDate(dateTime!!.plusSeconds((hour * 60.0 * 60.0).roundToLong()))
    }

    /**
     * Returns a [JulianDate] of the given modified Julian date.
     *
     * @param mjd
     * Modified Julian Date
     * @return [JulianDate] instance.
     */
    fun atModifiedJulianDate(mjd: Double): JulianDate {
        val mjdi = Instant.ofEpochMilli(((mjd - 40587.0) * 86400000.0).roundToLong())
        return JulianDate(ZonedDateTime.ofInstant(mjdi, dateTime!!.zone))
    }

    /**
     * Returns a [JulianDate] of the given Julian century.
     *
     * @param jc
     * Julian Century
     * @return [JulianDate] instance.
     */
    fun atJulianCentury(jc: Double): JulianDate {
        return atModifiedJulianDate(jc * 36525.0 + 51544.5)
    }

    val julianCentury: Double
        /**
         * Returns the Julian Centuries.
         *
         * @return Julian Centuries, based on J2000 epoch, UTC.
         */
        get() = (modifiedJulianDate - 51544.5) / 36525.0
    val greenwichMeanSiderealTime: Double
        /**
         * Returns the Greenwich Mean Sidereal Time of this Julian Date.
         *
         * @return GMST
         */
        get() {
            val secs = 86400.0
            val mjd0 = floor(modifiedJulianDate)
            val ut = (modifiedJulianDate - mjd0) * secs
            val t0 = (mjd0 - 51544.5) / 36525.0
            val t = (modifiedJulianDate - 51544.5) / 36525.0
            val gmst =
                24110.54841 + 8640184.812866 * t0 + 1.0027379093 * ut + (0.093104 - 6.2e-6 * t) * t * t
            return ExtendedMath.PI2 / secs * (gmst % secs)
        }
    val trueAnomaly: Double
        /**
         * Returns the earth's true anomaly of the current date.
         *
         *
         * A simple approximation is used here.
         *
         * @return True anomaly, in radians
         */
        get() = ExtendedMath.PI2 * ExtendedMath.frac((dateTime!!.dayOfYear - 5.0) / 365.256363)

    override fun toString(): String {
        return String.format(
            "%dd %02dh %02dm %02ds",
            modifiedJulianDate.toLong(),
            (modifiedJulianDate * 24 % 24).toLong(),
            (modifiedJulianDate * 24 * 60 % 60).toLong(),
            (modifiedJulianDate * 24 * 60 * 60 % 60).toLong()
        )
    }
}
