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

import com.kurviger.suncalc.util.ExtendedMath.dms
import com.kurviger.suncalc.util.ExtendedMath.frac
import com.kurviger.suncalc.util.ExtendedMath.isZero
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [ExtendedMath].
 */
class ExtendedMathTest {
    @Test
    fun testFrac() {
        assertEquals(0.0, frac(1.0), ERROR)
        assertEquals(0.5, frac(0.5), ERROR)
        assertEquals(0.25, frac(123.25), ERROR)
        assertEquals(0.0, frac(0.0), ERROR)
        assertEquals(0.0, frac(-1.0), ERROR)
        assertEquals(-0.5, frac(-0.5), ERROR)
        assertEquals(-0.25, frac(-123.25), ERROR)
    }

    @Test
    fun testIsZero() {
        assertFalse(isZero(1.0))
        assertFalse(isZero(0.0001))
        assertTrue(isZero(0.0))
        assertTrue(isZero(-0.0))
        assertFalse(isZero(-0.0001))
        assertFalse(isZero(-1.0))
        assertFalse(isZero(Double.NaN))
        assertFalse(isZero(-Double.NaN))
        assertFalse(isZero(Double.POSITIVE_INFINITY))
        assertFalse(isZero(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun testDms() {
        // Valid parameters
        assertEquals(0.0, dms(0, 0, 0.0))
        assertEquals(13.4512, dms(13, 27, 4.32))
        assertEquals(-88.6523, dms(-88, 39, 8.28))

        // Sign at wrong position is ignored
        assertEquals(14.234, dms(14, -14, 2.4))
        assertEquals(66.213, dms(66, 12, -46.8))

        // Out of range values are carried to the next position
        assertEquals(0.02, dms(0, 0, 72.0)) // 0°  1' 12.0"
        assertEquals(2.37, dms(1, 80, 132.0)) // 2° 22' 12.0"
    }

    companion object {
        private val ERROR: Double = 0.001
    }
}
