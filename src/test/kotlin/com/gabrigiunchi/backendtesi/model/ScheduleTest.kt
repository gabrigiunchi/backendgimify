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
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case for start)`() {
        val date = DateDecorator.of("2019-04-20T08:00:00+0000").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case for end)`() {
        val date = DateDecorator.of("2019-04-20T16:00:00+0000").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case with timezone)`() {
        val date = DateDecorator.of("2019-04-20T10:00:00+0200").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(date)).isTrue()
    }

    @Test
    fun `Should say if it contains a date (edge case with more intervals)`() {
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval("08:00", "10:00"),
                TimeInterval("12:00", "14:00"),
                TimeInterval("16:00", "18:00")
        ))

        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T09:00:00+0000").date)).isTrue()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T13:00:00+0000").date)).isTrue()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T16:00:00+0000").date)).isTrue()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T17:00:00+0000").date)).isTrue()
    }

    @Test
    fun `Should say if does not contain a date`() {
        val date = DateDecorator.of("2019-04-20T07:00:00+0000").date
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(date)).isFalse()
    }

    @Test
    fun `Should say if does not contain a date (edge case with multiple intervals`() {
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(
                TimeInterval("08:00", "10:00"),
                TimeInterval("12:00", "14:00"),
                TimeInterval("16:00", "18:00")
        ))

        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T07:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T11:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T15:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-20T19:00:00+0000").date)).isFalse()
    }

    @Test
    fun `Should say if does not contain a date if the day of the week is different`() {
        val schedule = Schedule(DayOfWeek.SATURDAY, setOf(TimeInterval("00:01", "23:59")))
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-15T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-16T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-17T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-18T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-19T01:00:00+0000").date)).isFalse()
        Assertions.assertThat(schedule.contains(DateDecorator.of("2019-04-21T01:00:00+0000").date)).isFalse()
    }

    /************************************ CONTAINS A DATE INTERVAL *********************************************************/

    @Test
    fun `Should say if contains a date interval`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T12:00:00+0000").date,
                        DateDecorator.of("2019-04-21T14:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with start time)`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T08:00:00+0000").date,
                        DateDecorator.of("2019-04-21T14:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with end time)`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T12:00:00+0000").date,
                        DateDecorator.of("2019-04-21T16:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with start time and end time)`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T08:00:00+0000").date,
                        DateDecorator.of("2019-04-21T16:00:00+0000").date))
        ).isTrue()
    }

    @Test
    fun `Should say if contains a date interval (edge case with timezone)`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T10:00:00+0200").date,
                        DateDecorator.of("2019-04-21T18:00:00+0200").date))
        ).isTrue()
    }

    @Test
    fun `Should say if does not contain a date interval if the start is invalid`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T07:00:00+0000").date,
                        DateDecorator.of("2019-04-21T14:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if the end is invalid`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T12:00:00+0000").date,
                        DateDecorator.of("2019-04-21T18:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if both the start and the end are invalid`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(TimeInterval("08:00", "16:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T05:00:00+0000").date,
                        DateDecorator.of("2019-04-21T07:00:00+0000").date))
        ).isFalse()

        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T18:00:00+0000").date,
                        DateDecorator.of("2019-04-21T19:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if the interval is not within the same day`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(
                TimeInterval("08:00", "10:00"),
                TimeInterval("14:00", "18:00")
        ))

        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T09:00:00+0000").date,
                        DateDecorator.of("2019-04-22T015:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval if the interval is not the same weekday`() {
        val schedule = Schedule(DayOfWeek.MONDAY, setOf(TimeInterval("08:00", "20:00")))
        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T10:00:00+0000").date,
                        DateDecorator.of("2019-04-21T12:00:00+0000").date))
        ).isFalse()
    }

    @Test
    fun `Should say if does not contain a date interval (edge case with overlapping interval)`() {
        val schedule = Schedule(DayOfWeek.SUNDAY, setOf(
                TimeInterval("08:00", "10:00"),
                TimeInterval("14:00", "18:00")
        ))

        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T09:00:00+0000").date,
                        DateDecorator.of("2019-04-21T015:00:00+0000").date))
        ).isFalse()

        Assertions.assertThat(schedule.contains(
                DateInterval(
                        DateDecorator.of("2019-04-21T012:00:00+0000").date,
                        DateDecorator.of("2019-04-21T015:00:00+0000").date))
        ).isFalse()
    }
}
