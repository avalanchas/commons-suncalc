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

import org.shredzone.commons.suncalc.param.GenericParameter
import org.shredzone.commons.suncalc.param.LocationParameter
import org.shredzone.commons.suncalc.param.TimeParameter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Objects

/**
 * A base implementation of [LocationParameter] and [TimeParameter].
 *
 *
 * For internal use only.
 *
 * @param <T>
 * Type of the final builder
</T> */
open class BaseBuilder<T> : GenericParameter<T>, LocationParameter<T>, TimeParameter<T>, Cloneable {
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
    private var dateTime = ZonedDateTime.now()
    override fun on(dateTime: ZonedDateTime?): T {
        this.dateTime = Objects.requireNonNull(dateTime, "dateTime")
        return this as T
    }

    override fun on(dateTime: LocalDateTime?): T {
        Objects.requireNonNull(dateTime, "dateTime")
        return on(ZonedDateTime.of(dateTime, this.dateTime!!.zone))
    }

    override fun on(date: LocalDate?): T {
        Objects.requireNonNull(date, "date")
        return on(ZonedDateTime.of(date, LocalTime.MIDNIGHT, dateTime!!.zone))
    }

    override fun on(instant: Instant?): T {
        Objects.requireNonNull(instant, "instant")
        return on(ZonedDateTime.ofInstant(instant, dateTime!!.zone))
    }

    override fun on(year: Int, month: Int, date: Int, hour: Int, minute: Int, second: Int): T {
        return on(ZonedDateTime.of(year, month, date, hour, minute, second, 0, dateTime!!.zone))
    }

    override fun now(): T {
        return on(ZonedDateTime.now(dateTime!!.zone))
    }

    override fun plusDays(days: Int): T {
        return on(dateTime!!.plusDays(days.toLong()))
    }

    override fun midnight(): T {
        return on(dateTime!!.truncatedTo(ChronoUnit.DAYS))
    }

    override fun timezone(tz: ZoneId?): T {
        Objects.requireNonNull(tz, "tz")
        on(dateTime!!.withZoneSameLocal(tz))
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
        height = Math.max(h, 0.0)
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

    override fun copy(): T {
        return try {
            clone() as T
        } catch (ex: CloneNotSupportedException) {
            throw RuntimeException(ex) // Should never be thrown anyway
        }
    }

    val longitudeRad: Double
        /**
         * Returns the longitude.
         *
         * @return Longitude, in radians.
         */
        get() = Math.toRadians(longitude)
    val latitudeRad: Double
        /**
         * Returns the latitude.
         *
         * @return Latitude, in radians.
         */
        get() = Math.toRadians(latitude)
    val julianDate: JulianDate
        /**
         * Returns the [JulianDate] to be used.
         *
         * @return [JulianDate]
         */
        get() = JulianDate(dateTime)
}
