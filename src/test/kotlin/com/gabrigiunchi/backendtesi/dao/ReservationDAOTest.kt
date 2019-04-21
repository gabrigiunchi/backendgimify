package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
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
    private lateinit var regionDAO: RegionDAO

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
        val asset = this.createAsset()
        val user = this.createUser()
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


    private fun createAsset(): Asset {
        val gym = this.gymDAO.save(Gym("gym1", "address", this.regionDAO.save(Region(RegionEnum.EMILIA_ROMAGNA))))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        return this.assetDAO.save(Asset("ciclett1", kind, gym))
    }

    private fun createUser(): User {
        return this.userDAO.save(this.userFactory.createAdminUser("gabrigiunchi", "pass", "Gabriele", "Giunchi"))
    }
}