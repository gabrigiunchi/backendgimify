package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.exceptions.*
import com.gabrigiunchi.backendtesi.model.entities.*
import com.gabrigiunchi.backendtesi.model.rules.ReservationValidator
import com.gabrigiunchi.backendtesi.model.time.Timetable
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.service.ReservationService
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.time.OffsetDateTime

class ReservationValidatorTest : AbstractControllerTest() {


    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var reservationValidator: ReservationValidator

    @Autowired
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var reservationDAO: ReservationDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @Value("\${application.reservationThresholdInDays}")
    private var reservationThresholdInDays: Long = 0

    private var gym: Gym? = null

    @Before
    fun clearDB() {
        this.assetDAO.deleteAll()
        this.gymDAO.deleteAll()
        this.assetKindDAO.deleteAll()
        this.userDAO.deleteAll()
        this.reservationDAO.deleteAll()

        this.gym = this.mockGym
        this.mockTimetable()
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the interval is in the past`() {
        val reservation = Reservation(this.mockAsset(), this.mockUser,
                OffsetDateTime.now().minusMinutes(20), OffsetDateTime.now().minusMinutes(10))
        this.reservationValidator.validate(reservation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create a reservation if the start is after the end`() {
        val start = OffsetDateTime.now().plusMinutes(200)
        val reservation = Reservation(this.mockAsset(), this.mockUser, start, start.minusMinutes(20))
        this.reservationValidator.validate(reservation)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create a reservation if the start is equal to the end`() {
        val start = OffsetDateTime.now().plusMinutes(10)
        val reservation = Reservation(this.mockAsset(), this.mockUser, start, start)
        this.reservationValidator.validate(reservation)
    }

    @Test(expected = ReservationDurationException::class)
    fun `Should not create a reservation if the duration exceeds the maximum`() {
        val start = OffsetDateTime.now().plusMinutes(1)
        val end = start.plusMinutes(21)
        val reservation = Reservation(this.mockAsset(), this.mockUser, start, end)
        this.reservationValidator.validate(reservation)
    }

    @Test(expected = ReservationConflictException::class)
    fun `Should not create a reservation if there is another one in that time`() {
        val user = this.mockUser
        val asset = this.mockAsset(300)

        this.reservationDAO.save(Reservation(asset, user,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)

        this.reservationValidator.validate(Reservation(asset, user,
                OffsetDateTime.parse("2050-04-04T10:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))
    }

    @Test(expected = ReservationConflictException::class)
    fun `Should not create a reservation if there is another one in that time (edge case with same interval)`() {
        val asset = this.mockAsset(300)
        val user = this.mockUser

        this.reservationDAO.save(Reservation(asset, user,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))

        this.reservationValidator.validate(Reservation(asset, user,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed`() {
        val asset = this.mockAsset(300)
        this.reservationValidator.validate(Reservation(asset, this.mockUser,
                OffsetDateTime.parse("2050-04-04T07:00:00+00:00"), OffsetDateTime.parse("2050-04-04T09:00:00+00:00")))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed (2)`() {
        val asset = this.mockAsset(300)
        this.reservationValidator.validate(Reservation(asset, this.mockUser,
                OffsetDateTime.parse("2050-04-04T07:00:00+00:00"), OffsetDateTime.parse("2050-04-04T07:30:00+00:00")))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed (3)`() {
        this.reservationValidator.validate(Reservation(this.mockAsset(300), this.mockUser,
                OffsetDateTime.parse("2050-04-04T14:00:00+00:00"), OffsetDateTime.parse("2050-04-04T16:00:00+00:00")))
    }

    @Test(expected = ReservationDurationException::class)
    fun `Should not create a reservation if interval is not within the same day`() {
        this.reservationValidator.validate(Reservation(this.mockAsset(300), this.mockUser,
                OffsetDateTime.parse("2050-04-04T10:00:00+00:00"), OffsetDateTime.parse("2050-04-05T12:00:00+00:00")))
    }

    @Test(expected = ReservationThresholdExceededException::class)
    fun `Should not create a reservation if interval beyond the threshold`() {
        val start = OffsetDateTime.now().plusDays(this.reservationThresholdInDays).plusMinutes(1)
        val end = start.plusMinutes(5)
        this.reservationValidator.validate(Reservation(this.mockAsset(300), this.mockUser, start, end))
    }

    @Test
    fun `Should create a reservation`() {
        val start = OffsetDateTime.parse("2050-04-04T11:00:00+00:00")
        val end = start.plusMinutes(20)
        val reservationDTO = Reservation(this.mockAsset(), this.mockUser, start, end)
        Assertions.assertThat(this.reservationValidator.test(reservationDTO)).isTrue()
        this.reservationValidator.validate(reservationDTO)
    }

    @Test(expected = TooManyReservationsException::class)
    fun `Should not be possible to make 3 reservations per day`() {
        val user = this.mockUser
        val asset = this.mockAsset()
        this.reservationDAO.saveAll((1..2).map {
            Reservation(-1, asset, user, OffsetDateTime.now(), OffsetDateTime.now(),
                    OffsetDateTime.now().minusMinutes(1), true)
        })

        this.reservationValidator.validate(Reservation(asset, user,
                OffsetDateTime.parse("2050-04-18T11:00:00+00:00"), OffsetDateTime.parse("2050-04-18T11:15:00+00:00")))
    }

    @Test(expected = TooManyReservationsException::class)
    fun `Should not be possible to make 3 reservations per day even if I delete one of them`() {
        val user = this.mockUser
        val asset = this.mockAsset()

        val reservations = this.reservationDAO.saveAll((1..2).map {
            Reservation(-1, asset, user, OffsetDateTime.now(), OffsetDateTime.now(),
                    OffsetDateTime.now().minusMinutes(1), true)
        })
        this.reservationService.deleteReservation(reservations.first())
        this.reservationValidator.validate(Reservation(asset, user,
                OffsetDateTime.parse("2050-04-18T11:00:00+00:00"), OffsetDateTime.parse("2050-04-18T11:15:00+00:00")))
    }

    /**************************************** UTILS ***********************************************************/

    private val mockUser: User
        get() = this.userDAO.save(this.userFactory.createAdminUser("gabrigiunchi", "aaaa", "Gabriele", "Giunchi"))

    private fun mockTimetable(): Timetable =
            this.timetableDAO.save(Timetable(gym = this.gym!!, openings = MockEntities.mockOpenings, closingDays = emptySet()))

    private fun mockAsset(maxReservationTime: Int = 20): Asset {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, maxReservationTime))
        return this.assetDAO.save(Asset("ciclett1", kind, this.gym!!))
    }

    private val mockGym: Gym
        get() = this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(MockEntities.mockCities[0])))

}
