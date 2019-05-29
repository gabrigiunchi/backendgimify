package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.Entity

@Entity
class RepeatedInterval(
        id: Int,
        start: LocalDateTime,
        end: LocalDateTime,
        val repetitionType: RepetitionType,
        val repetitionEnd: LocalDateTime?
) : Interval(id, start, end) {

    constructor(start: String, end: String) : this(start, end, RepetitionType.NONE)

    constructor(start: String, end: String, repetitionType: RepetitionType) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, null)

    constructor(start: String, end: String, repetitionType: RepetitionType, repetitionEnd: String) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, LocalDateTime.parse(repetitionEnd))


    companion object {
        fun create(dayOfWeek: DayOfWeek, start: String, end: String): RepeatedInterval =
                this.create(dayOfWeek, LocalTime.parse(start), LocalTime.parse(end))

        fun create(dayOfWeek: DayOfWeek, start: LocalTime, end: LocalTime): RepeatedInterval {
            val startDate = LocalDateTime.from(dayOfWeek.adjustInto(start.atDate(LocalDate.ofYearDay(2019, 1))))
            val endDate = LocalDateTime.from(dayOfWeek.adjustInto(end.atDate(LocalDate.ofYearDay(2019, 1))))
            return RepeatedInterval(-1, startDate, endDate, RepetitionType.WEEKLY, null)
        }
    }

    override fun contains(interval: Interval): Boolean {
        if (this.repetitionType == RepetitionType.NONE) {
            return super.contains(interval)
        }

        var s = this.start
        var e = this.end

        while (s <= interval.end && !this.isBeyondRepetitionEnd(interval.end)) {
            if (Interval(s, e).contains(interval)) {
                return true
            }

            when (this.repetitionType) {
                RepetitionType.NONE -> {
                }
                RepetitionType.DAILY -> {
                    s = s.plusDays(1)
                    e = e.plusDays(1)
                }
                RepetitionType.WEEKLY -> {
                    s = s.plusWeeks(1)
                    e = e.plusWeeks(1)
                }

                RepetitionType.MONTHLY -> {
                    s = s.plusMonths(1)
                    e = e.plusMonths(1)
                }

                RepetitionType.YEARLY -> {
                    s = s.plusYears(1)
                    e = e.plusYears(1)
                }
            }
        }

        return false
    }

    override fun contains(date: LocalDateTime): Boolean {
        if (this.repetitionType == RepetitionType.NONE) {
            return super.contains(date)
        }

        var s = this.start
        var e = this.end

        while (s <= date && !this.isBeyondRepetitionEnd(date)) {
            if (Interval(s, e).contains(date)) {
                return true
            }

            when (this.repetitionType) {
                RepetitionType.NONE -> {
                }
                RepetitionType.DAILY -> {
                    s = s.plusDays(1)
                    e = e.plusDays(1)
                }
                RepetitionType.WEEKLY -> {
                    s = s.plusWeeks(1)
                    e = e.plusWeeks(1)
                }

                RepetitionType.MONTHLY -> {
                    s = s.plusMonths(1)
                    e = e.plusMonths(1)
                }

                RepetitionType.YEARLY -> {
                    s = s.plusYears(1)
                    e = e.plusYears(1)
                }
            }
        }

        return false
    }

    override fun overlaps(interval: Interval): Boolean {
        if (this.repetitionType == RepetitionType.NONE) {
            return super.overlaps(interval)
        }

        var s = this.start
        var e = this.end

        while (s <= interval.end && !this.isBeyondRepetitionEnd(interval.start)) {
            if (Interval(s, e).overlaps(interval)) {
                return true
            }

            when (this.repetitionType) {
                RepetitionType.NONE -> {
                }
                RepetitionType.DAILY -> {
                    s = s.plusDays(1)
                    e = e.plusDays(1)
                }
                RepetitionType.WEEKLY -> {
                    s = s.plusWeeks(1)
                    e = e.plusWeeks(1)
                }

                RepetitionType.MONTHLY -> {
                    s = s.plusMonths(1)
                    e = e.plusMonths(1)
                }

                RepetitionType.YEARLY -> {
                    s = s.plusYears(1)
                    e = e.plusYears(1)
                }
            }
        }

        return false
    }

    override fun contains(date: String): Boolean = this.contains(LocalDateTime.parse(date))

    private fun isBeyondRepetitionEnd(date: LocalDateTime) = this.repetitionEnd != null && date >= this.repetitionEnd
}