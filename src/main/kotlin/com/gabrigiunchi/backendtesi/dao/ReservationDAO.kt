package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.time.LocalDateTime
import java.util.*

interface ReservationDAO : PagingAndSortingRepository<Reservation, Int> {
    override fun findAll(pageable: Pageable): Page<Reservation>
    fun findByIdAndUser(id: Int, user: User): Optional<Reservation>
    fun findByAssetAndEndBetween(asset: Asset, start: LocalDateTime, end: LocalDateTime): Collection<Reservation>
    fun findByUserAndEndAfter(user: User, date: LocalDateTime): Collection<Reservation>
    fun findByAssetAndEndAfter(asset: Asset, date: LocalDateTime): Collection<Reservation>
    fun findByEndAfter(date: LocalDateTime): Collection<Reservation>
    fun findByUser(user: User, pageable: Pageable): Page<Reservation>
    fun findByAsset(asset: Asset): Collection<Reservation>
}