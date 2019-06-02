package com.gabrigiunchi.backendtesi.constants

import com.gabrigiunchi.backendtesi.model.RepeatedLocalInterval
import com.gabrigiunchi.backendtesi.model.type.RepetitionType

object Constants {

    val holidays = setOf(
            RepeatedLocalInterval("2018-12-25T00:00:00", "2018-12-26T00:00:00", RepetitionType.YEARLY)
    )
}