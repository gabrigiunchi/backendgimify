package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.IntervalDAO
import com.gabrigiunchi.backendtesi.dao.ScheduleDAO
import com.gabrigiunchi.backendtesi.model.Interval
import com.gabrigiunchi.backendtesi.model.Schedule
import com.gabrigiunchi.backendtesi.model.dto.ScheduleDTO
import com.gabrigiunchi.backendtesi.util.ApiUrls
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.DayOfWeek

class ScheduleControllerTest: AbstractControllerTest() {

    @Autowired
    private lateinit var scheduleDAO: ScheduleDAO

    @Autowired
    private lateinit var intervalDAO: IntervalDAO

    private val intervals = listOf(
            Interval(DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
            Interval(DateDecorator.of("2019-01-01T10:00:00+0000").date, DateDecorator.of("2019-01-01T12:00:00+0000").date),
            Interval(DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
            Interval(DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
    )

    private val schedules = listOf(
            Schedule(DayOfWeek.MONDAY, this.intervals.take(2).toSet()),
            Schedule(DayOfWeek.TUESDAY, setOf(this.intervals[0], this.intervals[2])),
            Schedule(DayOfWeek.FRIDAY),
            Schedule(DayOfWeek.WEDNESDAY))

    @Test
    fun `Should get all schedules`() {
        this.scheduleDAO.saveAll(this.schedules)
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.SCHEDULES)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.greaterThanOrEqualTo(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a schedule by its id`() {
        val schedule = this.scheduleDAO.save(this.schedules[0])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.SCHEDULES}/${schedule.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a schedule if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.SCHEDULES}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a schedule`() {
        this.scheduleDAO.saveAll(this.schedules)
        val interval = Interval(DateDecorator.now().date, DateDecorator.now().plusMinutes(120).date)
        val scheduleDTO = ScheduleDTO(DayOfWeek.WEDNESDAY, setOf(interval))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.SCHEDULES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(scheduleDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a schedule`() {
        val savedSchedule = this.scheduleDAO.save(this.schedules[0])

        Assertions.assertThat(this.scheduleDAO.findById(savedSchedule.id).isPresent).isTrue()
        savedSchedule.intervals.none { this.intervalDAO.findById(it.id).isEmpty }

        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.SCHEDULES}/${savedSchedule.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.scheduleDAO.findById(savedSchedule.id).isEmpty).isTrue()
        savedSchedule.intervals.none { this.intervalDAO.findById(it.id).isPresent }
    }

    @Test
    fun `Should not delete a schedule if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.SCHEDULES}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}