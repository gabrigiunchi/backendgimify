package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Schedule
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository

interface ScheduleDAO : PagingAndSortingRepository<Schedule, Int> {
    override fun findAll(pageable: Pageable): Page<Schedule>
}