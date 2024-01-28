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
import com.kurviger.suncalc.param.TimeParameter
import com.kurviger.suncalc.util.BaseBuilder
import com.kurviger.suncalc.util.JulianDate
import com.kurviger.suncalc.util.Moon
import com.kurviger.suncalc.util.Sun
import com.kurviger.suncalc.util.Vector
import com.kurviger.suncalc.util.toDegrees
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin

/**
 * Calculates the illumination of the moon.
 */
class MoonIllumination private constructor(
    /**
     * Illuminated fraction. `0.0` indicates new moon, `1.0` indicates full
     * moon.
     */
    val fraction: Double,
    /**
     * Moon phase. Starts at `-180.0` (new moon, waxing), passes `0.0` (full
     * moon) and moves toward `180.0` (waning, new moon).
     *
     *
     * Note that for historical reasons, the range of this phase is different to the
     * moon phase angle used in [MoonPhase].
     */
    val phase: Double,
    /**
     * The angle of the moon illumination relative to earth. The moon is waxing if the
     * angle is negative, and waning if positive.
     *
     *
     * By subtracting [MoonPosition.getParallacticAngle] from [.getAngle],
     * one can get the zenith angle of the moons bright limb (anticlockwise). The zenith
     * angle can be used do draw the moon shape from the observer's perspective (e.g. the
     * moon lying on its back).
     */
    val angle: Double
) {

    /**
     * Collects all parameters for [MoonIllumination].
     */
    interface Parameters : TimeParameter<Parameters?>, Builder<MoonIllumination?>

    /**
     * Builder for [MoonIllumination]. Performs the computations based on the
     * parameters, and creates a [MoonIllumination] object that holds the result.
     */
    private class MoonIlluminationBuilder : BaseBuilder<Parameters?>(), Parameters {
        override fun execute(): MoonIllumination {
            val t: JulianDate = julianDate
            val s: Vector = Sun.position(t)
            val m: Vector = Moon.position(t)
            val phi: Double =
                PI - acos(m.dot(s) / (m.r * s.r))
            val sunMoon: Vector = m.cross(s)
            val angle: Double = atan2(
                cos(s.theta) * sin(s.phi - m.phi),
                sin(s.theta) * cos(m.theta) - cos(
                    s.theta
                ) * sin(m.theta) * cos(s.phi - m.phi)
            )
            return MoonIllumination(
                (1 + cos(phi)) / 2,
                toDegrees(phi * sign(sunMoon.theta)),
                toDegrees(angle)
            )
        }
    }

    val closestPhase: MoonPhase.Phase
        /**
         * The closest [MoonPhase.Phase] that is matching the moon's angle.
         *
         * @return Closest [MoonPhase.Phase]
         * @since 3.5
         */
        get() = MoonPhase.Phase.toPhase(phase + 180.0)

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("MoonIllumination[fraction=").append(fraction)
        sb.append(", phase=").append(phase)
        sb.append("°, angle=").append(angle)
        sb.append("°]")
        return sb.toString()
    }

    companion object {
        /**
         * Starts the computation of [MoonIllumination].
         *
         * @return [Parameters] to set.
         */
        fun compute(): Parameters {
            return MoonIlluminationBuilder()
        }
    }
}
