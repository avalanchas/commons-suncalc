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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val ERROR: Double = 0.001

/**
 * Unit tests for [QuadraticInterpolation].
 */
class QuadraticInterpolationTest {
    @Test
    fun testTwoRootsAndMinimum() {
        val qi = QuadraticInterpolation(1.0, -1.0, 1.0)
        assertEquals(2, qi.numberOfRoots)
        assertEquals(-0.707, qi.root1, ERROR)
        assertEquals(0.707, qi.root2, ERROR)
        assertEquals(0.0, qi.xe, ERROR)
        assertEquals(-1.0, qi.ye, ERROR)
        assertFalse(qi.isMaximum)
    }

    @Test
    fun testTwoRootsAndMaximum() {
        val qi = QuadraticInterpolation(-1.0, 1.0, -1.0)
        assertEquals(2, qi.numberOfRoots)
        assertEquals(-0.707, qi.root1, ERROR)
        assertEquals(0.707, qi.root2, ERROR)
        assertEquals(0.0, qi.xe, ERROR)
        assertEquals(1.0, qi.ye, ERROR)
        assertTrue(qi.isMaximum)
    }

    @Test
    fun testOneRoot() {
        val qi = QuadraticInterpolation(2.0, 0.0, -1.0)
        assertEquals(1, qi.numberOfRoots)
        assertEquals(0.0, qi.root1, ERROR)
        assertEquals(1.5, qi.xe, ERROR)
        assertEquals(-1.125, qi.ye, ERROR)
        assertFalse(qi.isMaximum)
    }

    @Test
    fun testNoRoot() {
        val qi = QuadraticInterpolation(3.0, 2.0, 1.0)
        assertEquals(qi.numberOfRoots, 0)
    }
}
