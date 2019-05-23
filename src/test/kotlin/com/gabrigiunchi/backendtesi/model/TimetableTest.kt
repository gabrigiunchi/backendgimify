package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
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
    fun `Should say if it contains a date interval`() {
        val timetable = this.createTimetable()
        Assertions.assertThat(timetable.contains(Interval("2019-04-22T10:00:00", "2019-04-22T11:00:00"))).isTrue()
        Assertions.assertThat(timetable.contains(Interval("2019-04-22T08:00:00", "2019-04-22T12:00:00"))).isTrue()
    }

    @Test
    fun `Should say if it does not contain a date interval`() {
        Assertions.assertThat(
                this.createTimetable().contains(RepeatedInterval("2019-04-22T05:00:00", "2019-04-22T07:00:00"))
        ).isFalse()

        Assertions.assertThat(
                this.createTimetable().contains(RepeatedInterval("2019-04-22T19:00:00", "2019-04-22T20:00:00"))
        ).isFalse()

        Assertions.assertThat(
                this.createTimetable().contains(RepeatedInterval("2019-04-22T05:00:00", "2019-04-22T10:00:00"))
        ).isFalse()

        Assertions.assertThat(
                this.createTimetable().contains(RepeatedInterval("2019-04-29T08:00:00", "2019-04-29T12:00:00"))
        ).isFalse()
    }

    /**************************************** UTILS *************************************************************************/

    private fun createTimetable(): Timetable {
        val gym = this.mockGym()
        val openings = setOf(
                RepeatedInterval.create(DayOfWeek.MONDAY, "08:00", "12:00"),
                RepeatedInterval.create(DayOfWeek.MONDAY, "13:00", "19:00"),
                RepeatedInterval.create(DayOfWeek.WEDNESDAY, "08:00", "12:00"),
                RepeatedInterval.create(DayOfWeek.WEDNESDAY, "13:00", "19:00"),
                RepeatedInterval.create(DayOfWeek.FRIDAY, "08:00", "12:00"),
                RepeatedInterval.create(DayOfWeek.FRIDAY, "13:00", "19:00")
        )
        val closingDays = setOf(
                // Monday 29 April 2019
                RepeatedInterval("2019-04-29T00:00:00", "2019-04-30T00:00:00")
        )
        return Timetable(gym = gym, openings = openings, closingDays = closingDays)
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("Gym1", "Via 2", this.cityDAO.save(MockEntities.mockCities[0])))
    }
}