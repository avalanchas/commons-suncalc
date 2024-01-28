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

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

private const val ERROR: Double = 0.001
private const val PI_HALF: Double = PI / 2.0

/**
 * Unit tests for [Vector].
 */
class VectorTest {
    @Test
    fun testConstructors() {
        val v1 = Vector(20.0, 10.0, 5.0)
        assertEquals(20.0, v1.x)
        assertEquals(10.0, v1.y)
        assertEquals(5.0, v1.z)
        val v2 = Vector(doubleArrayOf(20.0, 10.0, 5.0))
        assertEquals(20.0, v2.x)
        assertEquals(10.0, v2.y)
        assertEquals(5.0, v2.z)
        val v3: Vector = Vector.ofPolar(0.5, 0.25)
        assertEquals(0.5, v3.phi)
        assertEquals(0.25, v3.theta)
        assertEquals(1.0, v3.r)
        val v4: Vector = Vector.ofPolar(0.5, 0.25, 50.0)
        assertEquals(0.5, v4.phi)
        assertEquals(0.25, v4.theta)
        assertEquals(50.0, v4.r)
    }

    @Test
    fun testBadConstructor() {
        assertFailsWith(IllegalArgumentException::class) {
            Vector(doubleArrayOf(20.0, 10.0))
        }
    }

    @Test
    fun testAdd() {
        val v1 = Vector(20.0, 10.0, 5.0)
        val v2 = Vector(10.0, 25.0, 15.0)
        val r1: Vector = v1.add(v2)
        assertEquals(30.0, r1.x)
        assertEquals(35.0, r1.y)
        assertEquals(20.0, r1.z)
        val r2: Vector = v2.add(v1)
        assertEquals(30.0, r2.x)
        assertEquals(35.0, r2.y)
        assertEquals(20.0, r2.z)
    }

    @Test
    fun testSubtract() {
        val v1 = Vector(20.0, 10.0, 5.0)
        val v2 = Vector(10.0, 25.0, 15.0)
        val r1: Vector = v1.subtract(v2)
        assertEquals(10.0, r1.x)
        assertEquals(-15.0, r1.y)
        assertEquals(-10.0, r1.z)
        val r2: Vector = v2.subtract(v1)
        assertEquals(-10.0, r2.x)
        assertEquals(15.0, r2.y)
        assertEquals(10.0, r2.z)
    }

    @Test
    fun testMultiply() {
        val v1 = Vector(20.0, 10.0, 5.0)
        val r1: Vector = v1.multiply(5.0)
        assertEquals(100.0, r1.x)
        assertEquals(50.0, r1.y)
        assertEquals(25.0, r1.z)
    }

    @Test
    fun testNegate() {
        val v1 = Vector(20.0, 10.0, 5.0)
        val r1: Vector = v1.negate()
        assertEquals(-20.0, r1.x)
        assertEquals(-10.0, r1.y)
        assertEquals(-5.0, r1.z)
    }

    @Test
    fun testCross() {
        val v1 = Vector(3.0, -3.0, 1.0)
        val v2 = Vector(4.0, 9.0, 2.0)
        val r1: Vector = v1.cross(v2)
        assertEquals(-15.0, r1.x)
        assertEquals(-2.0, r1.y)
        assertEquals(39.0, r1.z)
    }

    @Test
    fun testDot() {
        val v1 = Vector(1.0, 2.0, 3.0)
        val v2 = Vector(4.0, -5.0, 6.0)
        val r1: Double = v1.dot(v2)
        assertEquals(12.0, r1, ERROR)
    }

    @Test
    fun testNorm() {
        val v1 = Vector(5.0, -6.0, 7.0)
        val r1: Double = v1.norm()
        assertEquals(10.488, r1, ERROR)
    }

    @Test
    fun testEquals() {
        val v1 = Vector(3.0, -3.0, 1.0)
        val v2 = Vector(4.0, 9.0, 2.0)
        val v3 = Vector(3.0, -3.0, 1.0)
        assertNotEquals(v1, v2)
        assertEquals(v1, v3)
        assertNotEquals(v2, v3)
        assertEquals(v3, v1)
        assertNotNull(v1)
        assertNotEquals(v1, Any())
    }

    @Test
    fun testHashCode() {
        val h1: Int = Vector(3.0, -3.0, 1.0).hashCode()
        val h2: Int = Vector(4.0, 9.0, 2.0).hashCode()
        val h3: Int = Vector(3.0, -3.0, 1.0).hashCode()
        assertNotEquals(0, h1)
        assertNotEquals(0, h2)
        assertNotEquals(0, h3)
        assertNotEquals(h1, h2)
        assertEquals(h3, h1)
    }

