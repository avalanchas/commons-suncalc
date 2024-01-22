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

/**
 * Calculations and constants for the Sun.
 *
 * @see "Astronomy on the Personal Computer, 4th edition
 *
 */
object Sun {
    private const val SUN_DISTANCE = 149598000.0
    private const val SUN_MEAN_RADIUS = 695700.0

    /**
     * Calculates the equatorial position of the sun.
     *
     * @param date
     * [JulianDate] to be used
     * @return [Vector] containing the sun position
     */
    fun positionEquatorial(date: JulianDate): Vector {
        val T = date.julianCentury
        val M = ExtendedMath.PI2 * ExtendedMath.frac(0.993133 + 99.997361 * T)
        val L = ExtendedMath.PI2 * ExtendedMath.frac(
            0.7859453 + M / ExtendedMath.PI2 + (6893.0 * Math.sin(M) + 72.0 * Math.sin(2.0 * M) + 6191.2 * T) / 1296.0e3
        )
        val d = (SUN_DISTANCE
                * (1 - 0.016718 * Math.cos(date.trueAnomaly)))
        return Vector.Companion.ofPolar(L, 0.0, d)
    }

    /**
     * Calculates the geocentric position of the sun.
     *
     * @param date
     * [JulianDate] to be used
     * @return [Vector] containing the sun position
     */
    fun position(date: JulianDate): Vector {
        val rotateMatrix = ExtendedMath.equatorialToEcliptical(date).transpose()
        return rotateMatrix.multiply(positionEquatorial(date))
    }

    /**
     * Calculates the horizontal position of the sun.
     *
     * @param date
     * [JulianDate] to be used
     * @param lat
     * Latitude, in radians
     * @param lng
     * Longitute, in radians
     * @return [Vector] of horizontal sun position
     */
    fun positionHorizontal(date: JulianDate, lat: Double, lng: Double): Vector {
        val mc = position(date)
        val h = date.greenwichMeanSiderealTime + lng - mc.phi
        return ExtendedMath.equatorialToHorizontal(h, mc.theta, mc.r, lat)
    }

    /**
     * Returns the angular radius of the sun.
     *
     * @param distance
     * Distance of the sun, in kilometers.
     * @return Angular radius of the sun, in radians.
     * @see [Wikipedia: Angular
     * Diameter](https://en.wikipedia.org/wiki/Angular_diameter)
     */
    fun angularRadius(distance: Double): Double {
        return Math.asin(SUN_MEAN_RADIUS / distance)
    }
}
