package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity

@Entity
class RepeatedInterval(
        id: Int,
        start: LocalDateTime,
        end: LocalDateTime,
        val repetitionType: RepetitionType,
        val repetitionEnd: LocalDate
) : Interval(id, start, end) {

    constructor(start: String, end: String, repetitionType: RepetitionType) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, LocalDate.MAX)

    constructor(start: String, end: String, repetitionType: RepetitionType, repetitionEnd: String) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, LocalDate.parse(repetitionEnd))

    override fun contains(date: LocalDateTime): Boolean {
        var s = this.start
        var e = this.end

        while (s <= date && s.toLocalDate() < this.repetitionEnd) {
            if (date in s..e) {
                return true
            }

            when (this.repetitionType) {
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
        var s = this.start
        var e = this.end

        while (s <= interval.end && s.toLocalDate() < this.repetitionEnd) {
            if (Interval(s, e).overlaps(interval)) {
                return true
            }

            when (this.repetitionType) {
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