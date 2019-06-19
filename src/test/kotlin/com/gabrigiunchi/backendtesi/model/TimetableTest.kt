package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.time.RepeatedLocalInterval
import com.gabrigiunchi.backendtesi.model.time.Timetable
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.DayOfWeek
import java.time.OffsetDateTime

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
    fun `Should say if it is open at a zoned date`() {
        val timetable = this.createTimetable()
        Assertions.assertThat(timetable.isOpenAt(OffsetDateTime.parse("2019-06-03T08:00:00+00:00"))).isTrue()
        Assertions.assertThat(timetable.isOpenAt(OffsetDateTime.parse("2019-06-03T12:00:00+00:00"))).isTrue()
        Assertions.assertThat(timetable.isOpenAt(OffsetDateTime.parse("2019-06-03T06:00:00-02:00"))).isTrue()

        Assertions.assertThat(timetable.isOpenAt(OffsetDateTime.parse("2019-06-04T08:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isOpenAt(OffsetDateTime.parse("2019-06-03T07:59:59+00:00"))).isFalse()
        Assertions.assertThat(timetable.isOpenAt(OffsetDateTime.parse("2019-06-03T12:00:01+00:00"))).isFalse()
        Assertions.assertThat(timetable.isOpenAt(OffsetDateTime.parse("2019-06-03T08:00:00+02:00"))).isFalse()
    }

    @Test
    fun `Should say if it is open at a zoned interval`() {
        val timetable = this.createTimetable()
        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2019-04-22T10:00:00+00:00", "2019-04-22T11:00:00+00:00"))).isTrue()
        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2019-04-22T08:00:00+00:00", "2019-04-22T12:00:00+00:00"))).isTrue()
        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2019-04-22T06:00:00-02:00", "2019-04-22T14:00:00+02:00"))).isTrue()

        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2019-04-22T05:00:00+00:00", "2019-04-22T07:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2019-04-22T19:00:00+00:00", "2019-04-22T20:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2019-04-22T05:00:00+00:00", "2019-04-22T10:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2020-12-25T08:00:00+00:00", "2020-12-25T12:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isOpenAt(ZonedInterval("2019-04-22T06:00:00+02:00", "2019-04-22T14:00:00+02:00"))).isFalse()
    }

    @Test
    fun `Should say if it is closed at a zoned date`() {
        val timetable = this.createTimetable()
        Assertions.assertThat(timetable.isClosedAt(OffsetDateTime.parse("2019-12-25T00:00:00+00:00"))).isTrue()
        Assertions.assertThat(timetable.isClosedAt(OffsetDateTime.parse("2019-12-25T23:59:59+00:00"))).isTrue()
        Assertions.assertThat(timetable.isClosedAt(OffsetDateTime.parse("2019-12-26T01:00:00+02:00"))).isTrue()

        Assertions.assertThat(timetable.isClosedAt(OffsetDateTime.parse("2019-12-24T23:59:59+00:00"))).isFalse()
        Assertions.assertThat(timetable.isClosedAt(OffsetDateTime.parse("2019-12-26T00:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isClosedAt(OffsetDateTime.parse("2019-12-25T00:00:00+02:00"))).isFalse()
    }

    @Test
    fun `Should say if it is closed at a zoned interval`() {
        val timetable = this.createTimetable()
        Assertions.assertThat(timetable.isClosedAt(ZonedInterval("2019-12-24T10:00:00+00:00", "2019-12-25T00:00:01+00:00"))).isTrue()
        Assertions.assertThat(timetable.isClosedAt(ZonedInterval("2019-12-25T08:00:00+00:00", "2019-12-25T12:00:00+00:00"))).isTrue()
        Assertions.assertThat(timetable.isClosedAt(ZonedInterval("2019-12-24T23:00:00-02:00", "2019-12-26T01:00:00+02:00"))).isTrue()

        Assertions.assertThat(timetable.isClosedAt(ZonedInterval("2019-12-24T10:00:00+00:00", "2019-12-24T12:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isClosedAt(ZonedInterval("2019-12-26T08:00:00+00:00", "2019-12-26T12:00:00+00:00"))).isFalse()
        Assertions.assertThat(timetable.isClosedAt(ZonedInterval("2019-12-25T01:00:00+03:00", "2019-12-25T01:00:00+02:00"))).isFalse()
    }

    /**************************************** UTILS *************************************************************************/

    private fun createTimetable(): Timetable {
        val gym = this.mockGym()
        val openings = setOf(
                RepeatedLocalInterval.create(DayOfWeek.MONDAY, "08:00", "12:00"),
                RepeatedLocalInterval.create(DayOfWeek.MONDAY, "13:00", "19:00"),
                RepeatedLocalInterval.create(DayOfWeek.WEDNESDAY, "08:00", "12:00"),
                RepeatedLocalInterval.create(DayOfWeek.WEDNESDAY, "13:00", "19:00"),
                RepeatedLocalInterval.create(DayOfWeek.FRIDAY, "08:00", "12:00"),
                RepeatedLocalInterval.create(DayOfWeek.FRIDAY, "13:00", "19:00")
        )
        val closingDays = setOf(
                // Christmas
                RepeatedLocalInterval("2019-12-25T00:00:00", "2019-12-25T23:59:59", RepetitionType.YEARLY),
                RepeatedLocalInterval("2019-01-01T00:00:00", "2019-01-01T23:59:59", RepetitionType.YEARLY)
        )
        return Timetable(gym = gym, openings = openings, closingDays = closingDays)
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("Gym1", "Via 2", this.cityDAO.save(MockEntities.mockCities[0])))
    }
}