package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.DateTimeInterval
import org.springframework.data.repository.CrudRepository

interface DateTimeIntervalDAO: CrudRepository<DateTimeInterval, Int>