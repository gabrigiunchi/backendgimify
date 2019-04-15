package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.User
import org.springframework.data.repository.CrudRepository

interface ReservationDAO: CrudRepository<Reservation, Int> {

    fun findByUser(user: User): Collection<Reservation>
    fun findByAsset(asset: Asset): Collection<Reservation>
}