package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.data.repository.PagingAndSortingRepository

interface ZonedIntervalDAO : PagingAndSortingRepository<ZonedInterval, Int>