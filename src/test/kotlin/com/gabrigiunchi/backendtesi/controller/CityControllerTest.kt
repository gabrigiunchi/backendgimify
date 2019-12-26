package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.model.dto.input.CityDTOInput
import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.service.MapsService
import com.google.maps.model.LatLng
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.ZoneId

class CityControllerTest : BaseTest()
{

    @Autowired
    private lateinit var cityDAO: CityDAO

    @MockBean
    private lateinit var mockMapsService: MapsService

    @Before
    fun clearDB()
    {
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(LatLng(10.0, 10.0))
        Mockito.`when`(mockMapsService.getTimezone(LatLng(10.0, 10.0))).thenReturn(ZoneId.of("UTC"))
        this.cityDAO.deleteAll()
    }

    @Test
    fun `Should get all the cities`() {
        this.cityDAO.saveAll(MockEntities.mockCities)
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(MockEntities.mockCities.size)))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.zoneId", Matchers.`is`(city.zoneId.toString())))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a city by its name`() {
        val city = this.mockCity()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/name/${city.name}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(city.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(city.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zoneId", Matchers.`is`(city.zoneId.toString())))
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
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.CITIES}/name/$name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city $name does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a city`() {
        val city = CityDTOInput("Los Angeles")
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(city)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`("Los Angeles")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zoneId", Matchers.`is`("UTC"))) // because of mock
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.cityDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not create a city if its name already exist`() {
        val city = City(CityEnum.NEW_YORK)
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
    fun `Should not create a city if it does not exist`() {
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(null)
        val name = "dkjasndas"
        val city = CityDTOInput(name)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.CITIES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(city)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city $name not found")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should modify a city`() {
        val savedCity = this.cityDAO.save(City(CityEnum.LOS_ANGELES))
        val cityDTO = CityDTOInput("New York")
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.CITIES}/${savedCity.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(cityDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`("New York")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.zoneId", Matchers.`is`("UTC"))) // because of mock
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.cityDAO.findById(savedCity.id).get().name).isEqualTo(cityDTO.name)
    }

    @Test
    fun `Should not modify a city if it does not exist`() {
        this.cityDAO.save(City(CityEnum.LOS_ANGELES))
        val cityDTO = CityDTOInput("New York")
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.CITIES}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(cityDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not modify a city if cannot be geocoded`() {
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(null)
        val savedCity = this.cityDAO.save(City(CityEnum.LOS_ANGELES))
        val cityDTO = CityDTOInput("dasjdnasjn")
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.CITIES}/${savedCity.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(cityDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city ${cityDTO.name} not found")))
                .andDo(MockMvcResultHandlers.print())
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