    @Test
    fun testToString() {
        val v1 = Vector(3.0, -3.0, 1.0)
        assertEquals("(x=3.0, y=-3.0, z=1.0)", v1.toString())
    }

    @Test
    fun testToCartesian() {
        val v1: Vector = Vector.ofPolar(0.0, 0.0)
        assertEquals(1.0, v1.x, ERROR)
        assertEquals(0.0, v1.y, ERROR)
        assertEquals(0.0, v1.z, ERROR)
        val v2: Vector = Vector.ofPolar(PI_HALF, 0.0)
        assertEquals(0.0, v2.x, ERROR)
        assertEquals(1.0, v2.y, ERROR)
        assertEquals(0.0, v2.z, ERROR)
        val v3: Vector = Vector.ofPolar(0.0, PI_HALF)
        assertEquals(0.0, v3.x, ERROR)
        assertEquals(0.0, v3.y, ERROR)
        assertEquals(1.0, v3.z, ERROR)
        val v4: Vector = Vector.ofPolar(PI_HALF, PI_HALF)
        assertEquals(0.0, v4.x, ERROR)
        assertEquals(0.0, v4.y, ERROR)
        assertEquals(1.0, v4.z, ERROR)
        val v5: Vector = Vector.ofPolar(PI_HALF, -PI_HALF)
        assertEquals(0.0, v5.x, ERROR)
        assertEquals(0.0, v5.y, ERROR)
        assertEquals(-1.0, v5.z, ERROR)
        val v6: Vector = Vector.ofPolar(0.0, 0.0, 5.0)
        assertEquals(5.0, v6.x, ERROR)
        assertEquals(0.0, v6.y, ERROR)
        assertEquals(0.0, v6.z, ERROR)
        val v7: Vector = Vector.ofPolar(PI_HALF, 0.0, 5.0)
        assertEquals(0.0, v7.x, ERROR)
        assertEquals(5.0, v7.y, ERROR)
        assertEquals(0.0, v7.z, ERROR)
        val v8: Vector = Vector.ofPolar(0.0, PI_HALF, 5.0)
        assertEquals(0.0, v8.x, ERROR)
        assertEquals(0.0, v8.y, ERROR)
        assertEquals(5.0, v8.z, ERROR)
        val v9: Vector = Vector.ofPolar(PI_HALF, PI_HALF, 5.0)
        assertEquals(0.0, v9.x, ERROR)
        assertEquals(0.0, v9.y, ERROR)
        assertEquals(5.0, v9.z, ERROR)
        val v10: Vector = Vector.ofPolar(PI_HALF, -PI_HALF, 5.0)
        assertEquals(0.0, v10.x, ERROR)
        assertEquals(0.0, v10.y, ERROR)
        assertEquals(-5.0, v10.z, ERROR)
    }

    @Test
    fun testToPolar() {
        val v1 = Vector(20.0, 0.0, 0.0)
        assertEquals(0.0, v1.phi)
        assertEquals(0.0, v1.theta)
        assertEquals(20.0, v1.r)
        val v2 = Vector(0.0, 20.0, 0.0)
        assertEquals(PI_HALF, v2.phi)
        assertEquals(0.0, v2.theta)
        assertEquals(20.0, v2.r)
        val v3 = Vector(0.0, 0.0, 20.0)
        assertEquals(0.0, v3.phi)
        assertEquals(PI_HALF, v3.theta)
        assertEquals(20.0, v3.r)
        val v4 = Vector(-20.0, 0.0, 0.0)
        assertEquals(PI, v4.phi)
        assertEquals(0.0, v4.theta)
        assertEquals(20.0, v4.r)
        val v5 = Vector(0.0, -20.0, 0.0)
        assertEquals(PI + PI_HALF, v5.phi)
        assertEquals(0.0, v5.theta)
        assertEquals(20.0, v5.r)
        val v6 = Vector(0.0, 0.0, -20.0)
        assertEquals(0.0, v6.phi)
        assertEquals(-PI_HALF, v6.theta)
        assertEquals(20.0, v6.r)
        val v7 = Vector(0.0, 0.0, 0.0)
        assertEquals(0.0, v7.phi)
        assertEquals(0.0, v7.theta)
        assertEquals(0.0, v7.r)
    }
}
