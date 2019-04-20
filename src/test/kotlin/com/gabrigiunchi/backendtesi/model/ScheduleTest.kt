package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.ScheduleDAO
import com.gabrigiunchi.backendtesi.dao.TimeIntervalDAO
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ScheduleTest : AbstractControllerTest() {
    @Autowired
    private lateinit var scheduleDAO: ScheduleDAO

    @Autowired
    private lateinit var timeIntervalDAO: TimeIntervalDAO

    private val schedules = MockEntities.mockSchedules.toList()

    @Before
    fun clearDB() {
        this.timeIntervalDAO.deleteAll()
        this.scheduleDAO.deleteAll()
    }

    fun `Should create a schedule and save in cascade also the intervals`() {
        this.scheduleDAO.save(schedules[0])
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.timeIntervalDAO.count()).isEqualTo(2)
    }

    @Test
    fun `Should delete also the intervals when deleting a schedule`() {
        val saved = this.scheduleDAO.save(schedules[0])
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.timeIntervalDAO.count()).isEqualTo(2)

        this.scheduleDAO.delete(saved)
        Assertions.assertThat(this.timeIntervalDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.scheduleDAO.count()).isEqualTo(0)
    }
}