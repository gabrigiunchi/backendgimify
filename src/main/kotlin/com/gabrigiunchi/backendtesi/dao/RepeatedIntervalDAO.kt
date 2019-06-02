package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.RepeatedLocalInterval
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface RepeatedIntervalDAO : PagingAndSortingRepository<RepeatedLocalInterval, Int> {
    override fun findAll(pageable: Pageable): Page<RepeatedLocalInterval>
}