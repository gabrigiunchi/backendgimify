package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import java.time.temporal.Temporal

interface RepeatedInterval<T : Temporal> : Interval<T> {

    val repetitionType: RepetitionType
    val repetitionEnd: T?
}