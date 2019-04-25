package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.exceptions.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReservationService(
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

    fun addReservation(reservationDTO: ReservationDTOInput, userId: Int): Reservation {
        if (reservationDTO.start < Date()) {
            throw BadRequestException("reservation must be in the future")
        }

        if (reservationDTO.start >= (reservationDTO.end)) {
            throw BadRequestException("start is after the end")
        }

        if (this.isBeyondTheThreshold(reservationDTO.start)) {
            throw ReservationThresholdExceededException()
        }

        if (this.assetDAO.findById(reservationDTO.assetID).isEmpty) {
            throw ResourceNotFoundException("asset ${reservationDTO.assetID} does not exist")
        }

        val user = this.getUser(userId)
        if (this.numberOfReservationsMadeByUserInDate(user, Date()) >= this.maxReservationsPerDay) {
            throw TooManyReservationsException()
        }

        val asset = this.assetDAO.findById(reservationDTO.assetID).get()
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

        return savedReservation
    }

    fun addReservation(reservationDTO: ReservationDTOInput): Reservation {
        return this.addReservation(reservationDTO, reservationDTO.userID)
    }

    fun getReservationOfUserById(user: User, reservationId: Int): Reservation {
        this.checkReservationOfUser(user, reservationId)
        return this.reservationDAO.findById(reservationId).get()
    }

    fun deleteReservationOfUser(user: User, reservationId: Int) {
        this.checkReservationOfUser(user, reservationId)
        this.reservationDAO.deleteById(reservationId)
    }

    fun getAvailableAssets(kindId: Int, start: Date, end: Date): Collection<Asset> {
        this.checkInterval(start, end)

        if (start < Date() || this.isBeyondTheThreshold(start)) {
            return emptyList()
        }

        return this.assetDAO.findByKind(this.getAssetKind(kindId), Pageable.unpaged())
                .content
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    fun getAvailableAssetsInCity(kindId: Int, cityId: Int, start: Date, end: Date): Collection<Asset> {
        this.checkInterval(start, end)
        this.getCity(cityId)

        if (start < Date() || this.isBeyondTheThreshold(start)) {
            return emptyList()
        }

        return this.assetDAO.findByKind(this.getAssetKind(kindId), Pageable.unpaged())
                .content
                .filter { it.gym.city.id == cityId }
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    fun getAvailableAssetsInGym(kindId: Int, gymId: Int, start: Date, end: Date): Collection<Asset> {
        this.checkInterval(start, end)
        val gym = this.getGym(gymId)

        if (start < Date() || this.isBeyondTheThreshold(start)) {
            return emptyList()
        }

        return this.assetDAO.findByGymAndKind(gym, this.getAssetKind(kindId))
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    private fun checkInterval(start: Date, end: Date) {
        if (start >= end) {
            throw IllegalArgumentException("start is after the end")
        }
    }

    private fun getAssetKind(kindId: Int): AssetKind {
        return this.assetKindDAO.findById(kindId).orElseThrow { ResourceNotFoundException("asset kind $kindId does not exist") }
    }

    private fun getGym(gymId: Int): Gym {
        return this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    private fun getCity(cityId: Int): City {
        return this.cityDAO.findById(cityId).orElseThrow { ResourceNotFoundException("city $cityId does not exist") }
    }

    /******************************* UTILS ************************************************************/
    fun numberOfReservationsMadeByUserInDate(user: User, date: Date): Int {
        val d = DateDecorator.of(date)
        return this.reservationLogDAO.findByUserAndDateBetween(user, d.minusDays(1).date, d.plusDays(1).date)
                .filter { DateDecorator.of(it.date).isSameDay(date) }
                .count()
    }

    fun isGymOpen(gym: Gym, start: Date, end: Date): Boolean {
        val timetable = this.timetableDAO.findByGym(gym)
        return timetable.isPresent && timetable.get().contains(DateInterval(start, end))
    }

    /**
     * Returns true if there are no other reservations for the given asset in the given interval
     */
    fun isAssetAvailable(asset: Asset, start: Date, end: Date): Boolean {
        val reservations = this.reservationDAO.findByAssetAndEndAfter(asset, start)
        return reservations.none { DateInterval(it.start, it.end).overlaps(DateInterval(start, end)) }
    }

    /**
     * Check if the duration given by the interval (start, end) is valid according to the asset's kind
     * For instance, a ciclette could be reserved for max 1 hour
     */
    fun isReservationDurationValid(asset: Asset, start: Date, end: Date): Boolean {
        return DateDecorator.of(start).plusMinutes(asset.kind.maxReservationTime).date >= end
    }

    /**
     * Check if the reservation belongs to the user
     * @throws ResourceNotFoundException if the user does not exist
     * @throws ResourceNotFoundException if the reservation does not exist or the user is not the owner
     */
    @Throws(ResourceNotFoundException::class)
    fun checkReservationOfUser(user: User, reservationId: Int) {
        if (this.reservationDAO.findByUser(user).none { it.id == reservationId }) {
            throw ResourceNotFoundException("user ${user.id} does not have reservation $reservationId")
        }
    }

    @Throws(ResourceNotFoundException::class)
    private fun getUser(userID: Int): User {
        return this.userDAO.findById(userID)
                .map { it }
                .orElseThrow { ResourceNotFoundException("user $userID does not exist") }
    }

    private fun isBeyondTheThreshold(date: Date) = date > DateDecorator.now().plusDays(this.reservationThresholdInDays).date
}