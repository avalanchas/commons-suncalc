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
@file:Suppress("UNCHECKED_CAST")

package com.kurviger.suncalc.util

import com.kurviger.suncalc.param.LocationParameter
import com.kurviger.suncalc.param.TimeParameter
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlin.math.max

/**
 * A base implementation of [LocationParameter] and [TimeParameter].
 *
 *
 * For internal use only.
 *
 * @param <T>
 * Type of the final builder
</T> */
open class BaseBuilder<T> : LocationParameter<T>, TimeParameter<T> {
    /**
     * Returns the latitude.
     *
     * @return Latitude, in degrees.
     */
    var latitude = 0.0
        private set

    /**
     * Returns the longitude.
     *
     * @return Longitude, in degrees.
     */
    var longitude = 0.0
        private set

    /**
     * Returns the height, in meters above sea level.
     *
     * @return Height, meters above sea level
     */
    var height = 0.0
        private set

    private var dateTime = ZonedDateTime(Clock.System.now(), TimeZone.currentSystemDefault())

    override fun on(zonedDateTime: ZonedDateTime): T {
        this.dateTime = zonedDateTime
        return this as T
    }

    override fun on(date: LocalDate): T {
        this.dateTime = ZonedDateTime(date.atTime(LocalTime.fromSecondOfDay(0)), dateTime.timeZone)
        return this as T
    }

    override fun on(instant: Instant): T {
        return on(ZonedDateTime(instant, TimeZone.currentSystemDefault()))
    }

    override fun on(year: Int, month: Int, date: Int, hour: Int, minute: Int, second: Int): T {
        return on(
            ZonedDateTime(
                LocalDateTime(year, month, date, hour, minute, second, 0),
                dateTime.timeZone
            )
        )
    }

    override fun now(): T {
        return on(ZonedDateTime(Clock.System.now(), TimeZone.currentSystemDefault()))
    }

    override fun plusDays(days: Int): T {
        return on(
            ZonedDateTime(
                dateTime.localDateTime.date.plus(days, DateTimeUnit.DAY)
                    .atTime(dateTime.localDateTime.time),
                dateTime.timeZone
            )
        )
    }

    override fun midnight(): T {
        return on(
            ZonedDateTime(
                dateTime.localDateTime.date.atStartOfDayIn(dateTime.timeZone),
                dateTime.timeZone
            )
        )
    }

    override fun timezone(tz: TimeZone): T {
        on(ZonedDateTime(dateTime.localDateTime, tz))
        return this as T
    }

    override fun latitude(lat: Double): T {
        require(!(lat < -90.0 || lat > 90.0)) { "Latitude out of range, -90.0 <= $lat <= 90.0" }
        latitude = lat
        return this as T
    }

    override fun longitude(lng: Double): T {
        require(!(lng < -180.0 || lng > 180.0)) { "Longitude out of range, -180.0 <= $lng <= 180.0" }
        longitude = lng
        return this as T
    }

    override fun height(h: Double): T {
        height = max(h, 0.0)
        return this as T
    }

    override fun sameTimeAs(t: TimeParameter<*>?): T {
        require(t is BaseBuilder<*>) { "Cannot read the TimeParameter" }
        dateTime = t.dateTime
        return this as T
    }

    override fun sameLocationAs(l: LocationParameter<*>?): T {
        require(l is BaseBuilder<*>) { "Cannot read the LocationParameter" }
        val origin = l
        latitude = origin.latitude
        longitude = origin.longitude
        height = origin.height
        return this as T
    }

    val longitudeRad: Double
        /**
         * Returns the longitude.
         *
         * @return Longitude, in radians.
         */
        get() = toRadians(longitude)
    val latitudeRad: Double
        /**
         * Returns the latitude.
         *
         * @return Latitude, in radians.
         */
        get() = toRadians(latitude)
    val julianDate: JulianDate
        /**
         * Returns the [JulianDate] to be used.
         *
         * @return [JulianDate]
         */
        get() = JulianDate(dateTime)
}
