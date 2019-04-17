package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.IntervalDAO
import com.gabrigiunchi.backendtesi.model.Interval
import com.gabrigiunchi.backendtesi.model.dto.IntervalDTO
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.OffsetTime

class IntervalControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var intervalDAO: IntervalDAO

    private val intervals = listOf(
            Interval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00")),
            Interval(OffsetTime.parse("12:00:00+00:00"), OffsetTime.parse("14:00:00+00:00")),
            Interval(OffsetTime.parse("14:00:00+00:00"), OffsetTime.parse("16:00:00+00:00")),
            Interval(OffsetTime.parse("16:00:00+00:00"), OffsetTime.parse("18:00:00+00:00")))

    @Test
    fun `Should get all intervals`() {
        this.intervalDAO.saveAll(this.intervals)
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.INTERVALS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.greaterThanOrEqualTo(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an interval by its id`() {
        val interval = this.intervalDAO.save(this.intervals[0])
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
        val intervalDTO = IntervalDTO(OffsetTime.parse("10:00Z"), OffsetTime.parse("12:00Z"))
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
        val intervalDTO = IntervalDTO(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00"))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.INTERVALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(intervalDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete an interval`() {
        val savedId = this.intervalDAO.save(this.intervals[0]).id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.INTERVALS}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.intervalDAO.findById(savedId).isEmpty).isTrue()
    }

    @Test
    fun `Should not delete an interval if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.INTERVALS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

}