package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Interval
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface IntervalDAO : PagingAndSortingRepository<Interval, Int> {
    override fun findAll(pageable: Pageable): Page<Interval>
}