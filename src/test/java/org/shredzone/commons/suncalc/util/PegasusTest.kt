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
package com.kurviger.suncalc.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val ERROR: Double = 0.001

/**
 * Unit tests for [Pegasus].
 */
class PegasusTest {
    @Test
    fun testParabola() {
        // f(x) = x^2 + 2x - 3
        // Roots at x = -3 and x = 1
        val parabola = { x: Double ->
            x * x + 2 * x - 3
        }
        val r1: Double = Pegasus.calculate(0.0, 3.0, 0.1, parabola)
        assertEquals(1.0, r1, ERROR)
        val r2: Double = Pegasus.calculate(-5.0, 0.0, 0.1, parabola)
        assertEquals(-3.0, r2, ERROR)
        assertFailsWith(ArithmeticException::class, "Found a non-existing root") {
            Pegasus.calculate(-2.0, 0.5, 0.1, parabola)
        }
    }

    @Test
    fun testParabola2() {
        // f(x) = x^2 + 3
        // No roots
        assertFailsWith(ArithmeticException::class) {
            Pegasus.calculate(-5.0, 5.0, 0.1) { x -> x * x + 3 }
        }
    }

}
