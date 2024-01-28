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
package com.kurviger.suncalc.util

import com.kurviger.suncalc.util.ExtendedMath.ARCS
import com.kurviger.suncalc.util.ExtendedMath.TAU
import com.kurviger.suncalc.util.ExtendedMath.equatorialToEcliptical
import com.kurviger.suncalc.util.ExtendedMath.equatorialToHorizontal
import com.kurviger.suncalc.util.ExtendedMath.frac
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin


/**
 * Calculations and constants for the Moon.
 *
 * @see "Astronomy on the Personal Computer, 4th edition
 *
 */
object Moon {
    private const val MOON_MEAN_RADIUS = 1737.1

    /**
     * Calculates the equatorial position of the moon.
     *
     * @param date
     * [JulianDate] to be used
     * @return [Vector] of equatorial moon position
     */
    fun positionEquatorial(date: JulianDate): Vector {
        val T: Double = date.julianCentury
        val L0: Double = frac(0.606433 + 1336.855225 * T)
        val l: Double = TAU * frac(0.374897 + 1325.552410 * T)
        val ls: Double = TAU * frac(0.993133 + 99.997361 * T)
        val D: Double = TAU * frac(0.827361 + 1236.853086 * T)
        val F: Double = TAU * frac(0.259086 + 1342.227825 * T)
        val D2 = 2.0 * D
        val l2 = 2.0 * l
        val F2 = 2.0 * F
        val dL: Double = (((22640.0 * sin(l)
                - 4586.0 * sin(l - D2)
                ) + 2370.0 * sin(D2) + 769.0 * sin(l2) - 668.0 * sin(
            ls
        ) - 412.0 * sin(F2) - 212.0 * sin(l2 - D2) - 206.0 * sin(
            l + ls - D2
        )
                + 192.0 * sin(l + D2)
                ) - 165.0 * sin(ls - D2) - 125.0 * sin(D) - 110.0 * sin(
            l + ls
        )
                + 148.0 * sin(l - ls)
                - 55.0 * sin(F2 - D2))
        val S: Double =
            F + (dL + 412.0 * sin(F2) + 541.0 * sin(ls)) / ARCS
        val h = F - D2
        val N: Double = ((-526.0 * sin(h)
                + 44.0 * sin(l + h)
                ) - 31.0 * sin(-l + h) - 23.0 * sin(ls + h)
                + 11.0 * sin(-ls + h)
                - 25.0 * sin(-l2 + F)
                + 21.0 * sin(-l + F))
        val l_Moon: Double = TAU * frac(L0 + dL / 1296.0e3)
        val b_Moon: Double = (18520.0 * sin(S) + N) / ARCS
        val dt: Double =
            385000.5584 - 20905.3550 * cos(l) - 3699.1109 * cos(D2 - l) - 2955.9676 * cos(
                D2
            ) - 569.9251 * cos(l2)
        return Vector.ofPolar(l_Moon, b_Moon, dt)
    }

    /**
     * Calculates the geocentric position of the moon.
     *
     * @param date
     * [JulianDate] to be used
     * @return [Vector] of geocentric moon position
     */
    fun position(date: JulianDate): Vector {
        val rotateMatrix: Matrix = equatorialToEcliptical(date).transpose()
        return rotateMatrix.multiply(positionEquatorial(date))
    }

    /**
     * Calculates the horizontal position of the moon.
     *
     * @param date
     * [JulianDate] to be used
     * @param lat
     * Latitude, in radians
     * @param lng
     * Longitute, in radians
     * @return [Vector] of horizontal moon position
     */
    fun positionHorizontal(date: JulianDate, lat: Double, lng: Double): Vector {
        val mc: Vector = position(date)
        val h: Double = date.greenwichMeanSiderealTime + lng - mc.phi
        return equatorialToHorizontal(h, mc.theta, mc.r, lat)
    }

    /**
     * Returns the angular radius of the moon.
     *
     * @param distance
     * Distance of the moon, in kilometers.
     * @return Angular radius of the moon, in radians.
     * @see [Wikipedia: Angular
     * Diameter](https://en.wikipedia.org/wiki/Angular_diameter)
     */
    fun angularRadius(distance: Double): Double {
        return asin(MOON_MEAN_RADIUS / distance)
    }
}
