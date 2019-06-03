package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.time.RepeatedZonedInterval
import org.springframework.data.repository.PagingAndSortingRepository

interface RepeatedZonedIntervalDAO : PagingAndSortingRepository<RepeatedZonedInterval, Int>