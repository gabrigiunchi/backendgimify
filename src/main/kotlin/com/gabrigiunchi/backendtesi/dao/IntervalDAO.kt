package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Interval
import org.springframework.data.repository.CrudRepository

interface IntervalDAO: CrudRepository<Interval, Int>