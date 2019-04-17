package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.junit.Test
import java.lang.IllegalArgumentException

class IntervalTest {

    @Test(expected = IllegalArgumentException::class)
    fun `Should raise an exception if the star is before the end`() {
        Interval(DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T09:59:59+0000").date)
    }
}