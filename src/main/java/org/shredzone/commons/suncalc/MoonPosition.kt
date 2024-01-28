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
package org.shredzone.commons.suncalc

import com.kurviger.suncalc.param.Builder
import com.kurviger.suncalc.param.LocationParameter
import com.kurviger.suncalc.param.TimeParameter
import com.kurviger.suncalc.util.BaseBuilder
import com.kurviger.suncalc.util.ExtendedMath.equatorialToHorizontal
import com.kurviger.suncalc.util.ExtendedMath.refraction
import com.kurviger.suncalc.util.JulianDate
import com.kurviger.suncalc.util.Moon
import com.kurviger.suncalc.util.Vector
import com.kurviger.suncalc.util.toDegrees
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan


/**
 * Calculates the position of the moon.
 */
class MoonPosition private constructor(
    azimuth: Double, altitude: Double, trueAltitude: Double,
    /**
     * Distance to the moon in kilometers.
     */
    val distance: Double, parallacticAngle: Double
) {
    /**
     * Moon azimuth, in degrees, north-based.
     *
     *
     * This is the direction along the horizon, measured from north to east. For example,
     * `0.0` means north, `135.0` means southeast, `270.0` means west.
     */
    val azimuth: Double

    /**
     * Moon altitude above the horizon, in degrees.
     *
     *
     * `0.0` means the moon's center is at the horizon, `90.0` at the zenith
     * (straight over your head). Atmospheric refraction is taken into account.
     *
     * @see .getTrueAltitude
     */
    val altitude: Double

    /**
     * The true moon altitude above the horizon, in degrees.
     *
     *
     * `0.0` means the moon's center is at the horizon, `90.0` at the zenith
     * (straight over your head).
     *
     * @see .getAltitude
     * @since 3.8
     */
    val trueAltitude: Double

    /**
     * Parallactic angle of the moon, in degrees.
     */
    val parallacticAngle: Double

    init {
        this.azimuth = (toDegrees(azimuth) + 180.0) % 360.0
        this.altitude = toDegrees(altitude)
        this.trueAltitude = toDegrees(trueAltitude)
        this.parallacticAngle = toDegrees(parallacticAngle)
    }

    /**
     * Collects all parameters for [MoonPosition].
     */
    interface Parameters : LocationParameter<Parameters?>, TimeParameter<Parameters?>,
        Builder<MoonPosition?>

    /**
     * Builder for [MoonPosition]. Performs the computations based on the
     * parameters, and creates a [MoonPosition] object that holds the result.
     */
    private class MoonPositionBuilder : BaseBuilder<Parameters?>(), Parameters {
        override fun execute(): MoonPosition {
            val t: JulianDate = julianDate
            val phi: Double = latitudeRad
            val lambda: Double = longitudeRad
            val mc: Vector = Moon.position(t)
            val h: Double = t.greenwichMeanSiderealTime + lambda - mc.phi
            val horizontal: Vector = equatorialToHorizontal(h, mc.theta, mc.r, phi)
            val hRef: Double = refraction(horizontal.theta)
            val pa: Double = atan2(
                sin(h),
                tan(phi) * cos(mc.theta) - sin(mc.theta) * cos(
                    h
                )
            )
            return MoonPosition(
                horizontal.phi,
                horizontal.theta + hRef,
                horizontal.theta,
                mc.r,
                pa
            )
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("MoonPosition[azimuth=").append(azimuth)
        sb.append("°, altitude=").append(altitude)
        sb.append("°, true altitude=").append(trueAltitude)
        sb.append("°, distance=").append(distance)
        sb.append(" km, parallacticAngle=").append(parallacticAngle)
        sb.append("°]")
        return sb.toString()
    }

    companion object {
        /**
         * Starts the computation of [MoonPosition].
         *
         * @return [Parameters] to set.
         */
        fun compute(): Parameters {
            return MoonPositionBuilder()
        }
    }
}
