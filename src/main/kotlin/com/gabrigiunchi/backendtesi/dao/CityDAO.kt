package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.City
import org.springframework.data.repository.CrudRepository
import java.util.*

interface CityDAO : CrudRepository<City, Int> {
    fun findByName(name: String): Optional<City>
}