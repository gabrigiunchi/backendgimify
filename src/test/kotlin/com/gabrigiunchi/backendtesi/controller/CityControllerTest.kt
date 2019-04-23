package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CityControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Test
    fun `Should get all the cities`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(CityEnum.values().size)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a city by its id`() {
        val city = this.cityDAO.findById(1).get()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/${city.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(city.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(city.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a city by its name`() {
        val city = this.cityDAO.findById(1).get()
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
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a city by name if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/by_name/ndakjsnjad")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a city`() {
        this.cityDAO.deleteAll()
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
        this.cityDAO.deleteAll()
        val city = this.cityDAO.save(City(CityEnum.MILANO))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(city)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.cityDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not create a city if its name already exist`() {
        this.cityDAO.deleteAll()
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
        this.cityDAO.deleteAll()
        val city = this.cityDAO.save(City(CityEnum.BERGAMO))
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
                .andDo(MockMvcResultHandlers.print())
    }
}