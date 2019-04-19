package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.TimeInterval
import org.springframework.data.repository.CrudRepository

interface TimeIntervalDAO: CrudRepository<TimeInterval, Int>