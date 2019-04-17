package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Schedule
import org.springframework.data.repository.CrudRepository

interface ScheduleDAO: CrudRepository<Schedule, Int>