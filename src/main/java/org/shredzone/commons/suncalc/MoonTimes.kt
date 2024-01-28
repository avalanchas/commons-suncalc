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
import com.kurviger.suncalc.util.ExtendedMath.apparentRefraction
import com.kurviger.suncalc.util.ExtendedMath.parallax
import com.kurviger.suncalc.util.JulianDate
import com.kurviger.suncalc.util.Moon
import com.kurviger.suncalc.util.QuadraticInterpolation
import com.kurviger.suncalc.util.Vector
import com.kurviger.suncalc.util.ZonedDateTime
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Calculates the times of the moon.
 */
class MoonTimes private constructor(
    rise: ZonedDateTime?, set: ZonedDateTime?,
    alwaysUp: Boolean, alwaysDown: Boolean
) {
    private val rise: ZonedDateTime?

    private val set: ZonedDateTime?

    /**
     * `true` if the moon never rises/sets, but is always above the horizon.
     */
    val isAlwaysUp: Boolean

    /**
     * `true` if the moon never rises/sets, but is always below the horizon.
     */
    val isAlwaysDown: Boolean

    init {
        this.rise = rise
        this.set = set
        isAlwaysUp = alwaysUp
        isAlwaysDown = alwaysDown
    }

    /**
     * Collects all parameters for [MoonTimes].
     */
    interface Parameters : LocationParameter<Parameters?>, TimeParameter<Parameters?>,
        Builder<MoonTimes?> {
        /**
         * Limits the calculation window to the given [Duration].
         *
         * @param duration
         * Duration of the calculation window. Must be positive.
         * @return itself
         * @since 3.1
         */
        fun limit(duration: Duration): Parameters

        /**
         * Limits the time window to the next 24 hours.
         *
         * @return itself
         */
        fun oneDay(): Parameters? {
            return limit(1.toDuration(DurationUnit.DAYS))
        }

        /**
         * Computes until all rise and set times are found.
         *
         *
         * This is the default.
         *
         * @return itself
         */
        fun fullCycle(): Parameters? {
            return limit(365.toDuration(DurationUnit.DAYS))
        }
    }

    /**
     * Builder for [MoonTimes]. Performs the computations based on the parameters,
     * and creates a [MoonTimes] object that holds the result.
     */
    private class MoonTimesBuilder : BaseBuilder<Parameters?>(), Parameters {
        private var limit: Duration = 365.toDuration(DurationUnit.DAYS)
        private val refraction: Double = apparentRefraction(0.0)

        override fun limit(duration: Duration): Parameters {
            require(!duration.isNegative()) { "duration must be positive" }
            limit = duration
            return this
        }

        override fun execute(): MoonTimes {
            val jd: JulianDate = julianDate
            var rise: Double? = null
            var set: Double? = null
            var alwaysUp = false
            var alwaysDown = false
            var ye: Double
            var hour = 0
            val limitHours: Double = limit.inWholeMilliseconds / (60 * 60 * 1000.0)
            val maxHours: Int = ceil(limitHours).toInt()
            var y_minus = correctedMoonHeight(jd.atHour(hour - 1.0))
            var y_0 = correctedMoonHeight(jd.atHour(hour.toDouble()))
            var y_plus = correctedMoonHeight(jd.atHour(hour + 1.0))
            if (y_0 > 0.0) {
                alwaysUp = true
            } else {
                alwaysDown = true
            }
            while (hour <= maxHours) {
                val qi = QuadraticInterpolation(y_minus, y_0, y_plus)
                ye = qi.ye
                if (qi.numberOfRoots == 1) {
                    val rt: Double = qi.root1 + hour
                    if (y_minus < 0.0) {
                        if (rise == null && rt >= 0.0 && rt < limitHours) {
                            rise = rt
                            alwaysDown = false
                        }
                    } else {
                        if (set == null && rt >= 0.0 && rt < limitHours) {
                            set = rt
                            alwaysUp = false
                        }
                    }
                } else if (qi.numberOfRoots == 2) {
                    if (rise == null) {
                        val rt: Double = hour + if (ye < 0.0) qi.root2 else qi.root1
                        if (rt >= 0.0 && rt < limitHours) {
                            rise = rt
                            alwaysDown = false
                        }
                    }
                    if (set == null) {
                        val rt: Double = hour + if (ye < 0.0) qi.root1 else qi.root2
                        if (rt >= 0.0 && rt < limitHours) {
                            set = rt
                            alwaysUp = false
                        }
                    }
                }
                if (rise != null && set != null) {
                    break
                }
                hour++
                y_minus = y_0
                y_0 = y_plus
                y_plus = correctedMoonHeight(jd.atHour(hour + 1.0))
            }
            return MoonTimes(
                if (rise != null) jd.atHour(rise).zonedDateTime else null,
                if (set != null) jd.atHour(set).zonedDateTime else null,
                alwaysUp,
                alwaysDown
            )
        }

        /**
         * Computes the moon height at the given date and position.
         *
         * @param jd [JulianDate] to use
         * @return height, in radians
         */
        private fun correctedMoonHeight(jd: JulianDate): Double {
            val pos: Vector = Moon.positionHorizontal(jd, latitudeRad, longitudeRad)
            val hc: Double = (parallax(height, pos.r)
                    - refraction
                    - Moon.angularRadius(pos.r))
            return pos.theta - hc
        }
    }

    /**
     * Moonrise time. `null` if the moon does not rise that day.
     */
    fun getRise(): ZonedDateTime? {
        return rise
    }

    /**
     * Moonset time. `null` if the moon does not set that day.
     */
    fun getSet(): ZonedDateTime? {
        return set
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("MoonTimes[rise=").append(rise)
        sb.append(", set=").append(set)
        sb.append(", alwaysUp=").append(isAlwaysUp)
        sb.append(", alwaysDown=").append(isAlwaysDown)
        sb.append(']')
        return sb.toString()
    }

    companion object {
        /**
         * Starts the computation of [MoonTimes].
         *
         * @return [Parameters] to set.
         */
        fun compute(): Parameters {
            return MoonTimesBuilder()
        }
    }
}
