package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.dto.ReservationDTO
import org.springframework.stereotype.Service

@Service
class ReservationService(
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

        return this.reservationDAO.save(
                Reservation(
                        this.assetDAO.findById(reservationDTO.assetID).get(),
                        this.userDAO.findById(reservationDTO.userID).get(),
                        reservationDTO.start,
                        reservationDTO.end
                )
        )
    }
}