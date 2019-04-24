package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.dto.output.ReservationDTOOutput
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
    fun getAllReservations(): ResponseEntity<Iterable<ReservationDTOOutput>> {
        this.logger.info("GET all reservations")
        return ResponseEntity(this.reservationDAO.findAll().map { ReservationDTOOutput(it) }, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getReservationById(@PathVariable id: Int): ResponseEntity<ReservationDTOOutput> {
        this.logger.info("GET reservation #$id")
        return this.reservationDAO.findById(id)
                .map { ResponseEntity(ReservationDTOOutput(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }


    @GetMapping("/of_asset/{assetId}")
    fun getAllReservationsByAsset(@PathVariable assetId: Int): ResponseEntity<List<ReservationDTOOutput>> {
        this.logger.info("GET all reservations of asset #$assetId")
        return this.assetDAO.findById(assetId)
                .map { ResponseEntity(this.reservationDAO.findByAsset(it).map { r -> ReservationDTOOutput(r) }, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(assetId) }
    }

    @GetMapping("/of_asset/{assetId}/future")
    fun getAllFutureReservationsByAsset(@PathVariable assetId: Int): ResponseEntity<List<ReservationDTOOutput>> {
        this.logger.info("GET all future reservations of asset #$assetId")
        return this.assetDAO.findById(assetId)
                .map {
                    ResponseEntity(
                            this.reservationDAO.findByAssetAndEndAfter(it, Date()).map { r -> ReservationDTOOutput(r) },
                            HttpStatus.OK)
                }
                .orElseThrow { ResourceNotFoundException(assetId) }
    }

    @GetMapping("/of_asset/{assetId}/today")
    fun getAllReservationOfTodayByAsset(@PathVariable assetId: Int): ResponseEntity<List<ReservationDTOOutput>> {
        this.logger.info("GET all reservation of today for asset $assetId")
        return this.assetDAO.findById(assetId)
                .map {
                    ResponseEntity(
                            this.reservationDAO.findByAssetAndEndBetween(
                                    it,
                                    DateDecorator.startOfToday().date,
                                    DateDecorator.endOfToday().date
                            ).map { reservation -> ReservationDTOOutput(reservation) },
                            HttpStatus.OK)
                }
                .orElseThrow { ResourceNotFoundException(assetId) }
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
    fun addReservation(@Valid @RequestBody reservationDTO: ReservationDTOInput): ResponseEntity<ReservationDTOOutput> {
        this.logger.info("POST reservation")
        return ResponseEntity(
                ReservationDTOOutput(this.reservationService.addReservation(reservationDTO)),
                HttpStatus.CREATED)
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
    fun getAllReservationsOfLoggedUser(): ResponseEntity<List<ReservationDTOOutput>> {
        val user = this.getLoggedUser()
        this.logger.info("GET all reservations of user #${user.id}")
        return ResponseEntity(
                this.reservationDAO.findByUser(user).map { ReservationDTOOutput(it) },
                HttpStatus.OK)
    }

    @GetMapping("/me/future")
    fun getAllFutureReservationsOfLoggedUser(): ResponseEntity<Collection<ReservationDTOOutput>> {
        val user = this.getLoggedUser()
        this.logger.info("GET all future reservations of user #${user.id}")
        return ResponseEntity(
                this.reservationDAO
                        .findByUserAndEndAfter(user, DateDecorator.now().date)
                        .map { ReservationDTOOutput(it) },
                HttpStatus.OK)
    }

    @GetMapping("/me/{id}")
    fun getReservationOfUserById(@PathVariable id: Int): ResponseEntity<ReservationDTOOutput> {
        val user = this.getLoggedUser()
        this.logger.info("GET reservations #$id of user #${user.id}")
        return ResponseEntity(
                ReservationDTOOutput(this.reservationService.getReservationOfUserById(user, id)),
                HttpStatus.OK)
    }

    @PostMapping("/me")
    fun addReservationForLoggedUser(@Valid @RequestBody reservationDTO: ReservationDTOInput): ResponseEntity<ReservationDTOOutput> {
        val user = this.getLoggedUser()
        this.logger.info("POST reservation for user #${user.id}")
        return ResponseEntity(
                ReservationDTOOutput(this.reservationService.addReservation(reservationDTO, user.id)),
                HttpStatus.CREATED)
    }

    @DeleteMapping("/me/{id}")
    fun deleteReservationForLoggedUser(@PathVariable(name = "id") reservationId: Int): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE reservation #$reservationId of user #${user.id}")
        this.reservationService.deleteReservationOfUser(user, reservationId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}