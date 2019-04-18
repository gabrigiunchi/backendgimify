package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.model.Region
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class RegionControllerTest: AbstractControllerTest() {

    @Autowired
    private lateinit var regionDAO: RegionDAO

    @Test
    fun `Should get all regions`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.REGIONS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(20)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a region by its id`() {
        val region = this.regionDAO.findById(1).get()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.REGIONS}/${region.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(region.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(region.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a region by its name`() {
        val region = this.regionDAO.findById(1).get()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.REGIONS}/by_name/${region.name}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(region.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(region.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a region if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.REGIONS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a region`() {
        this.regionDAO.deleteAll()
        val region = Region(RegionEnum.ABRUZZO)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.REGIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(region)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(region.name)))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.regionDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not create a region if its id already exist`() {
        this.regionDAO.deleteAll()
        val region = this.regionDAO.save(Region(RegionEnum.ABRUZZO))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.REGIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(region)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.regionDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should not create a region if its name already exist`() {
        this.regionDAO.deleteAll()
        val region = Region(RegionEnum.ABRUZZO)
        val savedRegion = this.regionDAO.save(region)

        Assertions.assertThat(region.id).isNotEqualTo(savedRegion.id)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.REGIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(region)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.regionDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should delete a region`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.REGIONS}/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.regionDAO.findById(1).isEmpty).isTrue()
        Assertions.assertThat(this.regionDAO.count()).isEqualTo(19)
    }

    @Test
    fun `Should not delete a region if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.REGIONS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}