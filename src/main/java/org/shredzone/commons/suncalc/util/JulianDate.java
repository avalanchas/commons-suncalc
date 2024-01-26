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
package org.shredzone.commons.suncalc.util;

import static java.lang.Math.floor;
import static java.lang.Math.round;
import static org.shredzone.commons.suncalc.util.ExtendedMath.PI2;
import static org.shredzone.commons.suncalc.util.ExtendedMath.frac;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * This class contains a Julian Date representation of a date.
 * <p>
 * Objects are immutable and threadsafe.
 */
public class JulianDate {

    private final ZonedDateTime dateTime;
    private final double mjd;

    /**
     * Creates a new {@link JulianDate}.
     *
     * @param time
     *            {@link ZonedDateTime} to use for the date.
     */
    public JulianDate(ZonedDateTime time) {
        dateTime = Objects.requireNonNull(time, "time");
        mjd = dateTime.toInstant().toEpochMilli() / 86400000.0 + 40587.0;
    }

    /**
     * Returns a {@link JulianDate} of the current date and the given hour.
     *
     * @param hour
     *            Hour of this date. This is a floating point value. Fractions are used
     *            for minutes and seconds.
     * @return {@link JulianDate} instance.
     */
    public JulianDate atHour(double hour) {
        return new JulianDate(dateTime.plusSeconds(round(hour * 60.0 * 60.0)));
    }

    /**
     * Returns a {@link JulianDate} of the given modified Julian date.
     *
     * @param mjd
     *            Modified Julian Date
     * @return {@link JulianDate} instance.
     */
    public JulianDate atModifiedJulianDate(double mjd) {
        Instant mjdi = Instant.ofEpochMilli(Math.round((mjd - 40587.0) * 86400000.0));
        return new JulianDate(ZonedDateTime.ofInstant(mjdi, dateTime.getZone()));
    }

    /**
     * Returns a {@link JulianDate} of the given Julian century.
     *
     * @param jc
     *            Julian Century
     * @return {@link JulianDate} instance.
     */
    public JulianDate atJulianCentury(double jc) {
        return atModifiedJulianDate(jc * 36525.0 + 51544.5);
    }

    /**
     * Returns this {@link JulianDate} as {@link ZonedDateTime} object.
     *
     * @return {@link ZonedDateTime} of this {@link JulianDate}.
     */
    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Returns the Modified Julian Date.
     *
     * @return Modified Julian Date, UTC.
     */
    public double getModifiedJulianDate() {
        return mjd;
    }

    /**
     * Returns the Julian Centuries.
     *
     * @return Julian Centuries, based on J2000 epoch, UTC.
     */
    public double getJulianCentury() {
        return (mjd - 51544.5) / 36525.0;
    }

    /**
     * Returns the Greenwich Mean Sidereal Time of this Julian Date.
     *
     * @return GMST
     */
    public double getGreenwichMeanSiderealTime() {
        final double secs = 86400.0;

        double mjd0 = floor(mjd);
        double ut = (mjd - mjd0) * secs;
        double t0 = (mjd0 - 51544.5) / 36525.0;
        double t = (mjd - 51544.5) / 36525.0;

        double gmst = 24110.54841
                + 8640184.812866 * t0
                + 1.0027379093 * ut
                + (0.093104 - 6.2e-6 * t) * t * t;

        return (PI2 / secs) * (gmst % secs);
    }

    /**
     * Returns the earth's true anomaly of the current date.
     * <p>
     * A simple approximation is used here.
     *
     * @return True anomaly, in radians
     */
    public double getTrueAnomaly() {
        return PI2 * frac((dateTime.getDayOfYear() - 5.0) / 365.256363);
    }

    @Override
    public String toString() {
        return String.format("%dd %02dh %02dm %02ds",
                (long) mjd,
                (long) (mjd * 24 % 24),
                (long) (mjd * 24 * 60 % 60),
                (long) (mjd * 24 * 60 * 60 % 60));
    }

}
