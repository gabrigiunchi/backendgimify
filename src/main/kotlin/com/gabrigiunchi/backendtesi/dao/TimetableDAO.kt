package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.time.Timetable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface TimetableDAO : PagingAndSortingRepository<Timetable, Int> {
    override fun findAll(pageable: Pageable): Page<Timetable>
    fun findByGym(gym: Gym): Optional<Timetable>
}