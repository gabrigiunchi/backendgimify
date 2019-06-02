package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.AssetKind
import org.springframework.data.repository.CrudRepository
import java.util.*

interface AssetKindDAO : CrudRepository<AssetKind, Int> {
    fun findByName(name: String): Optional<AssetKind>
}