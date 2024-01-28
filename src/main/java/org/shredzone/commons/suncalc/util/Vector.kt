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

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A three dimensional vector.
 *
 *
 * Objects are is immutable and threadsafe.
 */
class Vector {
    /**
     * Returns the cartesian X coordinate.
     */
    val x: Double

    /**
     * Returns the cartesian Y coordinate.
     */
    val y: Double

    /**
     * Returns the cartesian Z coordinate.
     */
    val z: Double
    private val polar = Polar()

    /**
     * Creates a new [Vector] of the given cartesian coordinates.
     *
     * @param x
     * X coordinate
     * @param y
     * Y coordinate
     * @param z
     * Z coordinate
     */
    constructor(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Creates a new [Vector] of the given cartesian coordinates.
     *
     * @param d
     * Array of coordinates, must have 3 elements
     */
    constructor(d: DoubleArray) {
        require(d.size == 3) { "invalid vector length" }
        x = d[0]
        y = d[1]
        z = d[2]
    }

    val phi: Double
        /**
         * Returns the azimuthal angle (φ) in radians.
         */
        get() = polar.phi
    val theta: Double
        /**
         * Returns the polar angle (θ) in radians.
         */
        get() = polar.theta
    val r: Double
        /**
         * Returns the polar radial distance (r).
         */
        get() = polar.getR()

    /**
     * Returns a [Vector] that is the sum of this [Vector] and the given
     * [Vector].
     *
     * @param vec
     * [Vector] to add
     * @return Resulting [Vector]
     */
    fun add(vec: Vector): Vector {
        return Vector(
            x + vec.x,
            y + vec.y,
            z + vec.z
        )
    }

    /**
     * Returns a [Vector] that is the difference of this [Vector] and the
     * given [Vector].
     *
     * @param vec
     * [Vector] to subtract
     * @return Resulting [Vector]
     */
    fun subtract(vec: Vector): Vector {
        return Vector(
            x - vec.x,
            y - vec.y,
            z - vec.z
        )
    }

    /**
     * Returns a [Vector] that is the scalar product of this [Vector] and the
     * given scalar.
     *
     * @param scalar
     * Scalar to multiply
     * @return Resulting [Vector]
     */
    fun multiply(scalar: Double): Vector {
        return Vector(
            x * scalar,
            y * scalar,
            z * scalar
        )
    }

    /**
     * Returns the negation of this [Vector].
     *
     * @return Resulting [Vector]
     */
    fun negate(): Vector {
        return Vector(
            -x,
            -y,
            -z
        )
    }

    /**
     * Returns a [Vector] that is the cross product of this [Vector] and the
     * given [Vector].
     *
     * @param right
     * [Vector] to multiply
     * @return Resulting [Vector]
     */
    fun cross(right: Vector): Vector {
        return Vector(
            y * right.z - z * right.y,
            z * right.x - x * right.z,
            x * right.y - y * right.x
        )
    }

    /**
     * Returns the dot product of this [Vector] and the given [Vector].
     *
     * @param right
     * [Vector] to multiply
     * @return Resulting dot product
     */
    fun dot(right: Vector): Double {
        return x * right.x + y * right.y + z * right.z
    }

    /**
     * Returns the norm of this [Vector].
     *
     * @return Norm of this vector
     */
    fun norm(): Double {
        return sqrt(dot(this))
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Vector) {
            return false
        }
        val vec = other
        return x.compareTo(vec.x) == 0 && y.compareTo(vec.y) == 0 && z.compareTo(vec.z) == 0
    }

    override fun hashCode(): Int {
        return (x).hashCode() xor (y).hashCode() xor (z).hashCode()
    }

    override fun toString(): String {
        return "(x=$x, y=$y, z=$z)"
    }

    /**
     * Helper class for lazily computing the polar coordinates in an immutable Vector
     * object.
     */
    private inner class Polar {
        private var φ: Double? = null
        private var θ: Double? = null
        private var r: Double? = null

        /**
         * Sets polar coordinates.
         *
         * @param φ
         * Phi
         * @param θ
         * Theta
         * @param r
         * R
         */
        fun setPolar(φ: Double, θ: Double, r: Double) {
            this.φ = φ
            this.θ = θ
            this.r = r
        }

        val phi: Double
            get() {
                if (φ == null) {
                    φ = if (ExtendedMath.isZero(x) && ExtendedMath.isZero(y)) {
                        0.0
                    } else {
                        atan2(y, x)
                    }
                    if (φ!! < 0.0) {
                        φ = φ!! + ExtendedMath.TAU
                    }
                }
                return φ!!
            }

        val theta: Double
            get() {
                if (θ == null) {
                    val ρSqr = x * x + y * y
                    θ = if (ExtendedMath.isZero(z) && ExtendedMath.isZero(ρSqr)) {
                        0.0
                    } else {
                        atan2(z, sqrt(ρSqr))
                    }
                }
                return θ!!
            }

        fun getR(): Double {
            if (r == null) {
                r = sqrt(x * x + y * y + z * z)
            }
            return r!!
        }
    }

    companion object {
        /**
         * Creates a new [Vector] of the given polar coordinates.
         *
         * @param φ
         * Azimuthal Angle
         * @param θ
         * Polar Angle
         * @param r
         * Radial Distance
         * @return Created [Vector]
         */
        /**
         * Creates a new [Vector] of the given polar coordinates, with a radial distance
         * of 1.
         *
         * @param φ
         * Azimuthal Angle
         * @param θ
         * Polar Angle
         * @return Created [Vector]
         */
        fun ofPolar(φ: Double, θ: Double, r: Double = 1.0): Vector {
            val cosθ = cos(θ)
            val result = Vector(
                r * cos(φ) * cosθ,
                r * sin(φ) * cosθ,
                r * sin(θ)
            )
            result.polar.setPolar(φ, θ, r)
            return result
        }
    }
}
