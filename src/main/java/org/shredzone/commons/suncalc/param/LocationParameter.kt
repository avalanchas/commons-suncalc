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
package com.kurviger.suncalc.param

import com.kurviger.suncalc.util.ExtendedMath

/**
 * Location based parameters.
 *
 *
 * Use them to give information about the geolocation of the observer. If omitted, the
 * coordinates of [Null Island](https://en.wikipedia.org/wiki/Null_Island) are
 * used.
 *
 * @param <T>
 * Type of the final builder
</T> */
@Suppress("UNCHECKED_CAST")
interface LocationParameter<T> {
    /**
     * Sets the latitude.
     *
     * @param lat
     * Latitude, in degrees.
     * @return itself
     */
    fun latitude(lat: Double): T

    /**
     * Sets the longitude.
     *
     * @param lng
     * Longitude, in degrees.
     * @return itself
     */
    fun longitude(lng: Double): T

    /**
     * Sets the height.
     *
     * @param h
     * Height, in meters above sea level. Default: 0.0 m. Negative values are
     * silently changed to the acceptable minimum of 0.0 m.
     * @return itself
     */
    fun height(h: Double): T

    /**
     * Sets the height, in foot.
     *
     * @param ft
     * Height, in foot above sea level. Default: 0.0 ft. Negative values are
     * silently changed to the acceptable minimum of 0.0 ft.
     * @return itself
     * @since 3.8
     */
    fun heightFt(ft: Double): T {
        return height(ft * 0.3048)
    }

    /**
     * Sets the geolocation.
     *
     * @param lat
     * Latitude, in degrees.
     * @param lng
     * Longitude, in degrees.
     * @return itself
     */
    fun at(lat: Double, lng: Double): T {
        latitude(lat)
        longitude(lng)
        return this as T
    }

    /**
     * Sets the geolocation. In the given array, index 0 must contain the latitude, and
     * index 1 must contain the longitude. An optional index 2 may contain the height, in
     * meters.
     *
     *
     * This call is meant to be used for coordinates stored in constants.
     *
     * @param coordinates
     * Array containing the latitude and longitude, in degrees.
     * @return itself
     */
    fun at(coordinates: DoubleArray): T {
        require(!(coordinates.size != 2 && coordinates.size != 3)) { "Array must contain 2 or 3 doubles" }
        if (coordinates.size == 3) {
            height(coordinates[2])
        }
        return at(coordinates[0], coordinates[1])
    }

    /**
     * Sets the latitude.
     *
     * @param d
     * Degrees
     * @param m
     * Minutes
     * @param s
     * Seconds (and fraction of seconds)
     * @return itself
     */
    fun latitude(d: Int, m: Int, s: Double): T {
        return latitude(ExtendedMath.dms(d, m, s))
    }

    /**
     * Sets the longitude.
     *
     * @param d
     * Degrees
     * @param m
     * Minutes
     * @param s
     * Seconds (and fraction of seconds)
     * @return itself
     */
    fun longitude(d: Int, m: Int, s: Double): T {
        return longitude(ExtendedMath.dms(d, m, s))
    }

    /**
     * Uses the same location as given in the [LocationParameter] at this moment.
     *
     *
     * Changes to the source parameter will not affect this parameter, though.
     *
     * @param l  [LocationParameter] to be used.
     * @return itself
     */
    fun sameLocationAs(l: LocationParameter<*>?): T
}
