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
        val repetitionEnd: LocalDateTime?
) : Interval(id, start, end) {

    constructor(start: String, end: String) : this(start, end, RepetitionType.none)

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
            return RepeatedInterval(-1, startDate, endDate, RepetitionType.weekly, null)
        }
    }

    override fun contains(interval: Interval): Boolean =
            this.contains(interval.start) && this.contains(interval.end)

    override fun contains(date: LocalDateTime): Boolean {
        return if (this.isBeyondRepetitionEnd(date)) false
        else
            when (this.repetitionType) {
                RepetitionType.none -> date in this.start..this.end
                RepetitionType.daily -> date.toLocalTime() in this.start.toLocalTime()..this.end.toLocalTime()
                RepetitionType.weekly -> {
                    date.dayOfWeek == this.start.dayOfWeek && date.toLocalTime() in this.start.toLocalTime()..this.end.toLocalTime()
                }

                RepetitionType.monthly -> {
                    date.dayOfMonth == this.start.dayOfMonth && date.toLocalTime() in this.start.toLocalTime()..this.end.toLocalTime()
                }

                RepetitionType.yearly -> {
                    date.dayOfMonth == this.start.dayOfMonth &&
                            date.month == this.start.month &&
                            date.toLocalTime() in this.start.toLocalTime()..this.end.toLocalTime()

                }
            }
    }

    override fun contains(date: String): Boolean = this.contains(LocalDateTime.parse(date))

    private fun isBeyondRepetitionEnd(date: LocalDateTime) = this.repetitionEnd != null && date >= this.repetitionEnd
}