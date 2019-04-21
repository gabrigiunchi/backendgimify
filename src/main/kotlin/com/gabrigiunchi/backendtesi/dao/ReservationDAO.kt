package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ReservationDAO : CrudRepository<Reservation, Int> {

    fun findByStartAfter(date: Date): Collection<Reservation>
    fun findByEndAfter(date: Date): Collection<Reservation>
    fun findByUser(user: User): Collection<Reservation>
    fun findByAsset(asset: Asset): Collection<Reservation>
}