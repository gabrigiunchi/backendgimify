package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.DateInterval
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface DateIntervalDAO : PagingAndSortingRepository<DateInterval, Int> {
    override fun findAll(pageable: Pageable): Page<DateInterval>
}