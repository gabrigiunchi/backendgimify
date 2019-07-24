package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.entities.*
import com.gabrigiunchi.backendtesi.model.rules.*
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class ReservationService(
        private val mailService: MailService,
        private val cityDAO: CityDAO,
        private val gymDAO: GymDAO,
        private val assetKindDAO: AssetKindDAO,
        private val assetDAO: AssetDAO,
        private val gymOpenRule: GymOpenRule,
        private val reservationDurationRule: ReservationDurationRule,
        private val reservationOverlapRule: ReservationOverlapRule,
        private val reservationIntervalValidator: ReservationIntervalValidator,
        private val userDAO: UserDAO,
        private val reservationValidator: ReservationValidator,
        private val reservationDAO: ReservationDAO) {

    @Value("\${application.maxReservationsPerDay}")
    private var maxReservationsPerDay: Int = 0

    @Value("\${application.reservationThresholdInDays}")
    private var reservationThresholdInDays: Int = 0

    @Value("\${application.mail.enabled}")
    private var mailEnabled: Boolean = false

    fun addReservation(reservationDTO: ReservationDTOInput, userId: Int): Reservation {
        val asset = this.getAsset(reservationDTO.assetID)
        val user = this.getUser(userId)
        val reservation = Reservation(
                asset = asset,
                user = user,
                start = reservationDTO.start,
                end = reservationDTO.end
        )

        this.reservationValidator.validate(reservation)
        val savedReservation = this.reservationDAO.save(reservation)

        if (this.mailEnabled && user.notificationsEnabled) {
            this.sendReservationConfirmationEmail(savedReservation)
        }

        return savedReservation
    }

    fun addReservation(reservationDTO: ReservationDTOInput): Reservation {
        return this.addReservation(reservationDTO, reservationDTO.userID)
    }

    fun deleteReservation(reservation: Reservation): Reservation {
        reservation.active = false
        this.reservationDAO.save(reservation)

        if (this.mailEnabled && reservation.user.notificationsEnabled) {
            this.sendCancellationConfirmationEmail(reservation)
        }

        return reservation
    }

    fun deleteReservationOfUser(user: User, reservationId: Int): Reservation =
            this.deleteReservation(this.getReservationOfUser(user, reservationId))

    fun isAssetAvailable(assetId: Int, start: OffsetDateTime, end: OffsetDateTime): Boolean {
        val interval = ZonedInterval(start, end)
        val asset = this.getAsset(assetId)
        return this.reservationIntervalValidator.test(interval) &&
                this.reservationDurationRule.test(Pair(asset.kind, interval)) &&
                this.gymOpenRule.test(Pair(asset.gym, interval)) &&
                this.reservationOverlapRule.test(Pair(asset, interval))
    }

    fun getAvailableAssets(kindId: Int, start: OffsetDateTime, end: OffsetDateTime): Collection<Asset> {
        val kind = this.getAssetKind(kindId)
        val interval = ZonedInterval(start, end)
        if (!this.reservationIntervalValidator.test(interval) || !this.reservationDurationRule.test(Pair(kind, interval))) {
            return emptyList()
        }

        return this.assetDAO.findByKind(kind, Pageable.unpaged())
                .content
                .filter { this.gymOpenRule.test(Pair(it.gym, interval)) }
                .filter { this.reservationOverlapRule.test(Pair(it, interval)) }
    }

    fun getAvailableAssetsInCity(kindId: Int, cityId: Int, start: OffsetDateTime, end: OffsetDateTime): Collection<Asset> {
        val kind = this.getAssetKind(kindId)
        val city = this.getCity(cityId)
        val interval = ZonedInterval(start, end)
        if (!this.reservationIntervalValidator.test(interval) || !this.reservationDurationRule.test(Pair(kind, interval))) {
            return emptyList()
        }

        return this.assetDAO.findByKind(kind, Pageable.unpaged())
                .content
                .filter { it.gym.city.id == city.id }
                .filter { this.gymOpenRule.test(Pair(it.gym, interval)) }
                .filter { this.reservationOverlapRule.test(Pair(it, interval)) }
    }

    fun getAvailableAssetsInGym(kindId: Int, gymId: Int, start: OffsetDateTime, end: OffsetDateTime): Collection<Asset> {
        val kind = this.getAssetKind(kindId)
        val gym = this.getGym(gymId)
        val interval = ZonedInterval(start, end)

        if (!this.reservationIntervalValidator.test(interval) || !this.reservationDurationRule.test(Pair(kind, interval))) {
            return emptyList()
        }

        return this.assetDAO.findByGymAndKind(gym, kind)
                .filter { this.gymOpenRule.test(Pair(it.gym, interval)) }
                .filter { this.reservationOverlapRule.test(Pair(it, interval)) }
    }

    /*************************************** UTILS ************************************************************/

    @Throws(ResourceNotFoundException::class)
    private fun getAssetKind(kindId: Int): AssetKind {
        return this.assetKindDAO.findById(kindId).orElseThrow { ResourceNotFoundException("asset kind $kindId does not exist") }
    }

    @Throws(ResourceNotFoundException::class)
    fun getReservationOfUser(user: User, reservationId: Int): Reservation =
            this.reservationDAO.findByIdAndUser(reservationId, user)
                    .filter { it.active }
                    .orElseThrow { ResourceNotFoundException("reservation $reservationId does not exist or is not owned by user ${user.id}") }

    @Throws(ResourceNotFoundException::class)
    private fun getGym(gymId: Int): Gym =
            this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }

    @Throws(ResourceNotFoundException::class)
    private fun getCity(cityId: Int): City =
            this.cityDAO.findById(cityId).orElseThrow { ResourceNotFoundException("city $cityId does not exist") }

    @Throws(ResourceNotFoundException::class)
    private fun getUser(userID: Int): User {
        return this.userDAO.findById(userID).orElseThrow { ResourceNotFoundException("user $userID does not exist") }
    }

    @Throws(ResourceNotFoundException::class)
    private fun getAsset(assetId: Int): Asset =
            this.assetDAO.findById(assetId).orElseThrow { ResourceNotFoundException("asset $assetId does not exist") }


    private fun sendReservationConfirmationEmail(reservation: Reservation) {
        val user = reservation.user
        val duration = Duration.between(reservation.start, reservation.end).toMinutes()
        val start = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm").format(reservation.start)
        val end = DateTimeFormatter.ofPattern("HH:mm").format(reservation.end)

        val content = StringBuilder()
                .appendln("<!DOCTYPE html><html><body>")
                .appendln("<p>Hi <b>${user.name} ${user.surname}</b>, here's the details of your reservation:</p>")
                .appendln("<p><b>Asset</b>: ${reservation.asset.name}</p>")
                .appendln("<p><b>Gym</b>: ${reservation.asset.gym.name}</p>")
                .appendln("<p><b>Address</b>: ${reservation.asset.gym.address}</p>")
                .appendln("<p><b>Date</b>: $start-$end</p>")
                .appendln("<p><b>Duration</b>: $duration minutes</p>")
                .appendln("</body></html>")
                .toString()

        Thread { this.mailService.sendEmail(user.email, "Reservation Confirmation", content, "text/html") }.start()
    }

    private fun sendCancellationConfirmationEmail(reservation: Reservation) {
        val user = reservation.user
        val duration = Duration.between(reservation.start, reservation.end).toMinutes()
        val start = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm").format(reservation.start)
        val end = DateTimeFormatter.ofPattern("HH:mm").format(reservation.end)

        val content = StringBuilder()
                .appendln("<!DOCTYPE html><html><body>")
                .appendln("<p>Hi <b>${user.name} ${user.surname}</b>, you just cancelled the reservation:</p>")
                .appendln("<p><b>Asset</b>: ${reservation.asset.name}</p>")
                .appendln("<p><b>Gym</b>: ${reservation.asset.gym.name}</p>")
                .appendln("<p><b>Address</b>: ${reservation.asset.gym.address}</p>")
                .appendln("<p><b>Date</b>: $start-$end</p>")
                .appendln("<p><b>Duration</b>: $duration minutes</p>")
                .appendln("</body></html>")
                .toString()

        Thread { this.mailService.sendEmail(user.email, "Cancellation Confirmation", content, "text/html") }.start()
    }
}