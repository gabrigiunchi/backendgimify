package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.time.LocalInterval
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface LocalIntervalDAO : PagingAndSortingRepository<LocalInterval, Int> {
    override fun findAll(pageable: Pageable): Page<LocalInterval>
}