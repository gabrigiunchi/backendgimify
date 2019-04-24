package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.TimeInterval
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface TimeIntervalDAO : PagingAndSortingRepository<TimeInterval, Int> {
    override fun findAll(pageable: Pageable): Page<TimeInterval>
}