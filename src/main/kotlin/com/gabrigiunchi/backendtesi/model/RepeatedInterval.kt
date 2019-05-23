package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import java.time.*
import javax.persistence.Entity

@Entity
class RepeatedInterval(
        id: Int,
        start: LocalDateTime,
        end: LocalDateTime,
        val repetitionType: RepetitionType,
        val repetitionEnd: LocalDate
) : Interval(id, start, end) {

    constructor(start: String, end: String) : this(start, end, RepetitionType.none)

    constructor(start: String, end: String, repetitionType: RepetitionType) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, LocalDate.MAX)

    constructor(start: String, end: String, repetitionType: RepetitionType, repetitionEnd: String) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, LocalDate.parse(repetitionEnd))


    companion object {
        fun create(monthDay: MonthDay): RepeatedInterval =
                RepeatedInterval(
                        -1,
                        LocalDateTime.of(2018, monthDay.month, monthDay.dayOfMonth, 0, 0),
                        LocalDateTime.of(2018, monthDay.month, monthDay.dayOfMonth, 0, 0).plusDays(1),
                        RepetitionType.monthly,
                        LocalDate.MAX)

        fun create(dayOfWeek: DayOfWeek, start: String, end: String): RepeatedInterval =
                this.create(dayOfWeek, LocalTime.parse(start), LocalTime.parse(end))


        fun create(dayOfWeek: DayOfWeek, start: LocalTime, end: LocalTime): RepeatedInterval {
            val startDate = LocalDateTime.from(dayOfWeek.adjustInto(start.atDate(LocalDate.ofYearDay(2019, 1))))
            val endDate = LocalDateTime.from(dayOfWeek.adjustInto(end.atDate(LocalDate.ofYearDay(2019, 1))))
            return RepeatedInterval(-1, startDate, endDate, RepetitionType.weekly, LocalDate.MAX)
        }
    }

    override fun contains(interval: Interval): Boolean =
            this.contains(interval.start) && this.contains(interval.end)


    override fun contains(date: LocalDateTime): Boolean {
        if (this.repetitionType == RepetitionType.none) {
            return date in this.start..this.end
        }

        var s = this.start
        var e = this.end

        while (s <= date && s.toLocalDate() < this.repetitionEnd) {
            if (date in s..e) {
                return true
            }

            when (this.repetitionType) {
                RepetitionType.none -> {
                }

                RepetitionType.daily -> {
                    s = s.plusDays(1)
                    e = e.plusDays(1)
                }

                RepetitionType.weekly -> {
                    s = s.plusWeeks(1)
                    e = e.plusWeeks(1)
                }

                RepetitionType.monthly -> {
                    s = s.plusMonths(1)
                    e = e.plusMonths(1)
                }

                RepetitionType.yearly -> {
                    s = s.plusYears(1)
                    e = e.plusYears(1)
                }
            }
        }

        return false
    }

    override fun overlaps(interval: Interval): Boolean {
        if (this.repetitionType == RepetitionType.none) {
            return super.overlaps(interval)
        }

        var s = this.start
        var e = this.end

        while (s <= interval.end && s.toLocalDate() < this.repetitionEnd) {
            if (Interval(s, e).overlaps(interval)) {
                return true
            }

            when (this.repetitionType) {
                RepetitionType.none -> {
                }

                RepetitionType.daily -> {
                    s = s.plusDays(1)
                    e = e.plusDays(1)
                }

                RepetitionType.weekly -> {
                    s = s.plusWeeks(1)
                    e = e.plusWeeks(1)
                }

                RepetitionType.monthly -> {
                    s = s.plusMonths(1)
                    e = e.plusMonths(1)
                }

                RepetitionType.yearly -> {
                    s = s.plusYears(1)
                    e = e.plusYears(1)
                }
            }
        }

        return false
    }

    override fun contains(date: String): Boolean = this.contains(LocalDateTime.parse(date))
}