package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.ScheduleDAO
import com.gabrigiunchi.backendtesi.dao.TimeIntervalDAO
import com.gabrigiunchi.backendtesi.model.TimeInterval
import com.gabrigiunchi.backendtesi.model.dto.ScheduleDTO
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.DayOfWeek
import java.time.OffsetTime

class ScheduleControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var scheduleDAO: ScheduleDAO

    @Autowired
    private lateinit var timeIntervalDAO: TimeIntervalDAO

    private val schedules = MockEntities.mockSchedules.toList()

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.dayOfWeek", Matchers.`is`(schedule.dayOfWeek.toString())))
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
        this.scheduleDAO.deleteAll()
        val intervals = setOf(
                TimeInterval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00")))

        val scheduleDTO = ScheduleDTO(DayOfWeek.WEDNESDAY, intervals)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.SCHEDULES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(scheduleDTO.toJson()))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.dayOfWeek", Matchers.`is`(scheduleDTO.dayOfWeek.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timeIntervals.length()", Matchers.`is`(scheduleDTO.timeIntervals.size)))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(1)
        val savedSchedule = this.scheduleDAO.findAll().first()


        Assertions.assertThat(savedSchedule.timeIntervals.size).isEqualTo(scheduleDTO.timeIntervals.size)
        Assertions.assertThat(savedSchedule.timeIntervals.toList()[0].start)
                .isEqualTo(scheduleDTO.timeIntervals.toList()[0].start)

        Assertions.assertThat(savedSchedule.timeIntervals.toList()[0].end)
                .isEqualTo(scheduleDTO.timeIntervals.toList()[0].end)
        Assertions.assertThat(savedSchedule.dayOfWeek).isEqualTo(scheduleDTO.dayOfWeek)
    }

    @Test
    fun `Should delete a schedule`() {
        val savedSchedule = this.scheduleDAO.save(this.schedules[0])

        Assertions.assertThat(this.scheduleDAO.findById(savedSchedule.id).isPresent).isTrue()
        savedSchedule.timeIntervals.none { this.timeIntervalDAO.findById(it.id).isEmpty }

        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.SCHEDULES}/${savedSchedule.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.scheduleDAO.findById(savedSchedule.id).isEmpty).isTrue()
        savedSchedule.timeIntervals.none { this.timeIntervalDAO.findById(it.id).isPresent }
    }

    @Test
    fun `Should not delete a schedule if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.SCHEDULES}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}