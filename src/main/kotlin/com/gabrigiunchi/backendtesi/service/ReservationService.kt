package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.exceptions.GymClosedException
import com.gabrigiunchi.backendtesi.exceptions.ReservationConflictException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.DateInterval
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.dto.ReservationDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReservationService(
        private val timetableDAO: TimetableDAO,
        private val assetDAO: AssetDAO,
        private val userDAO: UserDAO,
        private val reservationDAO: ReservationDAO) {


    fun addReservation(reservationDTO: ReservationDTO): Reservation {
        if (this.assetDAO.findById(reservationDTO.assetID).isEmpty) {
            throw ResourceNotFoundException("asset #${reservationDTO.assetID} does not exist")
        }

        if (this.userDAO.findById(reservationDTO.userID).isEmpty) {
            throw ResourceNotFoundException("user #${reservationDTO.userID} does not exist")
        }

        if (reservationDTO.start.after(reservationDTO.end)) {
            throw BadRequestException("start is after the end")
        }

        if (reservationDTO.start == reservationDTO.end) {
            throw BadRequestException("start cannot be equals to the end")
        }

        val asset = this.assetDAO.findById(reservationDTO.assetID).get()

        if (!this.isReservationDurationValid(asset, reservationDTO.start, reservationDTO.end)) {
            throw BadRequestException("reservation duration exceeds maximum (max=${asset.kind.maxReservationTime} minutes)")
        }

        if (!this.isGymOpen(asset, reservationDTO.start, reservationDTO.end)) {
            throw GymClosedException()
        }

        if (!this.isReservationIntervalValid(asset, reservationDTO.start, reservationDTO.end)) {
            throw ReservationConflictException()
        }

        return this.reservationDAO.save(
                Reservation(
                        asset,
                        this.userDAO.findById(reservationDTO.userID).get(),
                        reservationDTO.start,
                        reservationDTO.end
                )
        )
    }

    fun isGymOpen(asset: Asset, start: Date, end: Date): Boolean {
        val timetable = this.timetableDAO.findByGym(asset.gym)
        return timetable.isPresent && timetable.get().contains(DateInterval(start, end))
    }

    /**
     * Returns true if there are no other reservations for the given asset in the given interval
     */
    fun isReservationIntervalValid(asset: Asset, start: Date, end: Date): Boolean {
        val reservations = this.reservationDAO.findByAssetAndEndAfter(asset, start)
        return reservations.none { DateInterval(it.start, it.end).overlaps(DateInterval(start, end)) }
    }

    fun isReservationDurationValid(asset: Asset, start: Date, end: Date): Boolean {
        return DateDecorator.of(start).plusMinutes(asset.kind.maxReservationTime).date >= end
    }
}