package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.AssetKind
import com.gabrigiunchi.backendtesi.model.Gym
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface AssetDAO : PagingAndSortingRepository<Asset, Int> {
    override fun findAll(pageable: Pageable): Page<Asset>
    fun findByGymAndKind(gym: Gym, kind: AssetKind): Collection<Asset>
    fun findByName(name: String): Collection<Asset>
    fun findByGym(gym: Gym, pageable: Pageable): Page<Asset>
    fun findByGymAndName(gym: Gym, name: String): Optional<Asset>
    fun findByKind(kind: AssetKind, pageable: Pageable): Page<Asset>
}