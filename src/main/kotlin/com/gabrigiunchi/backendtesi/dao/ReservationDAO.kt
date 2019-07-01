package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.entities.Reservation
import com.gabrigiunchi.backendtesi.model.entities.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.time.OffsetDateTime
import java.util.*

interface ReservationDAO : PagingAndSortingRepository<Reservation, Int> {
    fun findByUser(user: User, pageable: Pageable): Page<Reservation>
    fun findByIdAndUser(id: Int, user: User): Optional<Reservation>
    fun findByUserAndEndAfterAndActive(user: User, date: OffsetDateTime, active: Boolean): Collection<Reservation>
    fun findByAssetAndEndAfter(asset: Asset, date: OffsetDateTime): Collection<Reservation>
    fun findByEndAfter(date: OffsetDateTime): Collection<Reservation>
    fun findByUserAndActive(user: User, active: Boolean, pageable: Pageable): Page<Reservation>
    fun findByAsset(asset: Asset): Collection<Reservation>
    fun findByUserAndDateBetween(user: User, start: OffsetDateTime, end: OffsetDateTime): Collection<Reservation>
}