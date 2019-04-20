package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.ScheduleDAO
import com.gabrigiunchi.backendtesi.dao.TimeIntervalDAO
import com.gabrigiunchi.backendtesi.util.DateDecorator
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
    private lateinit var timeIntervalDAO: TimeIntervalDAO

    private val schedules = MockEntities.mockSchedules.toList()

    @Before
    fun clearDB() {
        this.timeIntervalDAO.deleteAll()
        this.scheduleDAO.deleteAll()
    }

    fun `Should create a schedule and save in cascade also the intervals`() {
        this.scheduleDAO.save(schedules[0])
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.timeIntervalDAO.count()).isEqualTo(2)
    }

    @Test
    fun `Should delete also the intervals when deleting a schedule`() {
        val saved = this.scheduleDAO.save(schedules[0])
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.timeIntervalDAO.count()).isEqualTo(2)

        this.scheduleDAO.delete(saved)
        Assertions.assertThat(this.timeIntervalDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(0)
    }


    @Test
    fun `Should say if it contains a date`() {
        val date = DateDecorator.of("2019-04-20T10:00:00+0000").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("16:00+00:00"))
        ))

        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case for start)`() {
        val date = DateDecorator.of("2019-04-20T08:00:00+0000").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("16:00+00:00"))
        ))

        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case for end)`() {
        val date = DateDecorator.of("2019-04-20T16:00:00+0000").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("16:00+00:00"))
        ))

        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case with timezone)`() {
        val date = DateDecorator.of("2019-04-20T10:00:00+0002").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("16:00+00:00"))
        ))

        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case with more intervals)`() {
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("10:00+00:00")),
                TimeInterval(OffsetTime.parse("12:00+00:00"), OffsetTime.parse("14:00+00:00")),
                TimeInterval(OffsetTime.parse("16:00+00:00"), OffsetTime.parse("18:00+00:00"))
        ))

        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T09:00:00+0000").date)).isTrue()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T13:00:00+0000").date)).isTrue()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T16:00:00+0000").date)).isTrue()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T17:00:00+0000").date)).isTrue()
    }

    @Test
    fun `Should say if does not contain a date`() {
        val date = DateDecorator.of("2019-04-20T07:00:00+0000").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("16:00+00:00"))
        ))

        Assertions.assertThat(schedule.contains(date)).isFalse()
    }

    @Test
    fun `Should say if does not contain a date (edge case with multiple intervals`() {
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("08:00+00:00"), OffsetTime.parse("10:00+00:00")),
                TimeInterval(OffsetTime.parse("12:00+00:00"), OffsetTime.parse("14:00+00:00")),
                TimeInterval(OffsetTime.parse("16:00+00:00"), OffsetTime.parse("18:00+00:00"))
        ))

        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T07:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T11:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T15:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T19:00:00+0000").date)).isFalse()
    }

    @Test
    fun `Should say if does not contain a date if the day of the week is different`() {
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval(OffsetTime.parse("00:01+00:00"), OffsetTime.parse("23:59+00:00"))
        ))

        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-15T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-16T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-17T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-18T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-19T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-21T01:00:00+0000").date)).isFalse()
    }
}