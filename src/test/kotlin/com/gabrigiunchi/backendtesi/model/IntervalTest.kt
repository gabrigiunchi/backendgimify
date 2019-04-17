package com.gabrigiunchi.backendtesi.model

import org.junit.Test
import java.time.OffsetTime

class IntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should raise an exception if the star is before the end`() {
        Interval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("08:00:00+00:00"))
    }
}