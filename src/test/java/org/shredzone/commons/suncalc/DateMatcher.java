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
package org.shredzone.commons.suncalc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * A {@link Matcher} for comparing a {@link Date} object with a string representation.
 */
public class DateMatcher extends BaseMatcher<Date> {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private final String expected;
    private final TimeZone timeZone;

    /**
     * Creates a new matcher for matching {@link Date} objects.
     *
     * @param expected
     *            Expected date, as string. Format "yyyy-MM-dd'T'HH:mm:ss'Z'". The
     *            expected date must be UTC. Compares date and time, but does not compare
     *            milliseconds.
     */
    public static DateMatcher is(String expected) {
        return new DateMatcher(expected, UTC);
    }

    /**
     * Creates a new matcher for matching {@link Date} objects.
     *
     * @param expected
     *            Expected date, as string. Format "yyyy-MM-dd'T'HH:mm:ssZ". The expected
     *            date must use the correct offset of the given time zone. Compares date
     *            and time, but does not compare milliseconds.
     * @param tz
     *            Name of the time zone to be used for matching
     */
    public static DateMatcher is(String expected, String tz) {
        return new DateMatcher(expected, TimeZone.getTimeZone(tz));
    }

    private DateMatcher(String expected, TimeZone timeZone) {
        this.expected = expected;
        this.timeZone = timeZone;
    }

    @Override
    public boolean matches(Object item) {
        if (item == null || !(item instanceof Date)) {
            return false;
        }

        String fmtDate = dateToString((Date) item, timeZone);
        return fmtDate.equals(expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(expected);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (item == null) {
            description.appendText("is null");
            return;
        }

        if (!(item instanceof Date)) {
            description.appendText("is not a Date");
            return;
        }

        description.appendText("was ").appendValue(dateToString((Date) item, timeZone));
    }

    /**
     * Formats date as String.
     *
     * @param date
     *            {@link Date} to format
     * @return String representation to compare with
     */
    private static String dateToString(Date date, TimeZone tz) {
        SimpleDateFormat fmt = new SimpleDateFormat(
              tz.equals(UTC)
            ? "yyyy-MM-dd'T'HH:mm:ss'Z'"
            : "yyyy-MM-dd'T'HH:mm:ssZ"
        );
        fmt.setTimeZone(tz);
        return fmt.format(date);
    }

}
