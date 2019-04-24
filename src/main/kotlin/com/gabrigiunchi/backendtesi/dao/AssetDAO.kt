package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.AssetKind
import com.gabrigiunchi.backendtesi.model.Gym
import org.springframework.data.repository.CrudRepository

interface AssetDAO : CrudRepository<Asset, Int> {
    fun findByGymAndKind(gym: Gym, kind: AssetKind): Collection<Asset>
    fun findByName(name: String): Collection<Asset>
    fun findByGym(gym: Gym): Collection<Asset>
    fun findByKind(kind: AssetKind): Collection<Asset>
}