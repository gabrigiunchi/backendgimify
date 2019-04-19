package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.IntervalDAO
import com.gabrigiunchi.backendtesi.dao.ScheduleDAO
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.DayOfWeek
import java.time.OffsetTime

class ScheduleTest : AbstractControllerTest() {
    @Autowired
    private lateinit var scheduleDAO: ScheduleDAO

    @Autowired
    private lateinit var intervalDAO: IntervalDAO

    private val intervals = listOf(
            Interval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00")),
            Interval(OffsetTime.parse("12:00:00+00:00"), OffsetTime.parse("14:00:00+00:00")),
            Interval(OffsetTime.parse("14:00:00+00:00"), OffsetTime.parse("16:00:00+00:00")),
            Interval(OffsetTime.parse("16:00:00+00:00"), OffsetTime.parse("18:00:00+00:00")))

    private val schedules = listOf(
            Schedule(DayOfWeek.MONDAY, this.intervals.take(2).toSet()),
            Schedule(DayOfWeek.TUESDAY, setOf(this.intervals[2], this.intervals[3])),
            Schedule(DayOfWeek.FRIDAY),
            Schedule(DayOfWeek.WEDNESDAY))

    @Before
    fun clearDB() {
        this.intervalDAO.deleteAll()
        this.scheduleDAO.deleteAll()
    }

    fun `Should create a schedule and save in cascade also the intervals`() {
        this.scheduleDAO.save(schedules[0])
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.intervalDAO.count()).isEqualTo(2)
    }

    @Test
    fun `Should delete also the intervals when deleting a schedule`() {
        val saved = this.scheduleDAO.save(schedules[0])
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.intervalDAO.count()).isEqualTo(2)

        this.scheduleDAO.delete(saved)
        Assertions.assertThat(this.intervalDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(0)
    }
}