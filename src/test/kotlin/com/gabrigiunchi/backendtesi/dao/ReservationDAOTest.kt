package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.model.entities.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.service.UserService
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime

class ReservationDAOTest : BaseTest()
{

    @Autowired
    private lateinit var reservationDAO: ReservationDAO

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Before
    fun clearDB() {
        this.assetDAO.deleteAll()
        this.assetKindDAO.deleteAll()
        this.gymDAO.deleteAll()
        this.userDAO.deleteAll()
        this.reservationDAO.deleteAll()
    }

    @Test
    fun `Should return the reservations in the furure`() {
        val asset = this.mockAsset("ciclette1")
        val user = this.mockUser("gabrigiunchi")
        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(asset, user, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(20)),
                Reservation(asset, user, OffsetDateTime.now().minusMinutes(200), OffsetDateTime.now().minusMinutes(20)),
                Reservation(asset, user, OffsetDateTime.parse("2018-01-01T00:00:00+00:00"), OffsetDateTime.parse("2018-01-01T00:00:00+00:00")),

                Reservation(asset, user, OffsetDateTime.parse("2100-01-01T10:00:00+00:00"),
                        OffsetDateTime.parse("2100-01-01T12:00:00+00:00")))).toList()

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(4)
        val result = this.reservationDAO.findByEndAfter(OffsetDateTime.now()).toList()
        Assertions.assertThat(result.size).isEqualTo(2)
        Assertions.assertThat(result[0].id).isEqualTo(reservations.first().id)
        Assertions.assertThat(result[1].id).isEqualTo(reservations.last().id)
    }

    @Test
    fun `Should return the reservations for an asset with end after a date`() {
        val asset1 = this.mockAsset("ciclette1")
        val asset2 = this.mockAsset("ciclette2")
        val user = this.mockUser("gabrigiunchi")
        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(asset1, user, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(20)),
                Reservation(asset1, user, OffsetDateTime.now().minusMinutes(200),
                        OffsetDateTime.now().minusMinutes(20)),
                Reservation(asset2, user, OffsetDateTime.now().minusMinutes(200), OffsetDateTime.now().minusMinutes(20)),
                Reservation(asset2, user, OffsetDateTime.parse("2018-01-01T00:00:00+00:00"), OffsetDateTime.parse("2018-01-01T00:00:00+00:00")),

                Reservation(asset1, user, OffsetDateTime.parse("2100-01-01T10:00:00+00:00"),
                        OffsetDateTime.parse("2100-01-01T12:00:00+00:00")))).toList()

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(5)
        val result = this.reservationDAO.findByAssetAndEndAfter(asset1, OffsetDateTime.now()).toList()
        Assertions.assertThat(result.size).isEqualTo(2)
        Assertions.assertThat(result[0].id).isEqualTo(reservations.first().id)
        Assertions.assertThat(result[1].id).isEqualTo(reservations.last().id)
        Assertions.assertThat(result.none { it.asset.id == asset2.id }).isTrue()
    }

    @Test
    fun `Should return the reservations for a specific user and end after`() {
        val asset1 = this.mockAsset("ciclette1")
        val asset2 = this.mockAsset("ciclette2")
        val user1 = this.mockUser("gabrigiunchi")
        val user2 = this.mockUser("dnasnajda")
        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(asset1, user1, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(20)),
                Reservation(asset1, user1, OffsetDateTime.now().minusMinutes(200),
                        OffsetDateTime.now().minusMinutes(20)),

                Reservation(asset2, user2, OffsetDateTime.now().minusMinutes(200), OffsetDateTime.now().minusMinutes(20)),
                Reservation(asset2, user2, OffsetDateTime.parse("2018-01-01T00:00:00+00:00"), OffsetDateTime.parse("2018-01-01T00:00:00+00:00")),

                Reservation(asset1, user1, OffsetDateTime.parse("2100-01-01T10:00:00+00:00"),
                        OffsetDateTime.parse("2100-01-01T12:00:00+00:00")))).toList()

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(5)
        val result = this.reservationDAO.findByUserAndEndAfterAndActive(user1, OffsetDateTime.now(), true).toList()
        Assertions.assertThat(result.size).isEqualTo(2)
        Assertions.assertThat(result[0].id).isEqualTo(reservations.first().id)
        Assertions.assertThat(result[1].id).isEqualTo(reservations.last().id)
        Assertions.assertThat(result.none { it.user.id == user2.id }).isTrue()
    }

    private fun mockAsset(name: String): Asset {
        val gym = this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(City(CityEnum.MIAMI))))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        return this.assetDAO.save(Asset(name, kind, gym))
    }

    private fun mockUser(username: String): User {
        return this.userDAO.save(this.userService.createRegularUser(username, "pass", "Gabriele", "Giunchi"))
    }
}