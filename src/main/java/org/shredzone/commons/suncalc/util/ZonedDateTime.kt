package com.kurviger.suncalc.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class ZonedDateTime(val localDateTime: LocalDateTime, val timeZone: TimeZone) :
    Comparable<ZonedDateTime> {

    constructor(instant: Instant, timeZone: TimeZone) : this(
        instant.toLocalDateTime(
            timeZone
        ), timeZone
    )

    fun toInstant() = localDateTime.toInstant(timeZone)

    override fun compareTo(other: ZonedDateTime): Int =
        this.localDateTime.compareTo(other.localDateTime)

    companion object {
        fun now(): ZonedDateTime {
            return now(TimeZone.currentSystemDefault())
        }

        fun now(timeZone: TimeZone): ZonedDateTime {
            return ZonedDateTime(Clock.System.now(), timeZone)
        }
    }
}
