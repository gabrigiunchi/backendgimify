package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.DayOfWeek

class TimetableTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Before
    fun clearDB() {
        this.cityDAO.deleteAll()
        this.timetableDAO.deleteAll()
        this.gymDAO.deleteAll()
    }

    @Test
    fun `Should delete a gym and its timetable`() {
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym))
        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(1)

        this.gymDAO.delete(gym)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(0)
    }


    @Test
    fun `Should say if it contains a date`() {
        val gym = this.mockGym()
        val openings = setOf(
                Schedule(DayOfWeek.MONDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00")
                )),

                Schedule(DayOfWeek.WEDNESDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00")
                )),

                Schedule(DayOfWeek.FRIDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00")
                ))
        )
        val closingDays = setOf(
                // Monday 29 April 2019
                DateInterval(DateDecorator.createDate("2019-04-29").date, DateDecorator.createDate("2019-04-30").date)
        )
        val exceptionalOpenings = setOf(
                // Tuesday 30 April 2019
                DateInterval(DateDecorator.createDate("2019-04-30").date, DateDecorator.createDate("2019-04-31").date)
        )
        val timetable = Timetable(gym = gym, openings = openings, exceptionalOpenings = exceptionalOpenings, closingDays = closingDays)

        // Monday 22 April 2019 10:00
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-04-22T10:00:00+0000").date)).isTrue()

        // Tuesday 23 April 2019 10:00
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-04-23T10:00:00+0000").date)).isFalse()

        // Wednesday 01 May 2019 10:00
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-05-01T10:00:00+0000").date)).isTrue()

        // Monday 29 April 2019 10:00
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-04-29T10:00:00+0000").date)).isFalse()

        // Tuesday 30 April 2019 10:00
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-04-30T10:00:00+0000").date)).isTrue()
    }

    @Test
    fun `Should give priority to closing days`() {
        val gym = this.mockGym()
        val openings = setOf(
                Schedule(DayOfWeek.MONDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00")
                ))
        )
        val closingDays = setOf(
                // Monday 29 April 2019
                DateInterval(DateDecorator.createDate("2019-04-29").date, DateDecorator.createDate("2019-04-30").date)
        )
        val exceptionalOpenings = setOf(
                // Monday 29 April 2019
                DateInterval(DateDecorator.createDate("2019-04-29T08:00:00+0000").date, DateDecorator.of("2019-04-29T19:00:00+0000").date)
        )
        val timetable = Timetable(gym = gym, openings = openings, exceptionalOpenings = exceptionalOpenings, closingDays = closingDays)

        // Monday 29 April 2019 10:00
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-04-29T10:00:00+0000").date)).isFalse()
    }

    /******************************** DATE TIME_INTERVALS *************************************************************/

    @Test
    fun `Should say if it contains a date interval`() {
        val timetable = this.createTimetable()

        // Regular openings
        Assertions.assertThat(timetable.contains(
                DateInterval(
                        DateDecorator.of("2019-04-22T10:00:00+0000").date,
                        DateDecorator.of("2019-04-22T11:00:00+0000").date
                ))
        ).isTrue()

        Assertions.assertThat(timetable.contains(
                DateInterval(
                        DateDecorator.of("2019-04-22T08:00:00+0000").date,
                        DateDecorator.of("2019-04-22T12:00:00+0000").date
                ))
        ).isTrue()

        // Exceptional opening
        Assertions.assertThat(timetable.contains(
                DateInterval(
                        DateDecorator.of("2019-04-30T08:00:00+0000").date,
                        DateDecorator.of("2019-04-30T12:00:00+0000").date
                ))
        ).isTrue()
    }

    @Test
    fun `Should say if it does not contain a date interval (out of range, too early)`() {
        Assertions.assertThat(this.createTimetable().contains(
                DateInterval(
                        DateDecorator.of("2019-04-22T05:00:00+0000").date,
                        DateDecorator.of("2019-04-22T07:00:00+0000").date
                ))
        ).isFalse()
    }

    @Test
    fun `Should say if it does not contain a date interval (out of range, too late)`() {
        Assertions.assertThat(this.createTimetable().contains(
                DateInterval(
                        DateDecorator.of("2019-04-22T19:00:00+0000").date,
                        DateDecorator.of("2019-04-22T20:00:00+0000").date
                ))
        ).isFalse()
    }

    @Test
    fun `Should say if it does not contain a date interval (overlapping)`() {
        Assertions.assertThat(this.createTimetable().contains(
                DateInterval(
                        DateDecorator.of("2019-04-22T05:00:00+0000").date,
                        DateDecorator.of("2019-04-22T10:00:00+0000").date
                ))
        ).isFalse()
    }

    @Test
    fun `Should say if it does not contain a date interval (closing day)`() {
        Assertions.assertThat(this.createTimetable().contains(
                DateInterval(
                        DateDecorator.of("2019-04-29T08:00:00+0000").date,
                        DateDecorator.of("2019-04-29T12:00:00+0000").date
                ))
        ).isFalse()
    }

    @Test
    fun `Should say if it does not contain a date interval (exceptional opening but wrong time)`() {
        Assertions.assertThat(this.createTimetable().contains(
                DateInterval(
                        DateDecorator.of("2019-04-30T05:00:00+0000").date,
                        DateDecorator.of("2019-04-30T07:00:00+0000").date
                ))
        ).isFalse()
    }

    @Test
    fun `Should give priority to closing days (date interval)`() {
        val gym = this.mockGym()
        val openings = setOf(
                Schedule(DayOfWeek.MONDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00")
                ))
        )
        val closingDays = setOf(
                // Monday 29 April 2019
                DateInterval(DateDecorator.createDate("2019-04-29").date, DateDecorator.createDate("2019-04-30").date)
        )
        val exceptionalOpenings = setOf(
                // Monday 29 April 2019
                DateInterval(DateDecorator.createDate("2019-04-29T08:00:00+0000").date, DateDecorator.of("2019-04-29T19:00:00+0000").date)
        )
        val timetable = Timetable(gym = gym, openings = openings, exceptionalOpenings = exceptionalOpenings, closingDays = closingDays)

        Assertions.assertThat(timetable.contains(
                DateInterval(
                        DateDecorator.of("2019-04-30T08:00:00+0000").date,
                        DateDecorator.of("2019-04-30T12:00:00+0000").date
                ))
        ).isFalse()
    }

    /************************************ RECURRING EXCEPTIONS *******************************************************************/

    @Test
    fun `Should say if it does not contain a date interval (recurring exceptions)`() {
        val timetable = Timetable(
                gym = this.mockGym(),
                recurringExceptions = MockEntities.mockHolidays,
                closingDays = emptySet(),
                openings = MockEntities.wildcardSchedules,
                exceptionalOpenings = emptySet()
        )
        Assertions.assertThat(timetable.contains(
                DateInterval(
                        DateDecorator.of("2019-12-18T09:00:00+0000").date,
                        DateDecorator.of("2019-12-18T09:30:00+0000").date
                ))
        ).isTrue()

        Assertions.assertThat(timetable.contains(
                DateInterval(
                        DateDecorator.of("2019-12-25T09:00:00+0000").date,
                        DateDecorator.of("2019-12-25T09:30:00+0000").date
                ))
        ).isFalse()
    }

    @Test
    fun `Should say it does not contain a date if the date is one of the recurring exceptions`() {
        val timetable = Timetable(
                gym = this.mockGym(),
                recurringExceptions = MockEntities.mockHolidays,
                closingDays = emptySet(),
                openings = MockEntities.wildcardSchedules,
                exceptionalOpenings = emptySet()
        )
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-12-18T09:00:00+0000").date)).isTrue()
        Assertions.assertThat(timetable.contains(DateDecorator.of("2019-12-25T09:00:00+0000").date)).isFalse()
    }

    @Test
    fun `Should say if the recurring exceptions contain a date`() {
        val timetable = Timetable(
                gym = this.mockGym(),
                recurringExceptions = MockEntities.mockHolidays,
                closingDays = emptySet(),
                openings = MockEntities.wildcardSchedules,
                exceptionalOpenings = emptySet()
        )
        Assertions.assertThat(timetable.exceptionsContain(DateDecorator.of("2019-12-25T01:00:00+0000").date)).isTrue()
    }

    @Test
    fun `Should say if the recurring exceptions does not contain a date`() {
        val timetable = Timetable(
                gym = this.mockGym(),
                recurringExceptions = MockEntities.mockHolidays,
                closingDays = emptySet(),
                openings = MockEntities.wildcardSchedules,
                exceptionalOpenings = emptySet()
        )
        Assertions.assertThat(timetable.exceptionsContain(DateDecorator.of("2019-02-02T01:00:00+0000").date)).isFalse()
    }

    /**************************************** UTILS *************************************************************************/

    private fun createTimetable(): Timetable {
        val openings = setOf(
                Schedule(DayOfWeek.MONDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00"))
                ),

                Schedule(DayOfWeek.WEDNESDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00"))
                ),

                Schedule(DayOfWeek.FRIDAY, setOf(
                        TimeInterval("08:00", "12:00"),
                        TimeInterval("13:00", "19:00"))
                )
        )
        val closingDays = setOf(
                // Monday 29 April 2019
                DateInterval(DateDecorator.createDate("2019-04-29").date, DateDecorator.createDate("2019-04-30").date)
        )
        val exceptionalOpenings = setOf(
                // Tuesday 30 April 2019
                DateInterval(
                        DateDecorator.of("2019-04-30T08:00:00+0000").date,
                        DateDecorator.of("2019-04-30T12:00:00+0000").date
                )
        )
        return Timetable(gym = this.mockGym(), openings = openings, exceptionalOpenings = exceptionalOpenings, closingDays = closingDays)
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("Gym1", "Via 2", this.cityDAO.save(MockEntities.mockCities[0])))
    }
}