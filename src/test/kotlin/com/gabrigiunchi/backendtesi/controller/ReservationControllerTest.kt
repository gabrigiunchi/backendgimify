package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.Reservation
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.model.dto.ReservationDTO
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.util.ApiUrls
import com.gabrigiunchi.backendtesi.util.DateDecorator
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class ReservationControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var reservationDAO: ReservationDAO

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Test
    fun `Should get all reservations`() {
        this.createReservations()
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.greaterThanOrEqualTo(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a reservation by its id`() {
        val reservation = this.reservationDAO.save(
                Reservation(this.mockAsset(), this.mockUser(),
                DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date))

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/${reservation.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(reservation.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get all reservations of an asset`() {
        val asset = this.assetDAO.findAll().toList()[0]
        val reservations = this.createReservations()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/${asset.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(reservations[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(reservations[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 when requesting the reservation of an asset that does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a reservation if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a reservation`() {
        val reservation = ReservationDTO(userID = this.mockUser().id, assetID = this.mockAsset().id,
                start = DateDecorator.of("2018-01-01T10:00:00+0000").date,
                end = DateDecorator.of("2018-01-01T12:00:00+0000").date)

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(reservation.userID)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a reservation`() {
        val savedId = this.reservationDAO.save(
                Reservation(this.mockAsset(), this.mockUser(),
                        DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date)
        ).id

        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete a reservation if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    private fun mockUser(): User {
        return this.userDAO.save(User("gabrigiunchi", "dsndja", "Gabriele", "Giunchi"))
    }

    private fun mockAsset(): Asset {
        System.out.println(this.assetDAO.findAll().toList().map { it.id })
        val asset =  this.assetDAO.save(Asset("tr01", this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get(), this.gymDAO.findAll().first()))
        System.out.println("asset id: ${asset.id}")
        return asset
    }


    private fun createReservations(): List<Reservation> {
        val user = this.mockUser()
        val assets = this.assetDAO.findAll().toList()

        return this.reservationDAO.saveAll(listOf(
                Reservation(assets[0], user, DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
                Reservation(assets[1], user, DateDecorator.of("2019-01-01T10:00:00+0000").date, DateDecorator.of("2019-01-01T12:00:00+0000").date),
                Reservation(assets[0], user, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
                Reservation(assets[3], user, DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
        )).toList()
    }
}