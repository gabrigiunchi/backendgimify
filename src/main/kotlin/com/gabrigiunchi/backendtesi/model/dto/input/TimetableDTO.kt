package com.gabrigiunchi.backendtesi.model.dto.input

import com.gabrigiunchi.backendtesi.model.RepeatedLocalInterval

class TimetableDTO(
        val gymId: Int,
        val openings: Set<RepeatedLocalInterval>,
        val closingDays: Set<RepeatedLocalInterval>)