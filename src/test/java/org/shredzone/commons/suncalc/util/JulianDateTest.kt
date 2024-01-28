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

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

private const val ERROR: Double = 0.001

/**
 * Unit tests for [JulianDate].
 */
class JulianDateTest {
    @Test
    fun testAtHour() {
        val jd = JulianDate(of(2017, 8, 19, 0, 0, 0, "UTC"))
        assertDate(jd, "2017-08-19T00:00:00Z")
        val jd2: JulianDate = jd.atHour(8.5)
        assertDate(jd2, "2017-08-19T08:30:00Z")
        val jd3 = JulianDate(of(2017, 8, 19, 0, 0, 0, "Europe/Berlin"))
        assertDate(jd3, "2017-08-19T00:00:00+02:00")
        val jd4: JulianDate = jd3.atHour(8.5)
        assertDate(jd4, "2017-08-19T08:30:00+02:00")
    }

    @Test
    fun testModifiedJulianDate() {
        // MJD epoch is midnight of November 17th, 1858.
        val jd1 = JulianDate(of(1858, 11, 17, 0, 0, 0, "UTC"))
        assertEquals(0.0, jd1.modifiedJulianDate)
        assertEquals("0d 00h 00m 00s", jd1.toString())
        val jd2 = JulianDate(of(2017, 8, 19, 15, 6, 16, "UTC"))
        assertEquals(57984.629, jd2.modifiedJulianDate, ERROR)
        assertEquals("57984d 15h 06m 16s", jd2.toString())
        val jd3 = JulianDate(of(2017, 8, 19, 15, 6, 16, "GMT+2"))
        assertEquals(57984.546, jd3.modifiedJulianDate, ERROR)
        assertEquals("57984d 13h 06m 16s", jd3.toString())
    }

    @Test
    fun testJulianCentury() {
        val jd1 = JulianDate(of(2000, 1, 1, 0, 0, 0, "UTC"))
        assertEquals(0.0, jd1.julianCentury, ERROR)
        val jd2 = JulianDate(of(2017, 1, 1, 0, 0, 0, "UTC"))
        assertEquals(0.17, jd2.julianCentury, ERROR)
        val jd3 = JulianDate(of(2050, 7, 1, 0, 0, 0, "UTC"))
        assertEquals(0.505, jd3.julianCentury, ERROR)
    }

    @Test
    fun testGreenwichMeanSiderealTime() {
        val jd1 = JulianDate(of(2017, 9, 3, 19, 5, 15, "UTC"))
        assertEquals(4.702, jd1.greenwichMeanSiderealTime, ERROR)
    }

    @Test
    fun testTrueAnomaly() {
        val jd1 = JulianDate(of(2017, 1, 4, 0, 0, 0, "UTC"))
        assertEquals(0.0, jd1.trueAnomaly, 0.1)
        val jd2 = JulianDate(of(2017, 7, 4, 0, 0, 0, "UTC"))
        assertEquals(PI, jd2.trueAnomaly, 0.1)
    }

    @Test
    fun testAtModifiedJulianDate() {
        val jd1 = JulianDate(of(2017, 8, 19, 15, 6, 16, "UTC"))
        val jd2: JulianDate = JulianDate(ZonedDateTime.now(TimeZone.UTC))
            .atModifiedJulianDate(jd1.modifiedJulianDate)
        assertEquals(jd1.zonedDateTime, jd2.zonedDateTime)
    }

    @Test
    fun testAtJulianCentury() {
        val jd1 = JulianDate(of(2017, 1, 1, 0, 0, 0, "UTC"))
        val jd2: JulianDate = JulianDate(ZonedDateTime.now(TimeZone.UTC))
            .atJulianCentury(jd1.julianCentury)
        assertEquals(jd1.zonedDateTime, jd2.zonedDateTime)
    }

    private fun of(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        zoneId: String
    ): ZonedDateTime {
        return ZonedDateTime(
            LocalDateTime(
                year = year,
                monthNumber = month,
                dayOfMonth = day,
                hour = hour,
                minute = minute,
                second = second,
                nanosecond = 0
            ),
            TimeZone.of(zoneId)
        )
    }

    private fun assertDate(jd: JulianDate, date: String) {
        assertEquals(date, jd.zonedDateTime.toString())
    }
}
