package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.LocalInterval
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface IntervalDAO : PagingAndSortingRepository<LocalInterval, Int> {
    override fun findAll(pageable: Pageable): Page<LocalInterval>
}