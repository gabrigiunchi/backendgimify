package com.gabrigiunchi.backendtesi.model.dto.input

import com.gabrigiunchi.backendtesi.model.DateInterval
import com.gabrigiunchi.backendtesi.model.Schedule
import java.time.MonthDay

class TimetableDTO(
        val gymId: Int,
        val openings: Set<Schedule>,
        val closingDays: Set<DateInterval>,
        val exceptionalOpenings: Set<DateInterval>,
        val recurringExceptions: Set<MonthDay>)