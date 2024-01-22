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
package org.shredzone.commons.suncalc.param

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar
import java.util.Date
import java.util.Objects
import java.util.TimeZone

/**
 * Time based parameters.
 *
 *
 * Use them to give information about the desired time. If ommitted, the current time and
 * the system's time zone is used.
 *
 * @param <T>
 * Type of the final builder
</T> */
interface TimeParameter<T> {
    /**
     * Sets date and time. Note that also seconds can be passed in for convenience, but
     * the results are not that accurate.
     *
     * @param year
     * Year
     * @param month
     * Month (1 = January, 2 = February, ...)
     * @param date
     * Day of month
     * @param hour
     * Hour of day
     * @param minute
     * Minute
     * @param second
     * Second
     * @return itself
     */
    fun on(year: Int, month: Int, date: Int, hour: Int, minute: Int, second: Int): T

    /**
     * Sets midnight of the year, month and date.
     *
     * @param year
     * Year
     * @param month
     * Month (1 = January, 2 = February, ...)
     * @param date
     * Day of month
     * @return itself
     */
    fun on(year: Int, month: Int, date: Int): T {
        return on(year, month, date, 0, 0, 0)
    }

    /**
     * Uses the given [ZonedDateTime] instance.
     *
     * @param dateTime
     * [ZonedDateTime] to be used.
     * @return itself
     */
    fun on(dateTime: ZonedDateTime?): T

    /**
     * Uses the given [LocalDateTime] instance.
     *
     * @param dateTime
     * [LocalDateTime] to be used.
     * @return itself
     */
    fun on(dateTime: LocalDateTime?): T

    /**
     * Uses the given [LocalDate] instance, and assumes midnight.
     *
     * @param date
     * [LocalDate] to be used.
     * @return itself
     */
    fun on(date: LocalDate?): T

    /**
     * Uses the given [Instant] instance.
     *
     * @param instant
     * [Instant] to be used.
     * @return itself
     */
    fun on(instant: Instant?): T

    /**
     * Uses the given [Date] instance.
     *
     * @param date
     * [Date] to be used.
     * @return itself
     */
    fun on(date: Date): T {
        Objects.requireNonNull(date, "date")
        return on(date.toInstant())
    }

    /**
     * Uses the given [Calendar] instance.
     *
     * @param cal
     * [Calendar] to be used
     * @return itself
     */
    fun on(cal: Calendar): T {
        Objects.requireNonNull(cal, "cal")
        return on(ZonedDateTime.ofInstant(cal.toInstant(), cal.timeZone.toZoneId()))
    }

    /**
     * Sets the current date and time. This is the default.
     *
     * @return itself
     */
    fun now(): T

    /**
     * Sets the time to the start of the current date ("last midnight").
     *
     * @return itself
     */
    fun midnight(): T

    /**
     * Adds a number of days to the current date.
     *
     * @param days
     * Number of days to add
     * @return itself
     */
    fun plusDays(days: Int): T

    /**
     * Sets today, midnight.
     *
     *
     * It is the same as `now().midnight()`.
     *
     * @return itself
     */
    fun today(): T {
        now()
        midnight()
        return this as T
    }

    /**
     * Sets tomorrow, midnight.
     *
     *
     * It is the same as `now().midnight().plusDays(1)`.
     *
     * @return itself
     */
    fun tomorrow(): T {
        today()
        plusDays(1)
        return this as T
    }

    /**
     * Sets the given [ZoneId]. The local time is retained, so the parameter order
     * is not important.
     *
     * @param tz
     * [ZoneId] to be used.
     * @return itself
     */
    fun timezone(tz: ZoneId?): T

    /**
     * Sets the given timezone. This is a convenience method that just invokes
     * [ZoneId.of].
     *
     * @param id
     * ID of the time zone.
     * @return itself
     * @see ZoneId.of
     */
    fun timezone(id: String?): T {
        return timezone(ZoneId.of(id))
    }

    /**
     * Sets the system's timezone. This is the default.
     *
     * @return itself
     */
    fun localTime(): T {
        return timezone(ZoneId.systemDefault())
    }

    /**
     * Sets the time zone to UTC.
     *
     * @return itself
     */
    fun utc(): T {
        return timezone("UTC")
    }

    /**
     * Sets the [TimeZone].
     *
     * @param tz [TimeZone] to be used
     * @return itself
     */
    fun timezone(tz: TimeZone): T {
        Objects.requireNonNull(tz, "tz")
        return timezone(tz.toZoneId())
    }

    /**
     * Uses the same time as given in the [TimeParameter].
     *
     *
     * Changes to the source parameter will not affect this parameter, though.
     *
     * @param t  [TimeParameter] to be used.
     * @return itself
     */
    fun sameTimeAs(t: TimeParameter<*>?): T
}
