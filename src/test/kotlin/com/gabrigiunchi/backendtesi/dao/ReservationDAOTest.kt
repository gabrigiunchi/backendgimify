package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.util.DateDecorator
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ReservationDAOTest : AbstractControllerTest() {

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
    private lateinit var userFactory: UserFactory

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
                Reservation(asset, user, DateDecorator.now().date, DateDecorator.now().plusMinutes(20).date),
                Reservation(asset, user, DateDecorator.now().minusMinutes(200).date, DateDecorator.now().minusMinutes(20).date),
                Reservation(asset, user, DateDecorator.createDate("2018-01-01").date,
                        DateDecorator.createDate("2018-01-01").date),

                Reservation(asset, user, DateDecorator.of("2100-01-01T10:00:00+0000").date,
                        DateDecorator.of("2100-01-01T12:00:00+0000").date))).toList()

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(4)
        val result = this.reservationDAO.findByEndAfter(DateDecorator.now().date).toList()
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
                Reservation(asset1, user, DateDecorator.now().date, DateDecorator.now().plusMinutes(20).date),
                Reservation(asset1, user, DateDecorator.now().minusMinutes(200).date,
                        DateDecorator.now().minusMinutes(20).date),
                Reservation(asset2, user, DateDecorator.now().minusMinutes(200).date, DateDecorator.now().minusMinutes(20).date),
                Reservation(asset2, user, DateDecorator.createDate("2018-01-01").date,
                        DateDecorator.createDate("2018-01-01").date),

                Reservation(asset1, user, DateDecorator.of("2100-01-01T10:00:00+0000").date,
                        DateDecorator.of("2100-01-01T12:00:00+0000").date))).toList()

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(5)
        val result = this.reservationDAO.findByAssetAndEndAfter(asset1, DateDecorator.now().date).toList()
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
                Reservation(asset1, user1, DateDecorator.now().date, DateDecorator.now().plusMinutes(20).date),
                Reservation(asset1, user1, DateDecorator.now().minusMinutes(200).date,
                        DateDecorator.now().minusMinutes(20).date),

                Reservation(asset2, user2, DateDecorator.now().minusMinutes(200).date, DateDecorator.now().minusMinutes(20).date),
                Reservation(asset2, user2, DateDecorator.createDate("2018-01-01").date,
                        DateDecorator.createDate("2018-01-01").date),

                Reservation(asset1, user1, DateDecorator.of("2100-01-01T10:00:00+0000").date,
                        DateDecorator.of("2100-01-01T12:00:00+0000").date))).toList()

        Assertions.assertThat(this.reservationDAO.count()).isEqualTo(5)
        val result = this.reservationDAO.findByUserAndEndAfter(user1, DateDecorator.now().date).toList()
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
        return this.userDAO.save(this.userFactory.createRegularUser(username, "pass", "Gabriele", "Giunchi"))
    }
}