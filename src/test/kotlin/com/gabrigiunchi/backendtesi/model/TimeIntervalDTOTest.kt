package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.TimeIntervalDTO
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.OffsetTime

class TimeIntervalDTOTest {

    @Test
    fun `Should serialize into a json` () {
        val intervalDTO = TimeIntervalDTO(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00"))
        Assertions.assertThat(intervalDTO.toJson()).isEqualTo("{\"start\":\"10:00Z\",\"end\":\"12:00Z\"}")
    }
}