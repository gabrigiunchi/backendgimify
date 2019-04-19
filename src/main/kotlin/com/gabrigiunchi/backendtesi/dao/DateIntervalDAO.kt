package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.DateInterval
import org.springframework.data.repository.CrudRepository

interface DateIntervalDAO: CrudRepository<DateInterval, Int>