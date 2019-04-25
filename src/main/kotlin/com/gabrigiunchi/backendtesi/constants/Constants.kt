package com.gabrigiunchi.backendtesi.constants

import java.time.MonthDay

object Constants {

    val holidays = setOf(
            MonthDay.of(1, 1),
            MonthDay.of(1, 6),
            MonthDay.of(4, 25),
            MonthDay.of(5, 1),
            MonthDay.of(6, 2),
            MonthDay.of(8, 15),
            MonthDay.of(9, 1),
            MonthDay.of(12, 25),
            MonthDay.of(12, 26),
            MonthDay.of(12, 31))
}