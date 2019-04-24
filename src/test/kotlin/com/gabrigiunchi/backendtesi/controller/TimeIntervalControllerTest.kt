package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.TimeIntervalDAO
import com.gabrigiunchi.backendtesi.model.dto.input.TimeIntervalDTO
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.OffsetTime

class TimeIntervalControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var timeIntervalDAO: TimeIntervalDAO

    private val timeIntervals = MockEntities.mockTimeIntervals

    @Before
    fun clearDB() {
        this.timeIntervalDAO.deleteAll()
    }

    @Test
    fun `Should get all intervals`() {
        this.timeIntervalDAO.deleteAll()
        this.timeIntervalDAO.saveAll(this.timeIntervals)
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.INTERVALS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an interval by its id`() {
        val interval = this.timeIntervalDAO.save(this.timeIntervals[0])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.INTERVALS}/${interval.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an interval if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.INTERVALS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create an interval`() {
        val intervalDTO = TimeIntervalDTO(OffsetTime.parse("10:00Z"), OffsetTime.parse("12:00Z"))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.INTERVALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(intervalDTO.toJson()))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.start", Matchers.`is`("10:00Z")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end", Matchers.`is`("12:00Z")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT create an interval if the start is after the end`() {
        val intervalDTO = TimeIntervalDTO(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00"))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.INTERVALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(intervalDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete an interval`() {
        val savedId = this.timeIntervalDAO.save(this.timeIntervals[0]).id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.INTERVALS}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timeIntervalDAO.findById(savedId).isEmpty).isTrue()
    }

    @Test
    fun `Should not delete an interval if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.INTERVALS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

}