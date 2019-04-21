package com.gabrigiunchi.backendtesi.util

import java.text.SimpleDateFormat
import java.util.*

class DateDecorator(val date: Date) {

    private val calendar = Calendar.getInstance()

    init {
        calendar.time = this.date
    }

    val year: Int
        get() = this.calendar.get(Calendar.YEAR)

    val month: Int
        get() = this.calendar.get(Calendar.MONTH)

    val day: Int
        get() = this.calendar.get(Calendar.DAY_OF_MONTH)

    val dayOfWeek: Int
        get() {
            val value = this.calendar.get(Calendar.DAY_OF_WEEK)
            return if (value == 1) 7 else value -1
        }

    val hour: Int
        get() = this.calendar.get(Calendar.HOUR_OF_DAY)

    val minutes: Int
        get() = this.calendar.get(Calendar.MINUTE)

    val seconds: Int
        get() = this.calendar.get(Calendar.SECOND)

    fun plusMinutes(minutes: Int): DateDecorator
    {
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

    fun minusMinutes(minutes: Int): DateDecorator
    {
        val c = Calendar.getInstance()
        c.time = this.date
        c.add(Calendar.MINUTE, -minutes)
        return DateDecorator(c.time)
    }

    fun isSameDay(dateDecorator: DateDecorator): Boolean {
        return dateDecorator.day == this.day && dateDecorator.month == this.month && dateDecorator.year == this.year
    }

    fun isSameDay(date: Date): Boolean {
        return this.isSameDay(of(date))
    }

    fun format(pattern: String, timeZone: TimeZone): String
    {
        val formatter = SimpleDateFormat(pattern)
        formatter.timeZone = timeZone
        return formatter.format(this.date)
    }

    /**
     * Format the date with the given pattern in UTC timezone
     */
    fun format(pattern: String): String
    {
        return this.format(pattern, TimeZone.getTimeZone("UTC"))
    }

    /**
     * Format the date with the format yyyy-MM-dd'T'HH:mm:ssZ in the UTC timezone
     */
    fun format(): String
    {
        return this.format(DATE_TIME_FORMAT)
    }

    override fun toString(): String
    {
        return this.date.toString()
    }

    override fun equals(other: Any?): Boolean
    {
        if(other == null || other !is DateDecorator)
            return false

        return this.date == other.date
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }

    companion object {

        @JvmStatic
        val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"

        @JvmStatic
        fun of(date: String, pattern: String): DateDecorator
        {
            val timeFormatter = SimpleDateFormat(pattern)
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
            return DateDecorator(timeFormatter.parse(date))
        }

        @JvmStatic
        fun of(date: String): DateDecorator
        {
            return of(date, DATE_TIME_FORMAT)
        }

        @JvmStatic
        fun of(dateObject: Date): DateDecorator
        {
            return DateDecorator(dateObject)
        }

        @JvmStatic
        fun now(): DateDecorator {
            return DateDecorator(Calendar.getInstance().time)
        }

        @JvmStatic
        fun max(): DateDecorator
        {
            val calendar = Calendar.getInstance()
            calendar.time = Date(Long.MAX_VALUE)
            return DateDecorator(calendar.time)
        }

        @JvmStatic
        fun createDate(date: String): DateDecorator {
            return Companion.of(date, "yyyy-MM-dd")
        }
    }
}