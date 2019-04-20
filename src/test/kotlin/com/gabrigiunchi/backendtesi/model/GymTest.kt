package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class GymTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var regionDAO: RegionDAO

    @Before
    fun clearDB() {
        this.regionDAO.deleteAll()
        this.gymDAO.deleteAll()
    }

    @Test
    fun `Should delete a region and all the gyms related to it`() {
        val region = this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))
        this.gymDAO.saveAll(listOf(
                Gym("gym1", "via1", region),
                Gym("gym2", "via2", region),
                Gym("gym3", "via3", region),
                Gym("gym4", "via4", region)))

        Assertions.assertThat(this.gymDAO.count()).isEqualTo(4)
        Assertions.assertThat(this.regionDAO.count()).isEqualTo(1)

        this.regionDAO.delete(region)
        Assertions.assertThat(this.regionDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(0)
    }
}