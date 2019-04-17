package com.gabrigiunchi.backendtesi.model.dto

import com.gabrigiunchi.backendtesi.model.Interval
import java.time.DayOfWeek

class ScheduleDTO(val dayOfWeek: DayOfWeek, val intervals: Set<Interval>)