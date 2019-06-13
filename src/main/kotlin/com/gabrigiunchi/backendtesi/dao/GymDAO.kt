package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface GymDAO : PagingAndSortingRepository<Gym, Int> {
    override fun findAll(pageable: Pageable): Page<Gym>
    fun findByName(name: String): Optional<Gym>
    fun findByCity(city: City): List<Gym>
}