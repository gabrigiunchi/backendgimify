package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.ScheduleDTO
import org.junit.Test
import java.time.DayOfWeek
import java.time.OffsetTime

class ScheduleDTOTest {

    @Test
    fun `Should serialize into a json` () {
        val intervals = setOf(
                Interval(OffsetTime.parse("10:00Z"), OffsetTime.parse("12:00Z"))
        )
        val scheduleDTO = ScheduleDTO(DayOfWeek.WEDNESDAY, intervals)
        System.out.println(scheduleDTO)
    }
}