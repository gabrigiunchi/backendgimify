package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.AssetKind
import com.gabrigiunchi.backendtesi.model.Gym
import org.springframework.data.repository.CrudRepository

interface AssetDAO : CrudRepository<Asset, Int> {
    fun findByName(name: String): List<Asset>
    fun findByGym(gym: Gym): List<Asset>
    fun findByKind(kind: AssetKind): List<Asset>
}