package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CityControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Before
    fun clearDB() {
        this.cityDAO.deleteAll()
    }

    @Test
    fun `Should get all the cities`() {
        this.cityDAO.saveAll(MockEntities.mockCities)
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(CityEnum.values().size)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a city by its id`() {
        val city = this.mockCity()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/${city.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(city.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(city.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a city by its name`() {
        val city = this.mockCity()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/by_name/${city.name}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(city.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(city.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a city by id if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a city by name if it does not exist`() {
        val name = "dbasdasd"
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/by_name/$name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city $name does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a city`() {
        val city = City(CityEnum.MILANO)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(city)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(city.name)))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.cityDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not create a city if its id already exist`() {
        val city = this.mockCity()
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(city)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.cityDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not create a city if its name already exist`() {
        val city = City(CityEnum.MILANO)
        val savedCity = this.cityDAO.save(city)

        Assertions.assertThat(city.id).isNotEqualTo(savedCity.id)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(city)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.cityDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should delete a city`() {
        val city = this.mockCity()
        Assertions.assertThat(this.cityDAO.findById(city.id).isPresent).isTrue()
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.CITIES}/${city.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.cityDAO.findById(1).isEmpty).isTrue()
    }

    @Test
    fun `Should not delete a city if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.CITIES}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    private fun mockCity(): City {
        return this.cityDAO.save(MockEntities.mockCities[0])
    }
}