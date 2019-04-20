package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.Timetable
import org.springframework.data.repository.CrudRepository
import java.util.*

interface TimetableDAO : CrudRepository<Timetable, Int> {
    fun findByGym(gym: Gym): Optional<Timetable>
}