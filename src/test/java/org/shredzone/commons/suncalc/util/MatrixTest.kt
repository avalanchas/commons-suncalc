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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

private const val ERROR: Double = 0.001
private const val PI_HALF: Double = PI / 2.0

/**
 * Unit tests for [Matrix].
 */
class MatrixTest {
    @Test
    fun testIdentity() {
        val mx: Matrix = Matrix.identity()
        assertValues(
            mx,
            1.0, 0.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 0.0, 1.0
        )
    }

    @Test
    fun testRotateX() {
        val mx: Matrix = Matrix.rotateX(PI_HALF)
        assertValues(
            mx,
            1.0, 0.0, 0.0,
            0.0, 0.0, 1.0,
            0.0, -1.0, 0.0
        )
    }

    @Test
    fun testRotateY() {
        val mx: Matrix = Matrix.rotateY(PI_HALF)
        assertValues(
            mx,
            0.0, 0.0, -1.0,
            0.0, 1.0, 0.0,
            1.0, 0.0, 0.0
        )
    }

    @Test
    fun testRotateZ() {
        val mx: Matrix = Matrix.rotateZ(PI_HALF)
        assertValues(
            mx,
            0.0, 1.0, 0.0,
            -1.0, 0.0, 0.0,
            0.0, 0.0, 1.0
        )
    }

    @Test
    fun testTranspose() {
        val mx: Matrix = Matrix.rotateX(PI_HALF).transpose()
        assertValues(
            mx,
            1.0, 0.0, 0.0,
            0.0, 0.0, -1.0,
            0.0, 1.0, 0.0
        )
    }

    @Test
    fun testNegate() {
        val mx: Matrix = Matrix.identity().negate()
        assertValues(
            mx,
            -1.0, 0.0, 0.0,
            0.0, -1.0, 0.0,
            0.0, 0.0, -1.0
        )
    }

    @Test
    fun testAdd() {
        val mx1: Matrix = Matrix.rotateX(PI_HALF)
        val mx2: Matrix = Matrix.rotateY(PI_HALF)
        assertValues(
            mx1.add(mx2),
            1.0, 0.0, -1.0,
            0.0, 1.0, 1.0,
            1.0, -1.0, 0.0
        )
        assertValues(
            mx2.add(mx1),
            1.0, 0.0, -1.0,
            0.0, 1.0, 1.0,
            1.0, -1.0, 0.0
        )
    }

    @Test
    fun testSubtract() {
        val mx1: Matrix = Matrix.rotateX(PI_HALF)
        val mx2: Matrix = Matrix.rotateY(PI_HALF)
        assertValues(
            mx1.subtract(mx2),
            1.0, 0.0, 1.0,
            0.0, -1.0, 1.0,
            -1.0, -1.0, 0.0
        )
        assertValues(
            mx2.subtract(mx1),
            -1.0, 0.0, -1.0,
            0.0, 1.0, -1.0,
            1.0, 1.0, 0.0
        )
    }

    @Test
    fun testMultiply() {
        val mx1: Matrix = Matrix.rotateX(PI_HALF)
        val mx2: Matrix = Matrix.rotateY(PI_HALF)
        assertValues(
            mx1.multiply(mx2),
            0.0, 0.0, -1.0,
            1.0, 0.0, 0.0,
            0.0, -1.0, 0.0
        )
        assertValues(
            mx2.multiply(mx1),
            0.0, 1.0, 0.0,
            0.0, 0.0, 1.0,
            1.0, 0.0, 0.0
        )
    }

    @Test
    fun testScalarMultiply() {
        val mx: Matrix = Matrix.identity().multiply(5.0)
        assertValues(
            mx,
            5.0, 0.0, 0.0,
            0.0, 5.0, 0.0,
            0.0, 0.0, 5.0
        )
    }

    @Test
    fun testVectorMultiply() {
        val mx: Matrix = Matrix.rotateX(PI_HALF)
        val vc = Vector(5.0, 8.0, -3.0)
        val result: Vector = mx.multiply(vc)
        assertEquals(5.0, result.x, ERROR)
        assertEquals(-3.0, result.y, ERROR)
        assertEquals(-8.0, result.z, ERROR)
    }

    @Test
    fun testEquals() {
        val mx1: Matrix = Matrix.identity()
        val mx2: Matrix = Matrix.rotateX(PI_HALF)
        val mx3: Matrix = Matrix.identity()
        assertNotEquals(mx1, mx2)
        assertEquals(mx1, mx3)
        assertNotEquals(mx2, mx3)
        assertEquals(mx3, mx1)
        assertNotNull(mx1)
        assertNotEquals(mx1, Any())
    }

    @Test
    fun testHashCode() {
        val mx1: Int = Matrix.identity().hashCode()
        val mx2: Int = Matrix.rotateX(PI_HALF).hashCode()
        val mx3: Int = Matrix.identity().hashCode()
        assertNotEquals(mx1, 0)
        assertNotEquals(mx2, 0)
        assertNotEquals(mx3, 0)
        assertNotEquals(mx1, mx2)
        assertNotEquals(mx1, mx2)
        assertEquals(mx1, mx3)
    }

    @Test
    fun testToString() {
        val mx: Matrix = Matrix.identity()
        assertEquals("[[1.0, 0.0, 0.0], [0.0, 1.0, 0.0], [0.0, 0.0, 1.0]]", mx.toString())
    }

    private fun assertValues(mx: Matrix, vararg values: Double) {
        for (ix in values.indices) {
            val r = ix / 3
            val c = ix % 3
            assertEquals(values[ix], mx[r, c], ERROR)
        }
    }
}
