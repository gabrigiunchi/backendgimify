package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class GymTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Before
    fun clearDB() {
        this.cityDAO.deleteAll()
        this.gymDAO.deleteAll()
    }

    @Test
    fun `Should delete a city and all the gyms related to it`() {
        val city = this.cityDAO.save(City(CityEnum.MIAMI))
        this.gymDAO.saveAll(listOf(
                Gym("gym1", "via1", city),
                Gym("gym2", "via2", city),
                Gym("gym3", "via3", city),
                Gym("gym4", "via4", city)))

        Assertions.assertThat(this.gymDAO.count()).isEqualTo(4)
        Assertions.assertThat(this.cityDAO.count()).isEqualTo(1)

        this.cityDAO.delete(city)
        Assertions.assertThat(this.cityDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(0)
    }
}