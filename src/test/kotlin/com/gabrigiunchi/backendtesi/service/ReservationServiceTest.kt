package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.exceptions.GymClosedException
import com.gabrigiunchi.backendtesi.exceptions.ReservationConflictException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.dto.ReservationDTO
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
import com.gabrigiunchi.backendtesi.util.DateDecorator
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class ReservationServiceTest : AbstractControllerTest() {

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var regionDAO: RegionDAO

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var reservationDAO: ReservationDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    private var user: User? = null
    private var gym: Gym? = null

    @Before
    fun clearDB() {
        this.assetDAO.deleteAll()
        this.gymDAO.deleteAll()
        this.assetKindDAO.deleteAll()
        this.userDAO.deleteAll()
        this.reservationDAO.deleteAll()

        this.user = this.userDAO.save(this.userFactory.createAdminUser("gabrigiunchi", "aaaa", "Gabriele", "Giunchi"))
        this.gym = this.createGym()
        this.createTimetable()
    }


    /************************************** CREATION *****************************************************************/

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the interval is in the past`() {
        val reservationDTO = ReservationDTO(this.user!!.id, -1,
                DateDecorator.now().minusMinutes(20).date, DateDecorator.now().minusMinutes(10).date)
        this.reservationService.addReservation(reservationDTO, reservationDTO.userID)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not create a reservation if the asset does not exist`() {
        val reservationDTO = ReservationDTO(this.user!!.id, -1, Date(), DateDecorator.now().plusMinutes(20).date)
        this.reservationService.addReservation(reservationDTO, reservationDTO.userID)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not create a reservation if the user does not exist`() {
        val reservationDTO = ReservationDTO(-1, this.createAsset().id, Date(), DateDecorator.now().plusMinutes(20).date)
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the start is after the end`() {
        val start = DateDecorator.now().plusMinutes(200)
        val reservationDTO = ReservationDTO(this.user!!.id, this.createAsset().id, start.date, start.minusMinutes(20).date)
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the start is equal to the end`() {
        val start = DateDecorator.now().plusMinutes(10).date
        val reservationDTO = ReservationDTO(this.user!!.id, this.createAsset().id, start, start)
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the duration exceeds the maximum`() {
        val start = DateDecorator.now()
        val end = start.plusMinutes(21)
        val reservationDTO = ReservationDTO(this.user!!.id, this.createAsset().id, start.date, end.date)
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = ReservationConflictException::class)
    fun `Should not create a reservation if there is another one in that time`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T12:00:00+0000").date))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T10:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))
    }

    @Test(expected = ReservationConflictException::class)
    fun `Should not create a reservation if there is another one in that time (edge case with same interval)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T12:00:00+0000").date))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T12:00:00+0000").date))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2019-04-30T08:00:00+0000").date, DateDecorator.of("2019-04-30T09:00:00+0000").date))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed (2)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T08:00:00+0000").date, DateDecorator.of("2050-04-04T11:00:00+0000").date))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed (3)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2019-04-22T14:00:00+0000").date, DateDecorator.of("2019-04-22T16:00:00+0000").date))
    }

    @Test
    fun `Should create a reservation`() {
        val start = DateDecorator.of("2050-04-04T11:00:00+0000")
        val end = start.plusMinutes(20)
        val reservationDTO = ReservationDTO(this.user!!.id, this.createAsset().id, start.date, end.date)
        this.reservationService.addReservation(reservationDTO)
        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should create a reservation (edge case with one reservation right after the other)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:30:00+0000").date, DateDecorator.of("2050-04-04T12:00:00+0000").date))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(2)
    }


    /*************************************** OTHERS **********************************************************/

    @Test(expected = ResourceNotFoundException::class)
    fun `Should check if a user own a reservation and throws an exception if the reservation belongs to another user`() {
        val r = this.reservationDAO.save(Reservation(this.createAsset(), this.user!!, Date(), DateDecorator.now().plusMinutes(20).date))
        val anotherUser = this.userDAO.save(this.userFactory.createAdminUser("dasdaas", "aaaa", "Gabriele", "Giunchi"))

        this.reservationService.checkReservationOfUser(anotherUser, r.id)
    }

    @Test
    fun `Should check if a user own a reservation and not throw an exception if the reservation belongs to the user`() {
        val r = this.reservationDAO.save(Reservation(this.createAsset(), this.user!!, Date(), DateDecorator.now().plusMinutes(20).date))
        this.reservationService.checkReservationOfUser(this.user!!, r.id)
    }

    /**************************************** UTILS ***********************************************************/

    private fun createTimetable(): Timetable {
        return this.timetableDAO.save(Timetable(this.gym!!, MockEntities.mockSchedules))
    }

    private fun createAsset(maxReservationTime: Int = 20): Asset {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, maxReservationTime))
        return this.assetDAO.save(Asset("ciclett1", kind, this.gym!!))
    }

    private fun createGym(): Gym {
        return this.gymDAO.save(Gym("gym1", "address", this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))))
    }
}