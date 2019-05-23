package com.gabrigiunchi.backendtesi.model.dto.input

import com.gabrigiunchi.backendtesi.model.RepeatedInterval

class TimetableDTO(
        val gymId: Int,
        val openings: Set<RepeatedInterval>,
        val closingDays: Set<RepeatedInterval>)