package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.exceptions.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.util.DateDecorator
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.time.OffsetDateTime
import java.util.*

class ReservationServiceTest : AbstractControllerTest() {

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
    private lateinit var reservationService: ReservationService

    @Autowired
    private lateinit var reservationDAO: ReservationDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @Autowired
    private lateinit var reservationLogDAO: ReservationLogDAO

    @Value("\${application.reservationThresholdInDays}")
    private var reservationThresholdInDays: Long = 0

    private var user: User? = null
    private var gym: Gym? = null

    @Before
    fun clearDB() {
        this.assetDAO.deleteAll()
        this.gymDAO.deleteAll()
        this.assetKindDAO.deleteAll()
        this.userDAO.deleteAll()
        this.reservationDAO.deleteAll()
        this.reservationLogDAO.deleteAll()

        this.user = this.userDAO.save(this.userFactory.createAdminUser("gabrigiunchi", "aaaa", "Gabriele", "Giunchi"))
        this.gym = this.mockGym()
        this.mockTimetable()
    }


    /************************************** CREATION *****************************************************************/

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the interval is in the past`() {
        val reservationDTO = ReservationDTOInput(this.user!!.id, -1,
                OffsetDateTime.now().minusMinutes(20), OffsetDateTime.now().minusMinutes(10))
        this.reservationService.addReservation(reservationDTO, reservationDTO.userID)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not create a reservation if the asset does not exist`() {
        val start = OffsetDateTime.now().plusMinutes(5)
        val end = start.plusMinutes(5)
        val reservationDTO = ReservationDTOInput(this.user!!.id, -1, start, end)
        this.reservationService.addReservation(reservationDTO, reservationDTO.userID)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not create a reservation if the user does not exist`() {
        val start = OffsetDateTime.now().plusMinutes(1)
        val end = start.plusMinutes(10)
        val reservationDTO = ReservationDTOInput(-1, this.mockAsset().id, start, end)
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the start is after the end`() {
        val start = OffsetDateTime.now().plusMinutes(200)
        val reservationDTO = ReservationDTOInput(this.user!!.id, this.mockAsset().id, start, start.minusMinutes(20))
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the start is equal to the end`() {
        val start = OffsetDateTime.now().plusMinutes(10)
        val reservationDTO = ReservationDTOInput(this.user!!.id, this.mockAsset().id, start, start)
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if the duration exceeds the maximum`() {
        val start = OffsetDateTime.now().plusMinutes(1)
        val end = start.plusMinutes(21)
        val reservationDTO = ReservationDTOInput(this.user!!.id, this.mockAsset().id, start, end)
        this.reservationService.addReservation(reservationDTO)
    }

    @Test(expected = ReservationConflictException::class)
    fun `Should not create a reservation if there is another one in that time`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T10:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))
    }

    @Test(expected = ReservationConflictException::class)
    fun `Should not create a reservation if there is another one in that time (edge case with same interval)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T07:00:00+00:00"), OffsetDateTime.parse("2050-04-04T09:00:00+00:00")))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed (2)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T07:00:00+00:00"), OffsetDateTime.parse("2050-04-04T07:30:00+00:00")))
    }

    @Test(expected = GymClosedException::class)
    fun `Should not create a reservation if the gym is closed (3)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T14:00:00+00:00"), OffsetDateTime.parse("2050-04-04T16:00:00+00:00")))
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if interval is not within the same day`() {
        val asset = this.mockAsset(300)
        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T10:00:00+00:00"), OffsetDateTime.parse("2050-04-05T12:00:00+00:00")))
    }

    @Test(expected = ReservationThresholdExceededException::class)
    fun `Should not create a reservation if interval beyond the threshold`() {
        val asset = this.mockAsset(300)
        val start = OffsetDateTime.now().plusDays(this.reservationThresholdInDays).plusMinutes(1)
        val end = start.plusMinutes(5)
        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id, start, end))
    }

    @Test
    fun `Should create a reservation`() {
        val now = Date()
        val start = OffsetDateTime.parse("2050-04-04T11:00:00+00:00")
        val end = start.plusMinutes(20)
        val reservationDTO = ReservationDTOInput(this.user!!.id, this.mockAsset().id, start, end)
        val savedReservation = this.reservationService.addReservation(reservationDTO)
        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.reservationLogDAO.count()).isEqualTo(1)
        val log = this.reservationLogDAO.findByReservationId(savedReservation.id).get()
        Assertions.assertThat(log.user.id).isEqualTo(this.user!!.id)
        Assertions.assertThat(log.reservationId).isEqualTo(savedReservation.id)
        Assertions.assertThat(log.date.time).isGreaterThanOrEqualTo(now.time)
    }

    @Test
    fun `Two users should create reservations`() {
        val user1 = this.user!!
        val user2 = this.userDAO.save(this.userFactory.createRegularUser("jnj", "sss", "kmk", "kk"))
        val asset1 = this.mockAsset(300)
        val asset2 = this.assetDAO.save(Asset("ciclette2", asset1.kind, this.gym!!))

        this.reservationService.addReservation(ReservationDTOInput(user1.id, asset1.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        this.reservationService.addReservation(ReservationDTOInput(user1.id, asset2.id,
                OffsetDateTime.parse("2050-04-04T11:30:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))

        this.reservationService.addReservation(ReservationDTOInput(user2.id, asset1.id,
                OffsetDateTime.parse("2050-04-18T11:30:00+00:00"), OffsetDateTime.parse("2050-04-18T12:00:00+00:00")))

        this.reservationService.addReservation(ReservationDTOInput(user2.id, asset2.id,
                OffsetDateTime.parse("2050-04-25T11:30:00+00:00"), OffsetDateTime.parse("2050-04-25T12:00:00+00:00")))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(4)
    }

    @Test
    fun `Should create a reservation (edge case with one reservation right after the other)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:30:00+00:00"), OffsetDateTime.parse("2050-04-04T12:00:00+00:00")))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(2)
    }

    @Test
    fun `Should create a reservation (edge case with one reservation at the same hour but a week later)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-11T11:00:00+00:00"), OffsetDateTime.parse("2050-04-11T11:30:00+00:00")))

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(2)
    }

    @Test(expected = TooManyReservationsException::class)
    fun `Should not be possible to make 3 reservations per day`() {
        val asset = this.mockAsset()

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:15:00+00:00")))

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-11T11:00:00+00:00"), OffsetDateTime.parse("2050-04-11T11:15:00+00:00")))

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-18T11:00:00+00:00"), OffsetDateTime.parse("2050-04-18T11:15:00+00:00")))
    }

    @Test(expected = TooManyReservationsException::class)
    fun `Should not be possible to make 3 reservations per day even if I delete one of them`() {
        val asset = this.mockAsset()

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:15:00+00:00")))

        val savedReservation = this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-11T11:00:00+00:00"), OffsetDateTime.parse("2050-04-11T11:15:00+00:00")))

        this.reservationDAO.delete(savedReservation)
        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(1)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-18T11:00:00+00:00"), OffsetDateTime.parse("2050-04-18T11:15:00+00:00")))
    }

    /************************************* ASSET AVAILABILITY ***********************************************/
    @Test
    fun `Should say if an asset is available in a given interval (ends before the other)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                OffsetDateTime.parse("2050-04-04T08:00:00+00:00"),
                OffsetDateTime.parse("2050-04-04T10:00:00+00:00"))).isTrue()
    }

    @Test
    fun `Should say if an asset is available in a given interval (starts after the other)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                OffsetDateTime.parse("2050-04-04T16:00:00+00:00"),
                OffsetDateTime.parse("2050-04-04T18:00:00+00:00"))).isTrue()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (starts within the other)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                OffsetDateTime.parse("2050-04-04T11:10:00+00:00"),
                OffsetDateTime.parse("2050-04-04T12:00:00+00:00"))).isFalse()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (end within the other)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                OffsetDateTime.parse("2050-04-04T08:00:00+00:00"),
                OffsetDateTime.parse("2050-04-04T11:02:00+00:00"))).isFalse()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (is fully contained in the other)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                OffsetDateTime.parse("2050-04-04T11:02:00+00:00"),
                OffsetDateTime.parse("2050-04-04T11:04:00+00:00"))).isFalse()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (fully contains the other)`() {
        val asset = this.mockAsset(300)

        this.reservationService.addReservation(ReservationDTOInput(this.user!!.id, asset.id,
                OffsetDateTime.parse("2050-04-04T11:00:00+00:00"), OffsetDateTime.parse("2050-04-04T11:30:00+00:00")))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                OffsetDateTime.parse("2050-04-04T10:00:00+00:00"),
                OffsetDateTime.parse("2050-04-04T12:00:00+00:00"))).isFalse()
    }

    /****************************** SEARCH AVAILABLE ASSETS ***********************************************/

    @Test
    fun `Should return the available assets of a given kind in a given interval`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val assets = this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssets(kind.id,
                OffsetDateTime.parse("2050-04-04T10:15:00+00:00"), OffsetDateTime.parse("2050-04-04T10:30:00+00:00"))

        Assertions.assertThat(result.size).isEqualTo(10)
        Assertions.assertThat(assets.map { it.id }.toSet()).isEqualTo(result.map { it.id }.toSet())
    }

    @Test
    fun `Should return empty list when searching the free assets if the interval is too big`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssets(kind.id,
                OffsetDateTime.parse("2050-04-04T10:15:00+00:00"), OffsetDateTime.parse("2050-04-04T11:15:00+00:00"))

        Assertions.assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `Should return empty list when searching the free assets if the interval is in the past`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssets(kind.id,
                OffsetDateTime.parse("2019-04-22T10:15:00+00:00"), OffsetDateTime.parse("2019-04-22T10:20:00+00:00"))

        Assertions.assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `Should return empty list when searching the free assets if the interval is in the past (city filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssetsInCity(
                kind.id,
                this.gym!!.city.id,
                OffsetDateTime.parse("2019-04-22T10:15:00+00:00"),
                OffsetDateTime.parse("2019-04-22T10:20:00+00:00"))

        Assertions.assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `Should return empty list when searching the free assets if the interval is in the past (gym filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssetsInGym(kind.id,
                this.gym!!.id,
                OffsetDateTime.parse("2019-04-22T10:15:00+00:00"),
                OffsetDateTime.parse("2019-04-22T10:20:00+00:00"))

        Assertions.assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `Should return the available assets of a given kind in a given interval in a given gym`() {
        val anotherGym = this.gymDAO.save(
                Gym("gym2", "adddress2", this.cityDAO.save(MockEntities.mockCities[0])))
        this.timetableDAO.save(Timetable(anotherGym, MockEntities.mockSchedules))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, if (it % 2 == 0) this.gym!! else anotherGym) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssetsInGym(
                kind.id,
                anotherGym.id,
                OffsetDateTime.parse("2050-04-04T10:15:00+00:00"),
                OffsetDateTime.parse("2050-04-04T10:30:00+00:00"))

        Assertions.assertThat(result.size).isEqualTo(5)
        Assertions.assertThat(result.all { it.gym.id == anotherGym.id }).isTrue()
    }

    @Test
    fun `Should return the available assets of a given kind in a given interval in a given city`() {
        val cities = this.cityDAO.saveAll(MockEntities.mockCities.take(3)).toList()
        val gyms = this.gymDAO.saveAll(listOf(
                Gym("gym1", "address1", cities[0]),
                Gym("gym2", "address1", cities[1]),
                Gym("gym3", "address1", cities[2]),
                Gym("gym4", "address1", cities[1])
        )).toList()
        gyms.forEach { this.timetableDAO.save(Timetable(it, MockEntities.mockSchedules)) }

        val kinds = this.assetKindDAO.saveAll(
                listOf(
                        AssetKind(AssetKindEnum.CICLE, 20),
                        AssetKind(AssetKindEnum.TREADMILLS, 20),
                        AssetKind(AssetKindEnum.BENCH, 20)
                )
        ).toList()

        val assets = this.assetDAO.saveAll(listOf(
                Asset("ciclette1", kinds[0], gyms[0]),
                Asset("ciclette2", kinds[0], gyms[1]),
                Asset("ciclette3", kinds[0], gyms[2]),
                Asset("ciclette4", kinds[0], gyms[3]),
                Asset("pressa1", kinds[2], gyms[3]),
                Asset("tapis roulant 1", kinds[1], gyms[1]),
                Asset("tapis roulant 2", kinds[1], gyms[2])
        )).toList()

        val targetCity = cities[1]
        val targetKind = kinds[0]
        val result = this.reservationService.getAvailableAssetsInCity(
                targetKind.id,
                targetCity.id,
                OffsetDateTime.parse("2050-04-04T10:15:00+00:00"),
                OffsetDateTime.parse("2050-04-04T10:30:00+00:00"))

        val expectedResult = listOf(assets[1], assets[3])

        Assertions.assertThat(result.size).isEqualTo(2)
        Assertions.assertThat(result.all { it.gym.city.id == targetCity.id }).isTrue()
        Assertions.assertThat(result.all { it.kind.id == targetKind.id }).isTrue()
        Assertions.assertThat(result.map { it.id }.toSet()).isEqualTo(expectedResult.map { it.id }.toSet())
    }

    @Test
    fun `Should return empty list when searching the free assets if the gym is closed`() {
        val anotherGym = this.gymDAO.save(Gym("gym2", "address2", this.cityDAO.save(MockEntities.mockCities[0])))
        this.timetableDAO.save(Timetable(anotherGym, MockEntities.mockSchedules))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, if (it % 2 == 0) this.gym!! else anotherGym) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssetsInGym(
                kind.id,
                anotherGym.id,
                OffsetDateTime.parse("2050-04-05T12:15:00+00:00"),
                OffsetDateTime.parse("2050-04-05T12:30:00+00:00"))

        Assertions.assertThat(result.isEmpty()).isTrue()

        val result2 = this.reservationService.getAvailableAssetsInGym(
                kind.id,
                anotherGym.id,
                OffsetDateTime.parse("2050-04-05T18:15:00+06:00"),
                OffsetDateTime.parse("2050-04-05T18:30:00+06:00"))

        Assertions.assertThat(result2.isEmpty()).isTrue()
    }

    @Test
    fun `Should return the available asset in a given interval considering also conflicts with other reservations`() {
        val anotherGym = this.gymDAO.save(
                Gym("gym2", "adddress2", this.cityDAO.save(MockEntities.mockCities[0])))
        this.timetableDAO.save(Timetable(anotherGym, MockEntities.mockSchedules))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val assets = this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, if (it % 2 == 0) this.gym!! else anotherGym) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val reservedAssets = assets.take(2).toList()
        this.reservationDAO.saveAll(listOf(
                Reservation(
                        reservedAssets.first(),
                        this.user!!,
                        OffsetDateTime.parse("2050-04-04T10:15:00+00:00"),
                        OffsetDateTime.parse("2050-04-04T10:30:00+00:00")
                ),

                Reservation(
                        reservedAssets[1],
                        this.user!!,
                        OffsetDateTime.parse("2050-04-04T10:05:00+00:00"),
                        OffsetDateTime.parse("2050-04-04T10:25:00+00:00")
                )
        ))

        val result = this.reservationService.getAvailableAssets(
                kind.id,
                OffsetDateTime.parse("2050-04-04T10:10:00+00:00"),
                OffsetDateTime.parse("2050-04-04T10:20:00+00:00"))

        Assertions.assertThat(result.size).isEqualTo(8)
        Assertions.assertThat(result.none { r -> reservedAssets.map { it.id }.contains(r.id) }).isTrue()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when searching the free assets if the kind does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)
        this.reservationService.getAvailableAssetsInGym(
                -1,
                this.gym!!.id,
                OffsetDateTime.parse("2050-04-05T10:15:00+00:00"),
                OffsetDateTime.parse("2050-04-05T10:30:00+00:00"))
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when searching the free assets if the gym does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)
        this.reservationService.getAvailableAssetsInGym(
                kind.id,
                -1,
                OffsetDateTime.parse("2050-04-05T10:15:00+00:00"),
                OffsetDateTime.parse("2050-04-05T10:30:00+00:00"))
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throws an exception when searching the free assets if the city does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)
        this.reservationService.getAvailableAssetsInCity(
                kind.id,
                -1,
                OffsetDateTime.parse("2050-04-05T10:15:00+00:00"),
                OffsetDateTime.parse("2050-04-05T10:30:00+00:00"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the start is after the end when searching for free assets`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.reservationService.getAvailableAssets(kind.id, OffsetDateTime.now(), OffsetDateTime.now().minusMinutes(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the after is equal to the end when searching for free assets`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val now = OffsetDateTime.now()
        this.reservationService.getAvailableAssets(kind.id, now, now)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the start is after the end when searching for free assets (gym filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.reservationService.getAvailableAssetsInGym(kind.id, this.gym!!.id,
                OffsetDateTime.now(), OffsetDateTime.now().minusMinutes(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the start is after the end when searching for free assets (city filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.reservationService.getAvailableAssetsInCity(kind.id, this.gym!!.city.id,
                OffsetDateTime.now(), OffsetDateTime.now().minusMinutes(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the after is equal to the end when searching for free assets (gym filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val now = OffsetDateTime.now()
        this.reservationService.getAvailableAssetsInGym(kind.id, this.gym!!.id, now, now)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the after is equal to the end when searching for free assets (city filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val now = OffsetDateTime.now()
        this.reservationService.getAvailableAssetsInCity(kind.id, this.gym!!.city.id, now, now)
    }

    @Test
    fun `Should return empty list when searching for available assets if the interval is beyond the threshold`() {
        this.timetableDAO.deleteAll()
        this.timetableDAO.save(Timetable(this.gym!!, MockEntities.wildcardSchedules))
        val asset = this.mockAsset(300)
        val start = OffsetDateTime.now().plusDays(this.reservationThresholdInDays).plusMinutes(1)
        val end = start.plusMinutes(5)

        // Before the threshold
        Assertions.assertThat(this.reservationService.getAvailableAssets(asset.kind.id,
                start.minusDays(1), start.minusDays(1).plusMinutes(1)).size).isNotEqualTo(0)

        // After threshold
        Assertions.assertThat(this.reservationService.getAvailableAssets(asset.kind.id, start, end).size).isEqualTo(0)
    }

    @Test
    fun `Should return empty list when searching for available assets if the interval is beyond the threshold (city filter)`() {
        this.timetableDAO.deleteAll()
        this.timetableDAO.save(Timetable(this.gym!!, MockEntities.wildcardSchedules))
        val asset = this.mockAsset(300)
        val start = OffsetDateTime.now().plusDays(this.reservationThresholdInDays).plusMinutes(1)
        val end = start.plusMinutes(5)
        val cityId = this.gym!!.city.id

        // Before the threshold
        Assertions.assertThat(this.reservationService.getAvailableAssetsInCity(asset.kind.id, cityId,
                start.minusDays(1), start.minusDays(1).plusMinutes(1)).size).isNotEqualTo(0)

        // After threshold
        Assertions.assertThat(this.reservationService.getAvailableAssetsInCity(asset.kind.id, cityId, start, end).size)
                .isEqualTo(0)
    }

    @Test
    fun `Should return empty list when searching for available assets if the interval is beyond the threshold (gym filter)`() {
        this.timetableDAO.deleteAll()
        this.timetableDAO.save(Timetable(this.gym!!, MockEntities.wildcardSchedules))
        val asset = this.mockAsset(300)
        val start = OffsetDateTime.now().plusDays(this.reservationThresholdInDays).plusMinutes(1)
        val end = start.plusMinutes(5)
        val gymId = this.gym!!.id

        // Before the threshold
        Assertions.assertThat(this.reservationService.getAvailableAssetsInGym(asset.kind.id, gymId,
                start.minusDays(1), start.minusDays(1).plusMinutes(1)).size).isNotEqualTo(0)

        // After threshold
        Assertions.assertThat(this.reservationService.getAvailableAssetsInGym(asset.kind.id, gymId, start, end).size)
                .isEqualTo(0)
    }

    /*************************************** OTHERS **********************************************************/

    @Test(expected = ResourceNotFoundException::class)
    fun `Should check if a user own a reservation and throws an exception if the reservation belongs to another user`() {
        val r = this.reservationDAO.save(Reservation(this.mockAsset(), this.user!!, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(20)))
        val anotherUser = this.userDAO.save(this.userFactory.createAdminUser("dasdaas", "aaaa", "Gabriele", "Giunchi"))

        this.reservationService.getReservationOfUser(anotherUser, r.id)
    }

    @Test
    fun `Should check if a user own a reservation and not throw an exception if the reservation belongs to the user`() {
        val r = this.reservationDAO.save(Reservation(this.mockAsset(), this.user!!, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(20)))
        this.reservationService.getReservationOfUser(this.user!!, r.id)
    }


    @Test
    fun `Should count the reservations made by a user in a given date`() {
        val user = this.user!!
        this.reservationLogDAO.saveAll(listOf(
                ReservationLog(-1, user, -1, DateDecorator.of("2019-01-01T10:00:00+0000").date),
                ReservationLog(-1, user, -1, DateDecorator.of("2019-01-02T10:00:00+0000").date),
                ReservationLog(-1, user, -1, DateDecorator.of("2019-04-23T23:59:59+0000").date),
                ReservationLog(-1, user, -1, DateDecorator.of("2019-04-24T10:00:00+0000").date),
                ReservationLog(-1, user, -1, DateDecorator.of("2019-04-24T12:00:00+0000").date),
                ReservationLog(-1, user, -1, DateDecorator.of("2019-04-25T10:00:00+0000").date),
                ReservationLog(-1, user, -1, DateDecorator.of("2019-04-25T00:00:01+0000").date)
        ))

        val date = DateDecorator.createDate("2019-04-24").date
        Assertions.assertThat(this.reservationService.numberOfReservationsMadeByUserInDate(user, date)).isEqualTo(2)
    }

    @Test
    fun `Should delete the reservation of a user`() {
        val start = OffsetDateTime.parse("2050-04-04T11:00:00+00:00")
        val end = start.plusMinutes(20)
        val reservationDTO = ReservationDTOInput(this.user!!.id, this.mockAsset().id, start, end)
        val savedReservation = this.reservationService.addReservation(reservationDTO)

        this.reservationService.deleteReservationOfUser(this.user!!, savedReservation.id)
        Assertions.assertThat(this.reservationDAO.findById(savedReservation.id).isEmpty).isTrue()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not delete the reservation of a user if the reservation is not of the user`() {
        val user1 = this.user!!
        val user2 = this.userDAO.save(this.userFactory.createRegularUser("dnjasda", "nj", "s", "", ""))
        val start = OffsetDateTime.parse("2050-04-04T11:00:00+00:00")
        val end = start.plusMinutes(20)
        val reservationDTO = ReservationDTOInput(user2.id, this.mockAsset().id, start, end)
        val savedReservation = this.reservationService.addReservation(reservationDTO)

        this.reservationService.deleteReservationOfUser(user1, savedReservation.id)
    }

    /******************************* CHECK SPECIFIC ASSET AVAILABILITY *******************************/

    @Test
    fun `Should return false if the start is after the end or the start is in the past or if the start is beyond the threshold`() {
        this.timetableDAO.deleteAll()
        this.timetableDAO.save(Timetable(this.gym!!, MockEntities.wildcardSchedules, emptySet()))
        Assertions.assertThat(
                this.reservationService.isAssetAvailable(
                        this.mockAsset().id,
                        OffsetDateTime.parse("2050-04-04T11:20:00+00:00"),
                        OffsetDateTime.parse("2050-04-04T11:10:00+00:00")
                )
        ).isFalse()

        Assertions.assertThat(
                this.reservationService.isAssetAvailable(
                        this.mockAsset().id,
                        OffsetDateTime.parse("2017-04-04T11:00:00+00:00"),
                        OffsetDateTime.parse("2017-04-04T11:10:00+00:00")
                )
        ).isFalse()


        val start = OffsetDateTime.now().plusDays(this.reservationThresholdInDays).plusMinutes(1)
        val end = start.plusMinutes(5)
        Assertions.assertThat(
                this.reservationService.isAssetAvailable(
                        this.mockAsset().id,
                        start,
                        end
                )
        ).isFalse()
    }

    @Test
    fun `Should return false if the gym is closed`() {
        Assertions.assertThat(
                this.reservationService.isAssetAvailable(
                        this.mockAsset().id,
                        OffsetDateTime.parse("2050-04-09T11:00:00+00:00"),
                        OffsetDateTime.parse("2050-04-09T11:10:00+00:00")
                )
        ).isFalse()
    }

    @Test
    fun `Should return false if there is another reservation at that time`() {
        val start = OffsetDateTime.parse("2050-04-04T11:10:00+00:00")
        val end = OffsetDateTime.parse("2050-04-04T11:20:00+00:00")
        val asset = this.mockAsset()
        this.reservationDAO.save(Reservation(asset, this.user!!, start, end))
        Assertions.assertThat(this.reservationService.isAssetAvailable(asset.id, start.minusMinutes(1), end)).isFalse()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception if the asset does not exist when checking its availability`() {
        this.reservationService.isAssetAvailable(-1,
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusMinutes(1))
    }

    @Test
    fun `Should return true if the asset is available`() {
        val start = OffsetDateTime.parse("2050-04-04T11:00:00+00:00")
        val end = OffsetDateTime.parse("2050-04-04T11:10:00+00:00")
        val asset = this.mockAsset()
        Assertions.assertThat(this.reservationService.isAssetAvailable(asset.id, start.minusMinutes(1), end)).isTrue()
    }

    /**************************************** UTILS ***********************************************************/

    private fun mockTimetable(): Timetable {
        return this.timetableDAO.save(Timetable(gym = this.gym!!, openings = MockEntities.mockSchedules, closingDays = emptySet()))
    }

    private fun mockAsset(maxReservationTime: Int = 20): Asset {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, maxReservationTime))
        return this.assetDAO.save(Asset("ciclett1", kind, this.gym!!))
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(MockEntities.mockCities[0])))
    }
}