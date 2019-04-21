package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
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
    private lateinit var regionDAO: RegionDAO

    @Before
    fun clearDB() {
        this.regionDAO.deleteAll()
        this.timetableDAO.deleteAll()
        this.gymDAO.deleteAll()
    }

    @Test
    fun `Should delete a gym and its timetable`() {
        val gym = this.createGym()
        this.timetableDAO.save(Timetable(gym))
        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(1)

        this.gymDAO.delete(gym)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(0)
    }


    @Test
    fun `Should say if it contains a date`() {
        val gym = this.createGym()
        val openings = setOf(
                Schedule(DayOfWeek.MONDAY, setOf(
                        TimeInterval("08:00+00:00", "12:00+00:00"),
                        TimeInterval("13:00+00:00", "19:00+00:00")
                )),

                Schedule(DayOfWeek.WEDNESDAY, setOf(
                        TimeInterval("08:00+00:00", "12:00+00:00"),
                        TimeInterval("13:00+00:00", "19:00+00:00")
                )),

                Schedule(DayOfWeek.FRIDAY, setOf(
                        TimeInterval("08:00+00:00", "12:00+00:00"),
                        TimeInterval("13:00+00:00", "19:00+00:00")
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
        val timetable = Timetable(gym = gym, openings = openings, openingExceptions = exceptionalOpenings, closingDays = closingDays)

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

    private fun createGym(): Gym {
        return this.gymDAO.save(Gym("Gym1", "Via 2", this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))))
    }
}