package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface ReservationDAO : PagingAndSortingRepository<Reservation, Int> {
    override fun findAll(pageable: Pageable): Page<Reservation>
    fun findByAssetAndEndBetween(asset: Asset, start: Date, end: Date): Collection<Reservation>
    fun findByUserAndEndAfter(user: User, date: Date): Collection<Reservation>
    fun findByAssetAndEndAfter(asset: Asset, date: Date): Collection<Reservation>
    fun findByEndAfter(date: Date): Collection<Reservation>
    fun findByUser(user: User): Collection<Reservation>
    fun findByAsset(asset: Asset): Collection<Reservation>
}