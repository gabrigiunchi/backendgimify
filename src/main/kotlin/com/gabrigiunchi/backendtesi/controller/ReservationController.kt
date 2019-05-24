package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.dao.ReservationLogDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.dto.output.ReservationDTOOutput
import com.gabrigiunchi.backendtesi.service.ReservationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.OffsetDateTime
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
        private val reservationService: ReservationService,
        private val assetDAO: AssetDAO,
        private val reservationLogDAO: ReservationLogDAO,
        private val reservationDAO: ReservationDAO,
        val userDAO: UserDAO) : BaseController(userDAO) {

    private val logger = LoggerFactory.getLogger(ReservationController::class.java)

    @Value("\${application.zoneId}")
    private var zoneId: String = "UTC"

    @GetMapping("/page/{page}/size/{size}")
    fun getAllReservations(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<ReservationDTOOutput>> {
        this.logger.info("GET all reservations, page=$page size=$size")
        return ResponseEntity(this.reservationDAO.findAll(this.pageRequest(page, size)).map { ReservationDTOOutput(it) }, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getReservationById(@PathVariable id: Int): ResponseEntity<ReservationDTOOutput> {
        this.logger.info("GET reservation #$id")
        return this.reservationDAO.findById(id)
                .map { ResponseEntity(ReservationDTOOutput(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("reservation $id does not exist") }
    }

    @GetMapping("/of_asset/{assetId}")
    fun getAllReservationsByAsset(@PathVariable assetId: Int): ResponseEntity<List<ReservationDTOOutput>> {
        this.logger.info("GET all reservations of asset #$assetId")
        return this.assetDAO.findById(assetId)
                .map { ResponseEntity(this.reservationDAO.findByAsset(it).map { r -> ReservationDTOOutput(r) }, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("asset $assetId does not exist") }
    }

    @GetMapping("/available/kind/{kindId}/from/{from}/to/{to}")
    fun getAvailableAssets(@PathVariable kindId: Int,
                           @PathVariable from: String,
                           @PathVariable to: String): ResponseEntity<Collection<Asset>> {

        this.logger.info("GET available assets of kind $kindId from $from to $to")
        return ResponseEntity(this.reservationService.getAvailableAssets(
                kindId, OffsetDateTime.parse(from), OffsetDateTime.parse(to)), HttpStatus.OK)
    }

    @GetMapping("/available/kind/{kindId}/from/{from}/to/{to}/gym/{gymId}")
    fun getAvailableAssetsInGym(@PathVariable kindId: Int,
                                @PathVariable from: String,
                                @PathVariable to: String,
                                @PathVariable gymId: Int): ResponseEntity<Collection<Asset>> {

        this.logger.info("GET available assets of kind $kindId from $from to $to in gym $gymId")
        return ResponseEntity(this.reservationService.getAvailableAssetsInGym(
                kindId, gymId, OffsetDateTime.parse(from), OffsetDateTime.parse(to)), HttpStatus.OK)
    }

    @GetMapping("/available/kind/{kindId}/from/{from}/to/{to}/city/{cityId}")
    fun getAvailableAssetsInCity(@PathVariable kindId: Int,
                                 @PathVariable from: String,
                                 @PathVariable to: String,
                                 @PathVariable cityId: Int): ResponseEntity<Collection<Asset>> {

        this.logger.info("GET available assets of kind $kindId from $from to $to in city $cityId")
        return ResponseEntity(this.reservationService.getAvailableAssetsInCity(
                kindId, cityId, OffsetDateTime.parse(from), OffsetDateTime.parse(to)), HttpStatus.OK)
    }

    @GetMapping("/available/asset/{assetId}/from/{from}/to/{to}")
    fun isAssetAvailable(@PathVariable assetId: Int,
                         @PathVariable from: String,
                         @PathVariable to: String): ResponseEntity<Boolean> {

        this.logger.info("GET availability of asset $assetId in interval: from $from to $to")
        return ResponseEntity(this.reservationService.isAssetAvailable(
                assetId, OffsetDateTime.parse(from), OffsetDateTime.parse(to)), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun addReservation(@Valid @RequestBody reservationDTO: ReservationDTOInput): ResponseEntity<ReservationDTOOutput> {
        this.logger.info("POST reservation")
        return ResponseEntity(
                ReservationDTOOutput(this.reservationService.addReservation(reservationDTO)),
                HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteReservationById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE reservation #$id")
        this.reservationDAO.delete(
                this.reservationDAO.findById(id)
                        .orElseThrow { ResourceNotFoundException("reservation $id does not exist") }
        )
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /********************************* MY RESERVATIONS *****************************************************/

    @GetMapping("/me/count")
    fun countMyReservations(): ResponseEntity<Int> {
        val user = this.getLoggedUser()
        this.logger.info("GET number of reservations made by logged user (#${user.id})")
        return ResponseEntity(this.reservationLogDAO.findByUser(user).count(), HttpStatus.OK)
    }

    @GetMapping("/me/page/{page}/size/{size}")
    fun getAllReservationsOfLoggedUser(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<ReservationDTOOutput>> {
        val user = this.getLoggedUser()
        this.logger.info("GET all reservations of user #${user.id}")
        return ResponseEntity(
                this.reservationDAO
                        .findByUser(user, PageRequest.of(page, size, Sort.by("start").descending()))
                        .map { ReservationDTOOutput(it) },
                HttpStatus.OK)
    }

    @GetMapping("/me/from/{from}")
    fun getAllFutureReservationsOfLoggedUser(
            @PathVariable from: String): ResponseEntity<Collection<ReservationDTOOutput>> {
        val user = this.getLoggedUser()
        this.logger.info("GET all future reservations of user #${user.id}")
        return ResponseEntity(
                this.reservationDAO
                        .findByUserAndEndAfter(user, OffsetDateTime.parse(from))
                        .map { ReservationDTOOutput(it) },
                HttpStatus.OK)
    }

    @GetMapping("/me/{id}")
    fun getReservationOfUserById(@PathVariable id: Int): ResponseEntity<ReservationDTOOutput> {
        val user = this.getLoggedUser()
        this.logger.info("GET reservations #$id of user #${user.id}")
        return ResponseEntity(ReservationDTOOutput(this.reservationService.getReservationOfUser(user, id)), HttpStatus.OK)
    }

    @PostMapping("/me")
    fun addReservationForLoggedUser(@Valid @RequestBody reservationDTO: ReservationDTOInput): ResponseEntity<ReservationDTOOutput> {
        val user = this.getLoggedUser()
        this.logger.info("POST reservation for user #${user.id}")
        return ResponseEntity(ReservationDTOOutput(
                this.reservationService.addReservation(reservationDTO, user.id)),
                HttpStatus.CREATED)
    }

    @DeleteMapping("/me/{id}")
    fun deleteReservationForLoggedUser(@PathVariable(name = "id") reservationId: Int): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE reservation #$reservationId of user #${user.id}")
        this.reservationService.deleteReservationOfUser(user, reservationId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    private fun pageRequest(page: Int, size: Int, sort: Sort = Sort.by("id")) = PageRequest.of(page, size, sort)
}