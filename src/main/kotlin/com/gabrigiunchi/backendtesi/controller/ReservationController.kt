package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.dto.ReservationDTO
import com.gabrigiunchi.backendtesi.service.ReservationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
        private val reservationService: ReservationService,
        private val assetDAO: AssetDAO,
        private val reservationDAO: ReservationDAO,
        userDAO: UserDAO) : BaseController(userDAO) {

    private val logger = LoggerFactory.getLogger(ReservationController::class.java)

    @GetMapping
    fun getAllReservations(): ResponseEntity<Iterable<Reservation>> {
        this.logger.info("GET all reservations")
        return ResponseEntity(this.reservationDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getReservationById(@PathVariable id: Int): ResponseEntity<Reservation> {
        this.logger.info("GET reservation #$id")
        return this.reservationDAO.findById(id).map { ResponseEntity(it, HttpStatus.OK) }.orElseThrow { ResourceNotFoundException(id) }
    }


    @GetMapping("/of_asset/{id}")
    fun getAllReservationsByAsset(@PathVariable id: Int): ResponseEntity<Collection<Reservation>> {
        this.logger.info("GET all reservations of gym #$id")
        return this.assetDAO.findById(id).map { ResponseEntity(this.reservationDAO.findByAsset(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @PostMapping
    fun addReservation(@Valid @RequestBody reservation: ReservationDTO): ResponseEntity<Reservation> {
        this.logger.info("POST reservation")
        return ResponseEntity(this.reservationService.addReservation(reservation), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteReservationById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE reservation #$id")
        if (this.reservationDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(id)
        }

        this.reservationDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /********************************* MY RESERVATIONS *****************************************************/

    @GetMapping("/me")
    fun getAllReservationsOfLoggedUser(): ResponseEntity<Collection<Reservation>> {
        val user = this.getLoggedUser()
        this.logger.info("GET all reservations of user #${user.id}")
        return ResponseEntity(this.reservationDAO.findByUser(user), HttpStatus.OK)
    }


}