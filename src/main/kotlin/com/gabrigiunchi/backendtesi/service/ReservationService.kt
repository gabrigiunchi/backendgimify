package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.exceptions.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class ReservationService(
        private val mailService: MailService,
        private val reservationLogDAO: ReservationLogDAO,
        private val cityDAO: CityDAO,
        private val gymDAO: GymDAO,
        private val assetKindDAO: AssetKindDAO,
        private val timetableDAO: TimetableDAO,
        private val assetDAO: AssetDAO,
        private val userDAO: UserDAO,
        private val reservationDAO: ReservationDAO) {

    @Value("\${application.maxReservationsPerDay}")
    private var maxReservationsPerDay: Int = 0

    @Value("\${application.reservationThresholdInDays}")
    private var reservationThresholdInDays: Int = 0

    @Value("\${application.mail.enabled}")
    private var mailEnabled: Boolean = false

    @Value("\${application.zoneId}")
    private var zoneId: String = "UTC"

    fun addReservation(reservationDTO: ReservationDTOInput, userId: Int): Reservation {
        if (this.isInThePast(reservationDTO.start)) {
            throw BadRequestException("reservation must be in the future")
        }

        if (reservationDTO.start >= (reservationDTO.end)) {
            throw BadRequestException("start is after the end")
        }

        if (this.isBeyondTheThreshold(reservationDTO.start)) {
            throw ReservationThresholdExceededException()
        }

        val asset = this.assetDAO.findById(reservationDTO.assetID)
                .orElseThrow { ResourceNotFoundException("asset ${reservationDTO.assetID} does not exist") }

        val user = this.getUser(userId)
        if (this.numberOfReservationsMadeByUserInDate(user, Date()) >= this.maxReservationsPerDay) {
            throw TooManyReservationsException()
        }

        if (!this.isReservationDurationValid(asset, reservationDTO.start, reservationDTO.end)) {
            throw BadRequestException("reservation duration exceeds maximum (max=${asset.kind.maxReservationTime} minutes)")
        }

        if (!this.isGymOpen(asset.gym, reservationDTO.start, reservationDTO.end)) {
            throw GymClosedException()
        }

        if (!this.isAssetAvailable(asset, reservationDTO.start, reservationDTO.end)) {
            throw ReservationConflictException()
        }

        val savedReservation = this.reservationDAO.save(
                Reservation(
                        asset = asset,
                        user = user,
                        start = reservationDTO.start,
                        end = reservationDTO.end
                )
        )
        this.reservationLogDAO.save(ReservationLog(savedReservation))

        if (this.mailEnabled && user.notificationsEnabled) {
            this.sendReservationConfirmationEmail(savedReservation)
        }

        return savedReservation
    }

    fun addReservation(reservationDTO: ReservationDTOInput): Reservation {
        return this.addReservation(reservationDTO, reservationDTO.userID)
    }

    fun deleteReservationOfUser(user: User, reservationId: Int): Reservation {
        val reservation = this.getReservationOfUser(user, reservationId)
        this.reservationDAO.delete(reservation)

        if (this.mailEnabled && user.notificationsEnabled) {
            this.sendCancellationConfirmationEmail(reservation)
        }

        return reservation
    }

    fun getAvailableAssets(kindId: Int, start: OffsetDateTime, end: OffsetDateTime): Collection<Asset> {
        this.checkInterval(start, end)

        if (this.isInThePast(start) || this.isBeyondTheThreshold(start)) {
            return emptyList()
        }

        return this.assetDAO.findByKind(this.getAssetKind(kindId), Pageable.unpaged())
                .content
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    fun getAvailableAssetsInCity(kindId: Int, cityId: Int, start: OffsetDateTime, end: OffsetDateTime): Collection<Asset> {
        this.checkInterval(start, end)
        this.getCity(cityId)

        if (this.isInThePast(start) || this.isBeyondTheThreshold(start)) {
            return emptyList()
        }

        return this.assetDAO.findByKind(this.getAssetKind(kindId), Pageable.unpaged())
                .content
                .filter { it.gym.city.id == cityId }
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    fun getAvailableAssetsInGym(kindId: Int, gymId: Int, start: OffsetDateTime, end: OffsetDateTime): Collection<Asset> {
        this.checkInterval(start, end)
        val gym = this.getGym(gymId)

        if (this.isInThePast(start) || this.isBeyondTheThreshold(start)) {
            return emptyList()
        }

        return this.assetDAO.findByGymAndKind(gym, this.getAssetKind(kindId))
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    fun isAssetAvailable(assetId: Int, start: OffsetDateTime, end: OffsetDateTime): Boolean {
        if (start >= end || this.isInThePast(start) || this.isBeyondTheThreshold(start)) {
            return false
        }
        return this.assetDAO.findById(assetId)
                .map {
                    this.isReservationDurationValid(it, start, end) &&
                            this.isGymOpen(it.gym, start, end) &&
                            this.isAssetAvailable(it, start, end)
                }
                .orElseThrow { ResourceNotFoundException("asset $assetId does not exist") }
    }

    private fun checkInterval(start: OffsetDateTime, end: OffsetDateTime) {
        if (start >= end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    private fun isInThePast(date: OffsetDateTime): Boolean = date.toInstant() < Instant.now()

    private fun getAssetKind(kindId: Int): AssetKind {
        return this.assetKindDAO.findById(kindId).orElseThrow { ResourceNotFoundException("asset kind $kindId does not exist") }
    }

    private fun getGym(gymId: Int): Gym {
        return this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    private fun getCity(cityId: Int): City {
        return this.cityDAO.findById(cityId).orElseThrow { ResourceNotFoundException("city $cityId does not exist") }
    }

    /*************************************** UTILS ************************************************************/

    fun numberOfReservationsMadeByUserInDate(user: User, date: Date): Int {
        val d = DateDecorator.of(date)
        return this.reservationLogDAO.findByUserAndDateBetween(user, d.minusDays(1).date, d.plusDays(1).date)
                .filter { DateDecorator.of(it.date).isSameDay(date, ZoneId.of(this.zoneId)) }
                .count()
    }

    fun isGymOpen(gym: Gym, start: OffsetDateTime, end: OffsetDateTime): Boolean {
        val timetable = this.timetableDAO.findByGym(gym)
        return timetable.isPresent && timetable.get()
                .contains(ZonedInterval(start, end).toInterval(gym.city.zoneId))
    }

    /**
     * Returns true if there are no other reservations for the given asset in the given interval
     */
    fun isAssetAvailable(asset: Asset, start: OffsetDateTime, end: OffsetDateTime): Boolean {
        val reservations = this.reservationDAO.findByAssetAndEndAfter(asset, start)
        return reservations.none { ZonedInterval(it.start, it.end).overlaps(ZonedInterval(start, end)) }
    }

    /**
     * Check if the duration given by the interval (start, end) is valid according to the asset's kind
     * For instance, a ciclette could be reserved for max 1 hour
     */
    fun isReservationDurationValid(asset: Asset, start: OffsetDateTime, end: OffsetDateTime): Boolean {
        return start.plusMinutes(asset.kind.maxReservationTime.toLong()) >= end
    }

    @Throws(ResourceNotFoundException::class)
    fun getReservationOfUser(user: User, reservationId: Int): Reservation {
        return this.reservationDAO.findByIdAndUser(reservationId, user)
                .orElseThrow { ResourceNotFoundException("reservation $reservationId does not exist or is not owned by user ${user.id}") }
    }

    @Throws(ResourceNotFoundException::class)
    private fun getUser(userID: Int): User {
        return this.userDAO.findById(userID).orElseThrow { ResourceNotFoundException("user $userID does not exist") }
    }

    private fun isBeyondTheThreshold(date: OffsetDateTime) = date.toInstant().isAfter(OffsetDateTime.now().plusDays(this.reservationThresholdInDays.toLong()).toInstant())

    private fun sendReservationConfirmationEmail(reservation: Reservation) {
        val user = reservation.user
        val duration = Duration.between(reservation.start, reservation.end).toMinutes()
        val start = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm").format(reservation.start)
        val end = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm").format(reservation.end)

        val content = StringBuilder()
                .appendln("Hi ${user.name} ${user.surname}, here's the details of your reservation:")
                .appendln()
                .appendln("Gym: ${reservation.asset.gym.name}")
                .appendln()
                .appendln("Address: ${reservation.asset.gym.address}")
                .appendln()
                .appendln("Asset: ${reservation.asset.name}")
                .appendln()
                .appendln("Date: $start-$end")
                .appendln()
                .appendln("Duration: $duration minutes")
                .toString()

        Thread { this.mailService.sendEmail(user.email, "Reservation Confirmation", content) }.start()
    }

    private fun sendCancellationConfirmationEmail(reservation: Reservation) {
        val user = reservation.user
        val duration = Duration.between(reservation.start, reservation.end).toMinutes()
        val start = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm").format(reservation.start)
        val end = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm").format(reservation.end)

        val content = StringBuilder()
                .appendln("Hi ${user.name} ${user.surname}, you just cancelled the reservation:")
                .appendln()
                .appendln("Gym: ${reservation.asset.gym.name}")
                .appendln()
                .appendln("Address: ${reservation.asset.gym.address}")
                .appendln()
                .appendln("Asset: ${reservation.asset.name}")
                .appendln()
                .appendln("Date: $start-$end")
                .appendln()
                .appendln("Duration: $duration minutes")
                .toString()

        Thread { this.mailService.sendEmail(user.email, "Cancellation Confirmation", content) }.start()
    }
}