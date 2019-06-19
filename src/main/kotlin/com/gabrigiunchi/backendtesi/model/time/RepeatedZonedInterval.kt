package com.gabrigiunchi.backendtesi.model.time

import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import java.time.*
import javax.persistence.Entity

@Entity
class RepeatedZonedInterval(
        id: Int,
        start: OffsetDateTime,
        end: OffsetDateTime,
        override val repetitionType: RepetitionType,
        override val repetitionEnd: OffsetDateTime?
) : ZonedInterval(id, start, end), RepeatedInterval<OffsetDateTime> {

    constructor(start: String, end: String) : this(start, end, RepetitionType.NONE)

    constructor(start: String, end: String, repetitionType: RepetitionType) :
            this(-1, OffsetDateTime.parse(start), OffsetDateTime.parse(end), repetitionType, null)

    constructor(start: String, end: String, repetitionType: RepetitionType, repetitionEnd: String) :
            this(-1, OffsetDateTime.parse(start), OffsetDateTime.parse(end), repetitionType, OffsetDateTime.parse(repetitionEnd))

    companion object {
        fun create(dayOfWeek: DayOfWeek, start: LocalTime, end: LocalTime, zoneId: ZoneId): RepeatedZonedInterval {
            val startDate =
                    LocalDateTime
                            .from(dayOfWeek.adjustInto(start.atDate(LocalDate.ofYearDay(2019, 1))))
                            .atZone(zoneId)
                            .toOffsetDateTime()
            val endDate = LocalDateTime
                    .from(dayOfWeek.adjustInto(end.atDate(LocalDate.ofYearDay(2019, 1))))
                    .atZone(zoneId)
                    .toOffsetDateTime()
            return RepeatedZonedInterval(-1, startDate, endDate, RepetitionType.WEEKLY, null)
        }
    }


    override fun contains(interval: Interval<OffsetDateTime>): Boolean {
        var s = this.start
        var e = this.end

        while (s <= interval.end && !this.isBeyondRepetitionEnd(interval.end)) {
            if (ZonedInterval(s, e).contains(interval)) {
                return true
            }

            when (this.repetitionType) {
                RepetitionType.NONE -> return false
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

    override fun contains(instant: OffsetDateTime): Boolean {
        if (this.repetitionType == RepetitionType.NONE) {
            return super.contains(instant)
        }

        var s = this.start
        var e = this.end

        while (s.toInstant() <= instant.toInstant() && !this.isBeyondRepetitionEnd(instant)) {
            if (ZonedInterval(s, e).contains(instant)) {
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

    override fun overlaps(interval: Interval<OffsetDateTime>): Boolean {
        if (this.repetitionType == RepetitionType.NONE) {
            return super.overlaps(interval)
        }

        var s = this.start
        var e = this.end

        while (s.toInstant() <= interval.end.toInstant() && !this.isBeyondRepetitionEnd(interval.start)) {
            if (ZonedInterval(s, e).overlaps(interval)) {
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

    fun contains(date: String): Boolean = this.contains(OffsetDateTime.parse(date))

    private fun isBeyondRepetitionEnd(date: OffsetDateTime) = this.repetitionEnd != null && date >= this.repetitionEnd
}