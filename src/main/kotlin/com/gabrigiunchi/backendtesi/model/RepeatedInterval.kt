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
        val repetitionEnd: LocalDateTime
) : Interval(id, start, end) {

    constructor(start: String, end: String) : this(start, end, RepetitionType.none)

    constructor(start: String, end: String, repetitionType: RepetitionType) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, LocalDateTime.MAX)

    constructor(start: String, end: String, repetitionType: RepetitionType, repetitionEnd: String) :
            this(-1, LocalDateTime.parse(start), LocalDateTime.parse(end), repetitionType, LocalDateTime.parse(repetitionEnd))


    companion object {
        fun create(dayOfWeek: DayOfWeek, start: String, end: String): RepeatedInterval =
                this.create(dayOfWeek, LocalTime.parse(start), LocalTime.parse(end))


        fun create(dayOfWeek: DayOfWeek, start: LocalTime, end: LocalTime): RepeatedInterval {
            val startDate = LocalDateTime.from(dayOfWeek.adjustInto(start.atDate(LocalDate.ofYearDay(2019, 1))))
            val endDate = LocalDateTime.from(dayOfWeek.adjustInto(end.atDate(LocalDate.ofYearDay(2019, 1))))
            return RepeatedInterval(-1, startDate, endDate, RepetitionType.weekly, LocalDateTime.MAX)
        }
    }

    override fun contains(date: OffsetDateTime, zoneId: ZoneId): Boolean {
        return if (date >= this.repetitionEnd.atZone(zoneId).toOffsetDateTime() ||
                !this.contains(date.atZoneSameInstant(zoneId).toOffsetDateTime().toOffsetTime())) false
        else
            when (this.repetitionType) {
                RepetitionType.none -> super.contains(date, zoneId)
                RepetitionType.daily -> true
                RepetitionType.weekly -> {
                    date.atZoneSameInstant(zoneId).dayOfWeek == this.start.atZone(zoneId).dayOfWeek
                }

                RepetitionType.monthly -> {
                    date.atZoneSameInstant(zoneId).dayOfMonth == this.start.atZone(zoneId).dayOfMonth
                }

                RepetitionType.yearly -> {
                    val s = this.start.atZone(zoneId)
                    val d = date.atZoneSameInstant(zoneId)
                    d.dayOfMonth == s.dayOfMonth && d.month == s.month
                }
            }
    }

    override fun contains(zonedInterval: ZonedInterval, zoneId: ZoneId): Boolean =
            this.contains(zonedInterval.start, zoneId) && this.contains(zonedInterval.end, zoneId)

    override fun contains(interval: Interval): Boolean =
            this.contains(interval.start) && this.contains(interval.end)

    override fun contains(date: LocalDateTime): Boolean {
        return if (date >= this.repetitionEnd) false
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

    private fun contains(offsetTime: OffsetTime): Boolean =
            offsetTime in this.start.toLocalTime().atOffset(offsetTime.offset)..this.end.toLocalTime().atOffset(offsetTime.offset)
}