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
                DateDecorator.of("2050-04-04T08:00:00+0000").date, DateDecorator.of("2050-04-04T09:00:00+0000").date))
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
                DateDecorator.of("2050-04-04T14:00:00+0000").date, DateDecorator.of("2050-04-04T16:00:00+0000").date))
    }

    @Test(expected = BadRequestException::class)
    fun `Should not create a reservation if interval is not within the same day`() {
        val asset = this.createAsset(300)
        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T10:00:00+0000").date, DateDecorator.of("2050-04-05T12:00:00+0000").date))
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

    /************************************* ASSET AVAILABILITY ***********************************************/
    @Test
    fun `Should say if an asset is available in a given interval (ends before the other)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                DateDecorator.of("2050-04-04T08:00:00+0000").date,
                DateDecorator.of("2050-04-04T10:00:00+0000").date)).isTrue()
    }

    @Test
    fun `Should say if an asset is available in a given interval (starts after the other)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                DateDecorator.of("2050-04-04T16:00:00+0000").date,
                DateDecorator.of("2050-04-04T18:00:00+0000").date)).isTrue()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (starts within the other)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                DateDecorator.of("2050-04-04T11:10:00+0000").date,
                DateDecorator.of("2050-04-04T12:00:00+0000").date)).isFalse()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (end within the other)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                DateDecorator.of("2050-04-04T08:00:00+0000").date,
                DateDecorator.of("2050-04-04T11:02:00+0000").date)).isFalse()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (is fully contained in the other)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                DateDecorator.of("2050-04-04T11:02:00+0000").date,
                DateDecorator.of("2050-04-04T11:04:00+0000").date)).isFalse()
    }

    @Test
    fun `Should say if an asset is NOT available in a given interval (fully contains the other)`() {
        val asset = this.createAsset(300)

        this.reservationService.addReservation(ReservationDTO(this.user!!.id, asset.id,
                DateDecorator.of("2050-04-04T11:00:00+0000").date, DateDecorator.of("2050-04-04T11:30:00+0000").date))

        Assertions.assertThat(this.reservationService.isAssetAvailable(
                asset,
                DateDecorator.of("2050-04-04T10:00:00+0000").date,
                DateDecorator.of("2050-04-04T12:00:00+0000").date)).isFalse()
    }

    /****************************** SEARCH AVAILABLE ASSETS ***********************************************/

    @Test
    fun `Should return the available assets of a given kind in a given interval`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        val assets = this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssets(kind.id,
                DateDecorator.of("2050-04-04T10:15:00+0000").date, DateDecorator.of("2050-04-04T10:30:00+0000").date)

        Assertions.assertThat(result.size).isEqualTo(10)
        Assertions.assertThat(assets.map { it.id }.toSet()).isEqualTo(result.map { it.id }.toSet())
    }

    @Test
    fun `Should return empty list when searching the free assets if the interval is too big`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssets(kind.id,
                DateDecorator.of("2050-04-04T10:15:00+0000").date, DateDecorator.of("2050-04-04T11:15:00+0000").date)

        Assertions.assertThat(result.size).isEqualTo(0)
    }

    @Test
    fun `Should return the available assets of a given kind in a given interval in a given gym`() {
        val anotherGym = this.gymDAO.save(Gym("gym2", "adddress2", this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))))
        this.timetableDAO.save(Timetable(anotherGym, MockEntities.mockSchedules))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, if (it % 2 == 0) this.gym!! else anotherGym) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssetsInGym(
                kind.id,
                anotherGym.id,
                DateDecorator.of("2050-04-04T10:15:00+0000").date,
                DateDecorator.of("2050-04-04T10:30:00+0000").date)

        Assertions.assertThat(result.size).isEqualTo(5)
        Assertions.assertThat(result.all { it.gym.id == anotherGym.id }).isTrue()
    }

    @Test
    fun `Should return the available assets of a given kind in a given interval in a given region`() {
        val regions = this.regionDAO.saveAll(
                listOf(Region(RegionEnum.ABRUZZO), Region(RegionEnum.EMILIA_ROMAGNA), Region(RegionEnum.CALABRIA))
        ).toList()
        val gyms = this.gymDAO.saveAll(listOf(
                Gym("gym1", "address1", regions[0]),
                Gym("gym2", "address1", regions[1]),
                Gym("gym3", "address1", regions[2]),
                Gym("gym4", "address1", regions[1])
        )).toList()
        gyms.forEach { this.timetableDAO.save(Timetable(it, MockEntities.mockSchedules)) }

        val kinds = this.assetKindDAO.saveAll(
                listOf(
                        AssetKind(AssetKindEnum.CICLETTE, 20),
                        AssetKind(AssetKindEnum.TAPIS_ROULANT, 20),
                        AssetKind(AssetKindEnum.PRESSA, 20)
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

        // Search asset in EMILIA_ROMAGNA (gyms in EMILIA_ROMAGNA = [gyms[1], gyms[1])
        val targetRegion = regions[1]
        val targetKind = kinds[0]
        val result = this.reservationService.getAvailableAssetsInRegion(
                targetKind.id,
                targetRegion.id,
                DateDecorator.of("2050-04-04T10:15:00+0000").date,
                DateDecorator.of("2050-04-04T10:30:00+0000").date)

        val expectedResult = listOf(assets[1], assets[3])

        Assertions.assertThat(result.size).isEqualTo(2)
        Assertions.assertThat(result.all { it.gym.region.id == targetRegion.id }).isTrue()
        Assertions.assertThat(result.all { it.kind.id == targetKind.id }).isTrue()
        Assertions.assertThat(result.map { it.id }.toSet()).isEqualTo(expectedResult.map { it.id }.toSet())
    }

    @Test
    fun `Should return empty list when searching the free assets if the gym is closed`() {
        val anotherGym = this.gymDAO.save(Gym("gym2", "address2", this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))))
        this.timetableDAO.save(Timetable(anotherGym, MockEntities.mockSchedules))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, if (it % 2 == 0) this.gym!! else anotherGym) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val result = this.reservationService.getAvailableAssetsInGym(
                kind.id,
                anotherGym.id,
                DateDecorator.of("2050-04-05T10:15:00+0000").date,
                DateDecorator.of("2050-04-05T10:30:00+0000").date)

        Assertions.assertThat(result.size).isEqualTo(0)
    }



    @Test
    fun `Should return the available asset in a given interval considering also conflicts with other reservations`() {
        val anotherGym = this.gymDAO.save(Gym("gym2", "adddress2", this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))))
        this.timetableDAO.save(Timetable(anotherGym, MockEntities.mockSchedules))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        val assets = this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, if (it % 2 == 0) this.gym!! else anotherGym) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)

        val reservedAssets = assets.take(2).toList()
        this.reservationDAO.saveAll(listOf(
                Reservation(
                        reservedAssets.first(),
                        this.user!!,
                        DateDecorator.of("2050-04-04T10:15:00+0000").date,
                        DateDecorator.of("2050-04-04T10:30:00+0000").date
                ),

                Reservation(
                        reservedAssets[1],
                        this.user!!,
                        DateDecorator.of("2050-04-04T10:05:00+0000").date,
                        DateDecorator.of("2050-04-04T10:25:00+0000").date
                )
        ))

        val result = this.reservationService.getAvailableAssets(
                kind.id,
                DateDecorator.of("2050-04-04T10:10:00+0000").date,
                DateDecorator.of("2050-04-04T10:20:00+0000").date)

        Assertions.assertThat(result.size).isEqualTo(8)
        Assertions.assertThat(result.none { r -> reservedAssets.map { it.id }.contains(r.id) }).isTrue()
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when searching the free assets if the kind does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)
        this.reservationService.getAvailableAssetsInGym(
                -1,
                this.gym!!.id,
                DateDecorator.of("2050-04-05T10:15:00+0000").date,
                DateDecorator.of("2050-04-05T10:30:00+0000").date)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when searching the free assets if the gym does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)
        this.reservationService.getAvailableAssetsInGym(
                kind.id,
                -1,
                DateDecorator.of("2050-04-05T10:15:00+0000").date,
                DateDecorator.of("2050-04-05T10:30:00+0000").date)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throws an exception when searching the free assets if the region does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.assetDAO.saveAll((1..10).map { Asset("ciclette$it", kind, this.gym!!) })
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(10)
        this.reservationService.getAvailableAssetsInRegion(
                kind.id,
                -1,
                DateDecorator.of("2050-04-05T10:15:00+0000").date,
                DateDecorator.of("2050-04-05T10:30:00+0000").date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the start is after the end when searching for free assets`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.reservationService.getAvailableAssets(kind.id, DateDecorator.now().date, DateDecorator.now().minusMinutes(1).date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the after is equal to the end when searching for free assets`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.reservationService.getAvailableAssets(kind.id, DateDecorator.now().date, DateDecorator.now().date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the start is after the end when searching for free assets (gym filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.reservationService.getAvailableAssetsInGym(kind.id, this.gym!!.id,
                DateDecorator.now().date, DateDecorator.now().minusMinutes(1).date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the start is after the end when searching for free assets (region filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.reservationService.getAvailableAssetsInRegion(kind.id, this.gym!!.region.id,
                DateDecorator.now().date, DateDecorator.now().minusMinutes(1).date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the after is equal to the end when searching for free assets (gym filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.reservationService.getAvailableAssetsInGym(kind.id, this.gym!!.id, DateDecorator.now().date, DateDecorator.now().date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw an exception if the after is equal to the end when searching for free assets (region filter)`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        this.reservationService.getAvailableAssetsInRegion(kind.id, this.gym!!.region.id, DateDecorator.now().date, DateDecorator.now().date)
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