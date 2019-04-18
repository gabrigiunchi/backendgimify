package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Region
import org.springframework.data.repository.CrudRepository
import java.util.*

interface RegionDAO: CrudRepository<Region, Int> {
    fun findByName(name: String): Optional<Region>
}