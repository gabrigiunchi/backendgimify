package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.IntervalDAO
import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.dao.ScheduleDAO
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.DayOfWeek
import java.time.OffsetTime

class GymTest: AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var regionDAO: RegionDAO

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

    var region = Region(RegionEnum.ABRUZZO)

    @Before
    fun clearDB() {
        this.gymDAO.deleteAll()
        this.regionDAO.deleteAll()
        this.intervalDAO.deleteAll()
        this.scheduleDAO.deleteAll()
        this.region = this.regionDAO.save(Region(RegionEnum.ABRUZZO))
    }

    @Test
    fun `Should create a gym with opening`() {
        val gym = Gym("gym dnjsnjdaj", "address", region, this.schedules.take(2))
        this.gymDAO.save(gym)
        Assertions.assertThat(this.intervalDAO.count()).isEqualTo(4)
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(2)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should delete also the openings when deleting a gym`() {
        val region = this.regionDAO.save(Region(RegionEnum.ABRUZZO))
        val gym = Gym("gym dnjsnjdaj", "Via Pacchioni 43 Forli", region, this.schedules.take(2))
        val savedGym = this.gymDAO.save(gym)
        this.gymDAO.delete(savedGym)
        Assertions.assertThat(this.intervalDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(0)
    }
}