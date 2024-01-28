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
package com.kurviger.suncalc

import com.kurviger.suncalc.param.Builder
import com.kurviger.suncalc.param.LocationParameter
import com.kurviger.suncalc.param.TimeParameter
import com.kurviger.suncalc.util.BaseBuilder
import com.kurviger.suncalc.util.ExtendedMath.equatorialToHorizontal
import com.kurviger.suncalc.util.ExtendedMath.refraction
import com.kurviger.suncalc.util.JulianDate
import com.kurviger.suncalc.util.Sun
import com.kurviger.suncalc.util.Vector
import com.kurviger.suncalc.util.toDegrees
import com.kurviger.suncalc.util.toRadians


/**
 * Calculates the position of the sun.
 */
class SunPosition private constructor(
    azimuth: Double, altitude: Double, trueAltitude: Double,
    /**
     * Sun's distance, in kilometers.
     */
    val distance: Double
) {
    /**
     * Sun azimuth, in degrees, north-based.
     *
     *
     * This is the direction along the horizon, measured from north to east. For example,
     * `0.0` means north, `135.0` means southeast, `270.0` means west.
     */
    val azimuth: Double

    /**
     * The visible sun altitude above the horizon, in degrees.
     *
     *
     * `0.0` means the sun's center is at the horizon, `90.0` at the zenith
     * (straight over your head). Atmospheric refraction is taken into account.
     *
     * @see .getTrueAltitude
     */
    val altitude: Double

    /**
     * The true sun altitude above the horizon, in degrees.
     *
     *
     * `0.0` means the sun's center is at the horizon, `90.0` at the zenith
     * (straight over your head).
     *
     * @see .getAltitude
     */
    val trueAltitude: Double

    init {
        this.azimuth = (toDegrees(azimuth) + 180.0) % 360.0
        this.altitude = toDegrees(altitude)
        this.trueAltitude = toDegrees(trueAltitude)
    }

    /**
     * Collects all parameters for [SunPosition].
     */
    interface Parameters : LocationParameter<Parameters?>,
        TimeParameter<Parameters?>, Builder<SunPosition?>

    /**
     * Builder for [SunPosition]. Performs the computations based on the parameters,
     * and creates a [SunPosition] object that holds the result.
     */
    private class SunPositionBuilder : BaseBuilder<Parameters?>(), Parameters {
        override fun execute(): SunPosition {
            val t: JulianDate = julianDate
            val lw: Double = toRadians(-longitude)
            val phi: Double = toRadians(latitude)
            val c: Vector = Sun.position(t)
            val h: Double = t.greenwichMeanSiderealTime - lw - c.phi
            val horizontal: Vector = equatorialToHorizontal(h, c.theta, c.r, phi)
            val hRef: Double = refraction(horizontal.theta)
            return SunPosition(
                horizontal.phi,
                horizontal.theta + hRef,
                horizontal.theta,
                horizontal.r
            )
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("SunPosition[azimuth=").append(azimuth)
        sb.append("°, altitude=").append(altitude)
        sb.append("°, true altitude=").append(trueAltitude)
        sb.append("°, distance=").append(distance).append(" km]")
        return sb.toString()
    }

    companion object {
        /**
         * Starts the computation of [SunPosition].
         *
         * @return [Parameters] to set.
         */
        fun compute(): Parameters {
            return SunPositionBuilder()
        }
    }
}
