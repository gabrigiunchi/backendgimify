package com.gabrigiunchi.backendtesi.model.dto.input

import com.gabrigiunchi.backendtesi.model.TimeInterval
import java.time.DayOfWeek

class ScheduleDTO(val dayOfWeek: DayOfWeek, val timeIntervals: Set<TimeInterval>)