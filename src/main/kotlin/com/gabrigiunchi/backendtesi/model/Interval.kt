package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.model.dto.IntervalDTO
import java.lang.IllegalArgumentException
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Interval(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Int,
        val start: Date,
        val end: Date
) {
    constructor(start: Date, end: Date): this(-1, start, end)
    constructor(intervalDTO: IntervalDTO): this(-1, intervalDTO.start, intervalDTO.end)

    init {
        if (this.start.after(this.end)) {
            throw IllegalArgumentException("start is after the end")
        }
    }
}