/*
 * Shredzone Commons - suncalc
 *
 * Copyright (C) 2018 Richard "Shred" Körber
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
import com.kurviger.suncalc.util.ExtendedMath.TAU
import com.kurviger.suncalc.util.JulianDate
import com.kurviger.suncalc.util.Moon
import com.kurviger.suncalc.util.Pegasus
import com.kurviger.suncalc.util.Sun
import com.kurviger.suncalc.util.Vector
import com.kurviger.suncalc.util.ZonedDateTime
import com.kurviger.suncalc.util.toRadians
import kotlin.math.PI


/**
 * Calculates the date and time when the moon reaches the desired phase.
 *
 *
 * Note: Due to the simplified formulas used in suncalc, the returned time can have an
 * error of several minutes.
 */
class MoonPhase private constructor(time: ZonedDateTime, distance: Double) {
    private val time: ZonedDateTime

    /**
     * Geocentric distance of the moon at the given phase, in kilometers.
     *
     * @since 3.4
     */
    val distance: Double

    init {
        this.time = time
        this.distance = distance
    }

    /**
     * Collects all parameters for [MoonPhase].
     */
    interface Parameters : TimeParameter<Parameters?>, Builder<MoonPhase?> {
        /**
         * Sets the desired [Phase].
         *
         *
         * Defaults to [Phase.NEW_MOON].
         *
         * @param phase
         * [Phase] to be used.
         * @return itself
         */
        fun phase(phase: Phase): Parameters

        /**
         * Sets a free phase to be used.
         *
         * @param phase
         * Desired phase, in degrees. 0 = new moon, 90 = first quarter, 180 =
         * full moon, 270 = third quarter.
         * @return itself
         */
        fun phase(phase: Double): Parameters
    }

    /**
     * Enumeration of moon phases.
     */
    enum class Phase(
        /**
         * Returns the moons's angle in reference to the sun, in degrees.
         */
        val angle: Double
    ) {
        /**
         * New moon.
         */
        NEW_MOON(0.0),

        /**
         * Waxing crescent moon.
         *
         * @since 3.5
         */
        WAXING_CRESCENT(45.0),

        /**
         * Waxing half moon.
         */
        FIRST_QUARTER(90.0),

        /**
         * Waxing gibbous moon.
         *
         * @since 3.5
         */
        WAXING_GIBBOUS(135.0),

        /**
         * Full moon.
         */
        FULL_MOON(180.0),

        /**
         * Waning gibbous moon.
         *
         * @since 3.5
         */
        WANING_GIBBOUS(225.0),

        /**
         * Waning half moon.
         */
        LAST_QUARTER(270.0),

        /**
         * Waning crescent moon.
         *
         * @since 3.5
         */
        WANING_CRESCENT(315.0);

        /**
         * Returns the moons's angle in reference to the sun, in radians.
         */
        val angleRad: Double

        init {
            angleRad = toRadians(angle)
        }

        companion object {
            /**
             * Converts an angle to the closest matching moon phase.
             *
             * @param angle
             * Moon phase angle, in degrees. 0 = New Moon, 180 = Full Moon. Angles
             * outside the [0,360) range are normalized into that range.
             * @return Closest Phase that is matching that angle.
             * @since 3.5
             */
            fun toPhase(angle: Double): Phase {
                // bring into range 0.0 .. 360.0
                var normalized = angle % 360.0
                if (normalized < 0.0) {
                    normalized += 360.0
                }
                if (normalized < 22.5) {
                    return NEW_MOON
                }
                if (normalized < 67.5) {
                    return WAXING_CRESCENT
                }
                if (normalized < 112.5) {
                    return FIRST_QUARTER
                }
                if (normalized < 157.5) {
                    return WAXING_GIBBOUS
                }
                if (normalized < 202.5) {
                    return FULL_MOON
                }
                if (normalized < 247.5) {
                    return WANING_GIBBOUS
                }
                if (normalized < 292.5) {
                    return LAST_QUARTER
                }
                return if (normalized < 337.5) {
                    WANING_CRESCENT
                } else NEW_MOON
            }
        }
    }

    /**
     * Builder for [MoonPhase]. Performs the computations based on the parameters,
     * and creates a [MoonPhase] object that holds the result.
     */
    private class MoonPhaseBuilder : BaseBuilder<Parameters?>(), Parameters {
        private var phase = Phase.NEW_MOON.angleRad
        override fun phase(phase: Phase): Parameters {
            this.phase = phase.angleRad
            return this
        }

        override fun phase(phase: Double): Parameters {
            this.phase = toRadians(phase)
            return this
        }

        override fun execute(): MoonPhase {
            val jd: JulianDate = julianDate
            val dT = 7.0 / 36525.0 // step rate: 1 week
            val accuracy = 0.5 / 1440.0 / 36525.0 // accuracy: 30 seconds
            var t0: Double = jd.julianCentury
            var t1 = t0 + dT
            var d0 = moonphase(jd, t0)
            var d1 = moonphase(jd, t1)
            while (d0 * d1 > 0.0 || d1 < d0) {
                t0 = t1
                d0 = d1
                t1 += dT
                d1 = moonphase(jd, t1)
            }
            val tphase: Double = Pegasus.calculate(t0, t1, accuracy) { x -> moonphase(jd, x) }
            val tjd: JulianDate = jd.atJulianCentury(tphase)
            return MoonPhase(tjd.zonedDateTime, Moon.positionEquatorial(tjd).r)
        }

        /**
         * Calculates the position of the moon at the given phase.
         *
         * @param jd
         * Base Julian date
         * @param t
         * Ephemeris time
         * @return difference angle of the sun's and moon's position
         */
        private fun moonphase(jd: JulianDate, t: Double): Double {
            val sun: Vector = Sun.positionEquatorial(jd.atJulianCentury(t - SUN_LIGHT_TIME_TAU))
            val moon: Vector = Moon.positionEquatorial(jd.atJulianCentury(t))
            var diff: Double = moon.phi - sun.phi - phase //NOSONAR: false positive
            while (diff < 0.0) {
                diff += TAU
            }
            return (diff + PI) % TAU - PI
        }

        companion object {
            private const val SUN_LIGHT_TIME_TAU = 8.32 / (1440.0 * 36525.0)
        }
    }

    /**
     * Date and time of the desired moon phase. The time is rounded to full minutes.
     */
    fun getTime(): ZonedDateTime {
        return time
    }

    val isSuperMoon: Boolean
        /**
         * Checks if the moon is in a SuperMoon position.
         *
         *
         * Note that there is no official definition of supermoon. Suncalc will assume a
         * supermoon if the center of the moon is closer than 360,000 km to the center of
         * Earth. Usually only full moons or new moons are candidates for supermoons.
         *
         * @since 3.4
         */
        get() = distance < 360000.0
    val isMicroMoon: Boolean
        /**
         * Checks if the moon is in a MicroMoon position.
         *
         *
         * Note that there is no official definition of micromoon. Suncalc will assume a
         * micromoon if the center of the moon is farther than 405,000 km from the center of
         * Earth. Usually only full moons or new moons are candidates for micromoons.
         *
         * @since 3.4
         */
        get() = distance > 405000.0

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("MoonPhase[time=").append(time)
        sb.append(", distance=").append(distance)
        sb.append(" km]")
        return sb.toString()
    }

    companion object {
        /**
         * Starts the computation of [MoonPhase].
         *
         * @return [Parameters] to set.
         */
        fun compute(): Parameters {
            return MoonPhaseBuilder()
        }
    }
}
