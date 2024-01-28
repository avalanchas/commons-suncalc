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
package com.kurviger.suncalc

import kotlinx.datetime.TimeZone

/**
 * Geocoordinates of some test locations.
 */
object Locations {
    /**
     * Cologne, Germany. A random city on the northern hemisphere.
     */
    val COLOGNE = doubleArrayOf(50.938056, 6.956944)
    val COLOGNE_TZ: TimeZone = TimeZone.of("Europe/Berlin")

    /**
     * Alert, Nunavut, Canada. The northernmost place in the world with a permanent
     * population.
     */
    val ALERT = doubleArrayOf(82.5, -62.316667)
    val ALERT_TZ: TimeZone = TimeZone.of("Canada/Eastern")

    /**
     * Wellington, New Zealand. A random city on the southern hemisphere, close to the
     * international date line.
     */
    val WELLINGTON = doubleArrayOf(-41.2875, 174.776111)
    val WELLINGTON_TZ: TimeZone = TimeZone.of("Pacific/Auckland")

    /**
     * Puerto Williams, Chile. The southernmost town in the world.
     */
    val PUERTO_WILLIAMS = doubleArrayOf(-54.933333, -67.616667)
    val PUERTO_WILLIAMS_TZ: TimeZone = TimeZone.of("America/Punta_Arenas")

    /**
     * Singapore. A random city close to the equator.
     */
    val SINGAPORE = doubleArrayOf(1.283333, 103.833333)
    val SINGAPORE_TZ: TimeZone = TimeZone.of("Asia/Singapore")

    /**
     * Martinique. To test a fix for issue #13.
     */
    val MARTINIQUE = doubleArrayOf(14.640725, -61.0112)
    val MARTINIQUE_TZ: TimeZone = TimeZone.of("America/Martinique")

    /**
     * Sydney. To test a fix for issue #14.
     */
    val SYDNEY = doubleArrayOf(-33.744272, 151.231291)
    val SYDNEY_TZ: TimeZone = TimeZone.of("Australia/Sydney")

    /**
     * Santa Monica, CA. To test a fix for issue #18.
     */
    val SANTA_MONICA = doubleArrayOf(34.0, -118.5)
    val SANTA_MONICA_TZ: TimeZone = TimeZone.of("America/Los_Angeles")
}
