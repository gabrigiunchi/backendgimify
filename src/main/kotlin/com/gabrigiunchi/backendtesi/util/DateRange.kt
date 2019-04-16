package com.gabrigiunchi.backendtesi.util

import java.util.*

class DateRange(val start: Date, val end: Date) {

    fun contains(date: Date): Boolean {
        return date in this.start..this.end
    }
}