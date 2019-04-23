package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.dto.ReservationDTO
import com.gabrigiunchi.backendtesi.service.ReservationService
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
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

    @GetMapping("/of_asset/{id}/future")
    fun getAllFutureReservationsByAsset(@PathVariable id: Int): ResponseEntity<Collection<Reservation>> {
        this.logger.info("GET all future reservations of gym #$id")
        return this.assetDAO.findById(id)
                .map { ResponseEntity(this.reservationDAO.findByAssetAndEndAfter(it, Date()), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @GetMapping("/of_asset/{id}/today")
    fun getAllReservationOfTodayByAsset(@PathVariable id: Int): ResponseEntity<Collection<Reservation>> {
        this.logger.info("GET all reservation of today for asset $id")
        return this.assetDAO.findById(id)
                .map {
                    ResponseEntity(
                            this.reservationDAO.findByAssetAndEndBetween(
                                    it,
                                    DateDecorator.startOfToday().date,
                                    DateDecorator.endOfToday().date
                            ),
                            HttpStatus.OK)
                }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @GetMapping("/available/kind/{kindId}/from/{from}/to/{to}")
    fun getAvailableAssets(@PathVariable kindId: Int,
                           @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") from: Date,
                           @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") to: Date): ResponseEntity<Collection<Asset>> {

        this.logger.info("GET available assets of kind $kindId from $from to $to")
        return ResponseEntity(this.reservationService.getAvailableAssets(kindId, from, to), HttpStatus.OK)
    }

    @GetMapping("/available/kind/{kindId}/from/{from}/to/{to}/gym/{gymId}")
    fun getAvailableAssetsInGym(@PathVariable kindId: Int,
                                @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") from: Date,
                                @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") to: Date,
                                @PathVariable gymId: Int): ResponseEntity<Collection<Asset>> {

        this.logger.info("GET available assets of kind $kindId from $from to $to in gym $gymId")
        return ResponseEntity(this.reservationService.getAvailableAssetsInGym(kindId, gymId, from, to), HttpStatus.OK)
    }

    @GetMapping("/available/kind/{kindId}/from/{from}/to/{to}/city/{cityId}")
    fun getAvailableAssetsInCity(@PathVariable kindId: Int,
                                 @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") from: Date,
                                 @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ") to: Date,
                                 @PathVariable cityId: Int): ResponseEntity<Collection<Asset>> {

        this.logger.info("GET available assets of kind $kindId from $from to $to in city $cityId")
        return ResponseEntity(this.reservationService.getAvailableAssetsInCity(kindId, cityId, from, to), HttpStatus.OK)
    }

    @PostMapping
    fun addReservation(@Valid @RequestBody reservationDTO: ReservationDTO): ResponseEntity<Reservation> {
        this.logger.info("POST reservation")
        return ResponseEntity(this.reservationService.addReservation(reservationDTO), HttpStatus.CREATED)
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

    @GetMapping("/me/future")
    fun getAllFutureReservationsOfLoggedUser(): ResponseEntity<Collection<Reservation>> {
        val user = this.getLoggedUser()
        this.logger.info("GET all future reservations of user #${user.id}")
        return ResponseEntity(this.reservationDAO.findByUserAndEndAfter(user, DateDecorator.now().date), HttpStatus.OK)
    }

    @GetMapping("/me/{id}")
    fun getReservationOfUserById(@PathVariable id: Int): ResponseEntity<Reservation> {
        val user = this.getLoggedUser()
        this.logger.info("GET reservations #$id of user #${user.id}")
        return ResponseEntity(this.reservationService.getReservationOfUserById(user, id), HttpStatus.OK)
    }

    @PostMapping("/me")
    fun addReservationForLoggedUser(@Valid @RequestBody reservationDTO: ReservationDTO): ResponseEntity<Reservation> {
        val user = this.getLoggedUser()
        this.logger.info("POST reservation for user #${user.id}")
        return ResponseEntity(this.reservationService.addReservation(reservationDTO, user.id), HttpStatus.CREATED)
    }

    @DeleteMapping("/me/{id}")
    fun deleteReservationForLoggedUser(@PathVariable(name = "id") reservationId: Int): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE reservation #$reservationId of user #${user.id}")
        this.reservationService.deleteReservationOfUser(user, reservationId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}