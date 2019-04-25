package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.ReservationDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.dto.output.ReservationDTOOutput
import com.gabrigiunchi.backendtesi.service.MailService
import com.gabrigiunchi.backendtesi.service.ReservationService
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
        private val mailService: MailService,
        private val reservationService: ReservationService,
        private val assetDAO: AssetDAO,
        private val reservationDAO: ReservationDAO,
        userDAO: UserDAO) : BaseController(userDAO) {

    private val logger = LoggerFactory.getLogger(ReservationController::class.java)

    @Value("\${application.mail.enabled}")
    private var mailEnabled: Boolean = false

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

    @GetMapping("/of_asset/{assetId}/future")
    fun getAllFutureReservationsByAsset(@PathVariable assetId: Int): ResponseEntity<List<ReservationDTOOutput>> {
        this.logger.info("GET all future reservations of asset #$assetId")
        return this.assetDAO.findById(assetId)
                .map {
                    ResponseEntity(
                            this.reservationDAO.findByAssetAndEndAfter(it, Date()).map { r -> ReservationDTOOutput(r) },
                            HttpStatus.OK)
                }
                .orElseThrow { ResourceNotFoundException("asset $assetId does not exist") }
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
                .orElseThrow { ResourceNotFoundException("asset $assetId does not exist") }
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
            throw ResourceNotFoundException("reservation $id does not exist")
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

        val savedReservation = this.reservationService.addReservation(reservationDTO, user.id)

        if (this.mailEnabled) {
            this.sendConfirmationEmail(savedReservation)
        }

        return ResponseEntity(ReservationDTOOutput(savedReservation), HttpStatus.CREATED)
    }

    @DeleteMapping("/me/{id}")
    fun deleteReservationForLoggedUser(@PathVariable(name = "id") reservationId: Int): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE reservation #$reservationId of user #${user.id}")
        val deletedReservation = this.reservationService.deleteReservationOfUser(user, reservationId)

        if (this.mailEnabled) {
            this.sendCancellationConfirmation(deletedReservation)
        }

        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    private fun pageRequest(page: Int, size: Int, sort: Sort = Sort.by("id")) = PageRequest.of(page, size, sort)

    private fun sendConfirmationEmail(reservation: Reservation) {
        val user = this.getLoggedUser()
        val content = "Hi ${user.name} ${user.surname}, here's your reservation:\n" +
                "Gym: ${reservation.asset.gym.name}\n" +
                "Address: ${reservation.asset.gym.address}" +
                "Asset: ${reservation.asset.name}" +
                "Date: ${reservation.start} - ${reservation.end}"

        Thread { this.mailService.sendEmail(user.email, "Reservation Confirmation", content) }.start()
    }

    private fun sendCancellationConfirmation(reservation: Reservation) {
        val user = this.getLoggedUser()
        val content = "Hi ${user.name} ${user.surname}, you just cancelled the reservation: \n" +
                "Gym: ${reservation.asset.gym.name}\n" +
                "Address: ${reservation.asset.gym.address}" +
                "Asset: ${reservation.asset.name}" +
                "Date: ${reservation.start} - ${reservation.end}"

        Thread { this.mailService.sendEmail(user.email, "Cancellation Confirmation", content) }.start()
    }
}