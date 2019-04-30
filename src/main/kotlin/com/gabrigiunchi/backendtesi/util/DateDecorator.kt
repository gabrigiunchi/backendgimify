package com.gabrigiunchi.backendtesi.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*

class DateDecorator(val date: Date) {

    private val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

    init {
        calendar.time = this.date
    }

    val year: Int
        get() = this.calendar.get(Calendar.YEAR)

    /**
     * Range 0-11
     */
    val month: Int
        get() = this.calendar.get(Calendar.MONTH)

    /**
     * Range 1-31
     */
    val day: Int
        get() = this.calendar.get(Calendar.DAY_OF_MONTH)

    val dayOfWeek: Int
        get() {
            val value = this.calendar.get(Calendar.DAY_OF_WEEK)
            return if (value == 1) 7 else value - 1
        }

    val hour: Int
        get() = this.calendar.get(Calendar.HOUR_OF_DAY)

    val minutes: Int
        get() = this.calendar.get(Calendar.MINUTE)

    val seconds: Int
        get() = this.calendar.get(Calendar.SECOND)

    fun plusMinutes(minutes: Int): DateDecorator {
        val c = Calendar.getInstance()
        c.time = this.date
        c.add(Calendar.MINUTE, minutes)
        return DateDecorator(c.time)
    }

    fun plusDays(days: Int): DateDecorator {
        val c = Calendar.getInstance()
        c.time = this.date
        c.add(Calendar.DATE, days)
        return DateDecorator(c.time)
    }

    fun minusMinutes(minutes: Int): DateDecorator {
        return this.plusMinutes(-minutes)
    }

    fun minusDays(days: Int): DateDecorator {
        return this.plusDays(-days)
    }

    fun isSameDay(dateDecorator: DateDecorator): Boolean {
        return dateDecorator.toLocalDate() == this.toLocalDate()
    }

    fun isSameDay(date: Date): Boolean {
        return this.isSameDay(of(date))
    }

    fun format(pattern: String, timeZone: TimeZone): String {
        val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)
        formatter.timeZone = timeZone
        return formatter.format(this.date)
    }

    /**
     * Format the date with the given pattern in UTC timezone
     */
    fun format(pattern: String): String {
        return this.format(pattern, TimeZone.getTimeZone("UTC"))
    }

    /**
     * Format the date with the format yyyy-MM-dd'T'HH:mm:ssZ in the UTC timezone
     */
    fun format(): String {
        return this.format(DATE_TIME_FORMAT)
    }

    fun toOffsetDateTime(zoneId: ZoneId = ZoneId.of("UTC")): OffsetDateTime {
        return OffsetDateTime.ofInstant(this.date.toInstant(), zoneId)
    }

    fun toLocalDate(zoneId: ZoneId = ZoneId.of("UTC")): LocalDate =
            LocalDate.ofInstant(this.date.toInstant(), zoneId)


    fun toLocalTime(zoneId: ZoneId): LocalTime = LocalTime.ofInstant(this.date.toInstant(), zoneId)

    override fun toString(): String {
        return this.date.toString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other == null || other !is DateDecorator)
            false
        else this.date == other.date
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }

    companion object {
        const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"

        fun of(date: String, pattern: String): DateDecorator {
            val timeFormatter = SimpleDateFormat(pattern)
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
            return DateDecorator(timeFormatter.parse(date))
        }

        fun of(date: String): DateDecorator = of(date, DATE_TIME_FORMAT)
        fun of(dateObject: Date): DateDecorator = DateDecorator(dateObject)
        fun now(): DateDecorator = DateDecorator(Calendar.getInstance().time)

        fun startOfToday(zoneId: ZoneId = ZoneId.of("UTC")): DateDecorator {
            return of(Date.from(LocalDate.now(zoneId)
                    .atStartOfDay(zoneId)
                    .toInstant()))
        }

        fun endOfToday(zoneId: ZoneId = ZoneId.of("UTC")): DateDecorator {
            return of(Date.from(LocalDate.now(zoneId)
                    .plusDays(1)
                    .atStartOfDay(zoneId)
                    .toOffsetDateTime()
                    .toInstant()))
        }

        fun max(): DateDecorator {
            val calendar = Calendar.getInstance()
            calendar.time = Date(Long.MAX_VALUE)
            return DateDecorator(calendar.time)
        }

        fun createDate(date: String): DateDecorator = of(date, "yyyy-MM-dd")
    }
}