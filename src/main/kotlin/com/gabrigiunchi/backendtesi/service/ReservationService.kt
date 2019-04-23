package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.exceptions.GymClosedException
import com.gabrigiunchi.backendtesi.exceptions.ReservationConflictException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.dto.ReservationDTO
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReservationService(
        private val regionDAO: RegionDAO,
        private val gymDAO: GymDAO,
        private val assetKindDAO: AssetKindDAO,
        private val timetableDAO: TimetableDAO,
        private val assetDAO: AssetDAO,
        private val userDAO: UserDAO,
        private val reservationDAO: ReservationDAO) {


    fun addReservation(reservationDTO: ReservationDTO, userId: Int): Reservation {
        if (reservationDTO.end.before(Date())) {
            throw BadRequestException("reservation must be in the future")
        }

        if (reservationDTO.start.after(reservationDTO.end)) {
            throw BadRequestException("start is after the end")
        }

        if (reservationDTO.start == reservationDTO.end) {
            throw BadRequestException("start cannot be equals to the end")
        }

        if (this.assetDAO.findById(reservationDTO.assetID).isEmpty) {
            throw ResourceNotFoundException("asset #${reservationDTO.assetID} does not exist")
        }

        this.checkUser(userId)
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

        return this.reservationDAO.save(
                Reservation(
                        asset = asset,
                        user = this.userDAO.findById(reservationDTO.userID).get(),
                        start = reservationDTO.start,
                        end = reservationDTO.end
                )
        )
    }

    fun addReservation(reservationDTO: ReservationDTO): Reservation {
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
        return this.assetDAO.findByKind(this.getAssetKind(kindId))
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    fun getAvailableAssetsInRegion(kindId: Int, regionId: Int, start: Date, end: Date): Collection<Asset> {
        this.checkInterval(start, end)
        this.getRegion(regionId)
        return this.assetDAO.findByKind(this.getAssetKind(kindId))
                .filter { it.gym.region.id == regionId }
                .filter { isGymOpen(it.gym, start, end) }
                .filter { isReservationDurationValid(it, start, end) }
                .filter { isAssetAvailable(it, start, end) }
    }

    fun getAvailableAssetsInGym(kindId: Int, gymId: Int, start: Date, end: Date): Collection<Asset> {
        this.checkInterval(start, end)
        this.getGym(gymId)
        return this.assetDAO.findByKind(this.getAssetKind(kindId))
                .filter { it.gym.id == gymId }
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

    private fun getRegion(regionId: Int): Region {
        return this.regionDAO.findById(regionId).orElseThrow { ResourceNotFoundException("region $regionId does not exist") }
    }

    /******************************* UTILS ************************************************************/

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
     * Check if the user exists
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Throws(ResourceNotFoundException::class)
    private fun checkUser(userID: Int) {
        if (this.userDAO.findById(userID).isEmpty) {
            throw ResourceNotFoundException("user #$userID does not exist")
        }
    }

    /**
     * Check if the reservation belongs to the user
     * @throws ResourceNotFoundException if the user does not exist
     * @throws ResourceNotFoundException if the reservation does not exist or the user is not the owner
     */
    @Throws(ResourceNotFoundException::class)
    fun checkReservationOfUser(user: User, reservationId: Int) {
        if (this.reservationDAO.findByUser(user).none { it.id == reservationId }) {
            throw ResourceNotFoundException("user #$${user.id} does not have reservation #$reservationId")
        }
    }
}