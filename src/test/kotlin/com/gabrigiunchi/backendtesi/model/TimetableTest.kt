package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class TimetableTest: AbstractControllerTest() {

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

    private fun createGym(): Gym {
        return this.gymDAO.save(Gym("Gym1", "Via 2", this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))))
    }
}