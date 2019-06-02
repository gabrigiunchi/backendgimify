package com.gabrigiunchi.backendtesi.model

import java.time.temporal.Temporal

interface Interval<T : Temporal> {
    val start: T
    val end: T

    fun contains(instant: T): Boolean
    fun overlaps(interval: Interval<T>): Boolean
    fun contains(interval: Interval<T>): Boolean
}