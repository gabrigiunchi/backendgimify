package com.gabrigiunchi.backendtesi.constants

import com.gabrigiunchi.backendtesi.model.RepeatedInterval
import com.gabrigiunchi.backendtesi.model.type.RepetitionType

object Constants {

    val holidays = setOf(
            RepeatedInterval("2018-25-12T00:00:00", "2018-12-26:00:00:00", RepetitionType.yearly)
    )
}