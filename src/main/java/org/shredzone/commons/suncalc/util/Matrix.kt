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

import java.util.Arrays

/**
 * A three dimensional matrix.
 *
 *
 * Objects are immutable and threadsafe.
 */
class Matrix {
    private val mx: DoubleArray

    private constructor() {
        mx = DoubleArray(9)
    }

    private constructor(vararg values: Double) {
        require(!(values == null || values.size != 9)) { "requires 9 values" }
        mx = values
    }

    /**
     * Transposes this matrix.
     *
     * @return [Matrix] that is a transposition of this matrix.
     */
    fun transpose(): Matrix {
        val result = Matrix()
        for (i in 0..2) {
            for (j in 0..2) {
                result[i, j] = get(j, i)
            }
        }
        return result
    }

    /**
     * Negates this matrix.
     *
     * @return [Matrix] that is a negation of this matrix.
     */
    fun negate(): Matrix {
        val result = Matrix()
        for (i in 0..8) {
            result.mx[i] = -mx[i]
        }
        return result
    }

    /**
     * Adds a matrix to this matrix.
     *
     * @param right
     * [Matrix] to add
     * @return [Matrix] that is a sum of both matrices
     */
    fun add(right: Matrix): Matrix {
        val result = Matrix()
        for (i in 0..8) {
            result.mx[i] = mx[i] + right.mx[i]
        }
        return result
    }

    /**
     * Subtracts a matrix from this matrix.
     *
     * @param right
     * [Matrix] to subtract
     * @return [Matrix] that is the difference of both matrices
     */
    fun subtract(right: Matrix): Matrix {
        val result = Matrix()
        for (i in 0..8) {
            result.mx[i] = mx[i] - right.mx[i]
        }
        return result
    }

    /**
     * Multiplies two matrices.
     *
     * @param right
     * [Matrix] to multiply with
     * @return [Matrix] that is the product of both matrices
     */
    fun multiply(right: Matrix): Matrix {
        val result = Matrix()
        for (i in 0..2) {
            for (j in 0..2) {
                var scalp = 0.0
                for (k in 0..2) {
                    scalp += get(i, k) * right[k, j]
                }
                result[i, j] = scalp
            }
        }
        return result
    }

    /**
     * Performs a scalar multiplication.
     *
     * @param scalar
     * Scalar to multiply with
     * @return [Matrix] that is the scalar product
     */
    fun multiply(scalar: Double): Matrix {
        val result = Matrix()
        for (i in 0..8) {
            result.mx[i] = mx[i] * scalar
        }
        return result
    }

    /**
     * Applies this matrix to a [Vector].
     *
     * @param right
     * [Vector] to multiply with
     * @return [Vector] that is the product of this matrix and the given vector
     */
    fun multiply(right: Vector): Vector {
        val vec = doubleArrayOf(right.x, right.y, right.z)
        val result = DoubleArray(3)
        for (i in 0..2) {
            var scalp = 0.0
            for (j in 0..2) {
                scalp += get(i, j) * vec[j]
            }
            result[i] = scalp
        }
        return Vector(result)
    }

    /**
     * Gets a value from the matrix.
     *
     * @param r
     * Row number (0..2)
     * @param c
     * Column number (0..2)
     * @return Value at that position
     */
    operator fun get(r: Int, c: Int): Double {
        require(!(r < 0 || r > 2 || c < 0 || c > 2)) { "row/column out of range: $r:$c" }
        return mx[r * 3 + c]
    }

    /**
     * Changes a value in the matrix. As a [Matrix] object is immutable from the
     * outside, this method is private.
     *
     * @param r
     * Row number (0..2)
     * @param c
     * Column number (0..2)
     * @param v
     * New value
     */
    private operator fun set(r: Int, c: Int, v: Double) {
        require(!(r < 0 || r > 2 || c < 0 || c > 2)) { "row/column out of range: $r:$c" }
        mx[r * 3 + c] = v
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj == null || obj !is Matrix) {
            false
        } else Arrays.equals(
            mx,
            obj.mx
        )
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(mx)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('[')
        for (ix in 0..8) {
            if (ix % 3 == 0) {
                sb.append('[')
            }
            sb.append(mx[ix])
            if (ix % 3 == 2) {
                sb.append(']')
            }
            if (ix < 8) {
                sb.append(", ")
            }
        }
        sb.append(']')
        return sb.toString()
    }

    companion object {
        /**
         * Creates an identity matrix.
         *
         * @return Identity [Matrix]
         */
        fun identity(): Matrix {
            return Matrix(
                1.0, 0.0, 0.0,
                0.0, 1.0, 0.0,
                0.0, 0.0, 1.0
            )
        }

        /**
         * Creates a matrix that rotates a vector by the given angle at the X axis.
         *
         * @param angle
         * angle, in radians
         * @return Rotation [Matrix]
         */
        fun rotateX(angle: Double): Matrix {
            val s = Math.sin(angle)
            val c = Math.cos(angle)
            return Matrix(
                1.0, 0.0, 0.0,
                0.0, c, s,
                0.0, -s, c
            )
        }

        /**
         * Creates a matrix that rotates a vector by the given angle at the Y axis.
         *
         * @param angle
         * angle, in radians
         * @return Rotation [Matrix]
         */
        fun rotateY(angle: Double): Matrix {
            val s = Math.sin(angle)
            val c = Math.cos(angle)
            return Matrix(
                c, 0.0, -s,
                0.0, 1.0, 0.0,
                s, 0.0, c
            )
        }

        /**
         * Creates a matrix that rotates a vector by the given angle at the Z axis.
         *
         * @param angle
         * angle, in radians
         * @return Rotation [Matrix]
         */
        fun rotateZ(angle: Double): Matrix {
            val s = Math.sin(angle)
            val c = Math.cos(angle)
            return Matrix(
                c, s, 0.0,
                -s, c, 0.0,
                0.0, 0.0, 1.0
            )
        }
    }
}
