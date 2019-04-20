package com.gabrigiunchi.backendtesi.model

import org.junit.Test
import java.time.OffsetTime

class TimeIntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should raise an exception if the start is before the end`() {
        TimeInterval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("08:00:00+00:00"))
    }
}