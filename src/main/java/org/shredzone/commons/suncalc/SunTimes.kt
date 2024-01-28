package com.kurviger.suncalc

import com.kurviger.suncalc.param.Builder
import com.kurviger.suncalc.param.LocationParameter
import com.kurviger.suncalc.param.TimeParameter
import com.kurviger.suncalc.util.BaseBuilder
import com.kurviger.suncalc.util.ExtendedMath
import com.kurviger.suncalc.util.JulianDate
import com.kurviger.suncalc.util.QuadraticInterpolation
import com.kurviger.suncalc.util.Sun
import com.kurviger.suncalc.util.ZonedDateTime
import com.kurviger.suncalc.util.toRadians
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * Calculates the rise and set times of the sun.
 */
class SunTimes private constructor(
    rise: ZonedDateTime?, set: ZonedDateTime?,
    noon: ZonedDateTime?, nadir: ZonedDateTime?,
    alwaysUp: Boolean, alwaysDown: Boolean
) {
    /**
     * Sunrise time. `null` if the sun does not rise that day.
     *
     *
     * Always returns a sunrise time if [Parameters.fullCycle] was set.
     */
    val rise: ZonedDateTime?

    /**
     * Sunset time. `null` if the sun does not set that day.
     *
     *
     * Always returns a sunset time if [Parameters.fullCycle] was set.
     */
    val set: ZonedDateTime?

    /**
     * The time when the sun reaches its highest point.
     *
     *
     * Use [.isAlwaysDown] to find out if the highest point is still below the
     * twilight angle.
     */
    val noon: ZonedDateTime?

    /**
     * The time when the sun reaches its lowest point.
     *
     *
     * Use [.isAlwaysUp] to find out if the lowest point is still above the
     * twilight angle.
     */
    val nadir: ZonedDateTime?

    /**
     * `true` if the sun never rises/sets, but is always above the twilight angle.
     */
    val isAlwaysUp: Boolean

    /**
     * `true` if the sun never rises/sets, but is always below the twilight angle.
     */
    val isAlwaysDown: Boolean

    init {
        this.rise = rise
        this.set = set
        this.noon = noon
        this.nadir = nadir
        isAlwaysUp = alwaysUp
        isAlwaysDown = alwaysDown
    }

    /**
     * Collects all parameters for [SunTimes].
     */
    interface Parameters : LocationParameter<Parameters>,
        TimeParameter<Parameters>, Builder<SunTimes> {
        /**
         * Sets the [Twilight] mode.
         *
         *
         * Defaults to [Twilight.VISUAL].
         *
         * @param twilight
         * [Twilight] mode to be used.
         * @return itself
         */
        fun twilight(twilight: Twilight): Parameters

        /**
         * Sets the desired elevation angle of the sun. The sunrise and sunset times are
         * referring to the moment when the center of the sun passes this angle.
         *
         * @param angle
         * Geocentric elevation angle, in degrees.
         * @return itself
         */
        fun twilight(angle: Double): Parameters

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
            return limit(1.days)
        }

        /**
         * Computes until all rise, set, noon, and nadir times are found.
         *
         *
         * This is the default.
         *
         * @return itself
         */
        fun fullCycle(): Parameters {
            return limit(365.days)
        }
    }

    /**
     * Enumeration of predefined twilights.
     *
     *
     * The twilight angles use a geocentric reference, by definition. However,
     * [.VISUAL] and [.VISUAL_LOWER] are topocentric, and take the spectator's
     * height and the atmospheric refraction into account.
     *
     * @see [Wikipedia: Twilight](https://en.wikipedia.org/wiki/Twilight)
     */
    enum class Twilight(
        /**
         * Returns the sun's angle at the twilight position, in degrees.
         */
        angle: Double,
        /**
         * Returns the angular position. `0.0` means center of the sun. `1.0`
         * means upper edge of the sun. `-1.0` means lower edge of the sun.
         * `null` means the angular position is not topocentric.
         */
        val position: Double? = null
    ) {
        /**
         * The moment when the visual upper edge of the sun crosses the horizon. This is
         * commonly referred to as "sunrise" and "sunset". Atmospheric refraction is taken
         * into account.
         *
         *
         * This is the default.
         */
        VISUAL(0.0, 1.0),

        /**
         * The moment when the visual lower edge of the sun crosses the horizon. This is
         * the ending of the sunrise and the starting of the sunset. Atmospheric
         * refraction is taken into account.
         */
        VISUAL_LOWER(0.0, -1.0),

        /**
         * The moment when the center of the sun crosses the horizon (0°).
         */
        HORIZON(0.0),

        /**
         * Civil twilight (-6°).
         */
        CIVIL(-6.0),

        /**
         * Nautical twilight (-12°).
         */
        NAUTICAL(-12.0),

        /**
         * Astronomical twilight (-18°).
         */
        ASTRONOMICAL(-18.0),

        /**
         * Golden hour (6°). The Golden hour is between [.GOLDEN_HOUR] and
         * [.BLUE_HOUR]. The Magic hour is between [.GOLDEN_HOUR] and
         * [.CIVIL].
         *
         * @see [](https://en.wikipedia.org/wiki/Golden_hour_
        ) */
        GOLDEN_HOUR(6.0),

        /**
         * Blue hour (-4°). The Blue hour is between [.NIGHT_HOUR] and
         * [.BLUE_HOUR].
         *
         * @see [Wikipedia: Blue hour](https://en.wikipedia.org/wiki/Blue_hour)
         */
        BLUE_HOUR(-4.0),

        /**
         * End of Blue hour (-8°).
         *
         *
         * "Night Hour" is not an official term, but just a name that is marking the
         * beginning/end of the Blue hour.
         */
        NIGHT_HOUR(-8.0);

        /**
         * Returns the sun's angle at the twilight position, in radians.
         */
        val angleRad: Double

        init {
            angleRad = toRadians(angle)
        }

        /**
         * Returns `true` if this twilight position is topocentric. Then the
         * parallax and the atmospheric refraction is taken into account.
         */
        fun isTopocentric(): Boolean = position != null
        fun getAngularPosition() = position
    }

    /**
     * Builder for [SunTimes]. Performs the computations based on the parameters,
     * and creates a [SunTimes] object that holds the result.
     */
    private class SunTimesBuilder : BaseBuilder<Parameters>(), Parameters {
        private var angle = Twilight.VISUAL.angleRad
        private var position: Double? = Twilight.VISUAL.getAngularPosition()
        private var limit = 365.days

        override fun twilight(twilight: Twilight): Parameters {
            angle = twilight.angleRad
            position = twilight.getAngularPosition()
            return this
        }

        override fun twilight(angle: Double): Parameters {
            this.angle = toRadians(angle)
            position = null
            return this
        }

        override fun limit(duration: Duration): Parameters {
            require(!(duration.isNegative())) { "duration must be positive" }
            limit = duration
            return this
        }

        override fun execute(): SunTimes {
            val jd = julianDate
            var rise: Double? = null
            var set: Double? = null
            var noon: Double? = null
            var nadir: Double? = null
            var alwaysUp = false
            var alwaysDown = false
            var ye: Double
            var hour = 0
            val limitHours = limit.inWholeMilliseconds / (60 * 60 * 1000.0)
            val maxHours = ceil(limitHours).toInt()
            var y_minus = correctedSunHeight(jd.atHour(hour - 1.0))
            var y_0 = correctedSunHeight(jd.atHour(hour.toDouble()))
            var y_plus = correctedSunHeight(jd.atHour(hour + 1.0))
            if (y_0 > 0.0) {
                alwaysUp = true
            } else {
                alwaysDown = true
            }
            while (hour <= maxHours) {
                val qi = QuadraticInterpolation(y_minus, y_0, y_plus)
                ye = qi.ye
                if (qi.numberOfRoots == 1) {
                    val rt = qi.root1 + hour
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
                        val rt = hour + if (ye < 0.0) qi.root2 else qi.root1
                        if (rt >= 0.0 && rt < limitHours) {
                            rise = rt
                            alwaysDown = false
                        }
                    }
                    if (set == null) {
                        val rt = hour + if (ye < 0.0) qi.root1 else qi.root2
                        if (rt >= 0.0 && rt < limitHours) {
                            set = rt
                            alwaysUp = false
                        }
                    }
                }
                val xeAbs = abs(qi.xe)
                if (xeAbs <= 1.0) {
                    val xeHour = qi.xe + hour
                    if (xeHour >= 0.0) {
                        if (qi.isMaximum) {
                            if (noon == null) {
                                noon = xeHour
                            }
                        } else {
                            if (nadir == null) {
                                nadir = xeHour
                            }
                        }
                    }
                }
                if (rise != null && set != null && noon != null && nadir != null) {
                    break
                }
                hour++
                y_minus = y_0
                y_0 = y_plus
                y_plus = correctedSunHeight(jd.atHour(hour + 1.0))
            }
            if (noon != null) {
                noon = ExtendedMath.readjustMax(noon, 2.0, 14) {
                    correctedSunHeight(jd.atHour(it))
                }
                if (noon < 0.0 || noon >= limitHours) {
                    noon = null
                }
            }
            if (nadir != null) {
                nadir = ExtendedMath.readjustMin(nadir, 2.0, 14) {
                    correctedSunHeight(jd.atHour(it))
                }
                if (nadir < 0.0 || nadir >= limitHours) {
                    nadir = null
                }
            }
            return SunTimes(
                if (rise != null) jd.atHour(rise).zonedDateTime else null,
                if (set != null) jd.atHour(set).zonedDateTime else null,
                if (noon != null) jd.atHour(noon).zonedDateTime else null,
                if (nadir != null) jd.atHour(nadir).zonedDateTime else null,
                alwaysUp,
                alwaysDown
            )
        }

        /**
         * Computes the sun height at the given date and position.
         *
         * @param jd [JulianDate] to use
         * @return height, in radians
         */
        private fun correctedSunHeight(jd: JulianDate): Double {
            val pos = Sun.positionHorizontal(
                jd,
                latitudeRad, longitudeRad
            )
            var hc = angle
            if (position != null) {
                hc -= ExtendedMath.apparentRefraction(hc)
                hc += ExtendedMath.parallax(height, pos.r)
                hc -= position!! * Sun.angularRadius(pos.r)
            }
            return pos.theta - hc
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("SunTimes[rise=").append(rise)
        sb.append(", set=").append(set)
        sb.append(", noon=").append(noon)
        sb.append(", nadir=").append(nadir)
        sb.append(", alwaysUp=").append(isAlwaysUp)
        sb.append(", alwaysDown=").append(isAlwaysDown)
        sb.append(']')
        return sb.toString()
    }

    companion object {
        /**
         * Starts the computation of [SunTimes].
         *
         * @return [Parameters] to set.
         */
        fun compute(): Parameters {
            return SunTimesBuilder()
        }
    }
}
