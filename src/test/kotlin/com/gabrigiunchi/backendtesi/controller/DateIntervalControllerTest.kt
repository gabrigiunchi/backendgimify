package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.DateIntervalDAO
import com.gabrigiunchi.backendtesi.model.dto.DateIntervalDTO
import com.gabrigiunchi.backendtesi.util.ApiUrls
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class DateIntervalControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var dateIntervalDAO: DateIntervalDAO

    private val dateIntervals = MockEntities.mockDateIntervals.toList()

    @Before
    fun clearDB() {
        this.dateIntervalDAO.deleteAll()
    }

    @Test
    fun `Should get all date intervals`() {
        this.dateIntervalDAO.saveAll(this.dateIntervals)
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.DATE_INTERVALS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(this.dateIntervals.size)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a date interval by its id`() {
        val interval = this.dateIntervalDAO.save(this.dateIntervals[0])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.DATE_INTERVALS}/${interval.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an interval if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.DATE_INTERVALS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a date interval`() {
        val intervalDTO = DateIntervalDTO(DateDecorator.of(
                "2018-10-10T10:00:00+0000").date,
                DateDecorator.of("2018-10-10T12:00:00+0000").date)

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.DATE_INTERVALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(intervalDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.start", Matchers.`is`("2018-10-10T10:00:00.000+0000")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.end", Matchers.`is`("2018-10-10T12:00:00.000+0000")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT create a date interval if the start is after the end`() {
        val intervalDTO = DateIntervalDTO(DateDecorator.of(
                "2018-10-10T16:00:00+0000").date,
                DateDecorator.of("2018-10-10T12:00:00+0000").date)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.DATE_INTERVALS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(intervalDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a date interval`() {
        val savedId = this.dateIntervalDAO.save(this.dateIntervals[0]).id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.DATE_INTERVALS}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.dateIntervalDAO.findById(savedId).isEmpty).isTrue()
    }

    @Test
    fun `Should not delete a date interval if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.DATE_INTERVALS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}

