package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.entities.*
import com.gabrigiunchi.backendtesi.model.time.Timetable
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.OffsetDateTime

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

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @Value("\${application.reservationThresholdInDays}")
    private var reservationThresholdInDays: Long = 0


    @Before
    fun clearDB() {
        this.reservationDAO.deleteAll()
        this.gymDAO.deleteAll()
        this.assetDAO.deleteAll()
        this.timetableDAO.deleteAll()
    }

    @Test
    fun `Should get all reservations`() {
        this.mockReservations()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/page/0/size/20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.greaterThanOrEqualTo(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a reservation by its id`() {
        val reservation = this.reservationDAO.save(
                Reservation(this.mockAsset(this.mockGym()), this.mockUser(),
                        OffsetDateTime.parse("2018-01-01T10:00:00+00:00"),
                        OffsetDateTime.parse("2018-01-01T12:00:00+00:00")))

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/${reservation.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(reservation.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(reservation.user.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.asset.id", Matchers.`is`(reservation.asset.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get all active reservations of an asset`() {
        val reservations = this.mockReservations()
        reservations[0].active = false
        this.reservationDAO.save(reservations[0])
        val asset = reservations[0].asset

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/${asset.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(reservations[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 when requesting the reservations of an asset that does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a reservation if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("reservation -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a reservation`() {
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val asset = this.mockAsset(gym)

        val reservation = ReservationDTOInput(
                userID = this.mockUser().id,
                assetID = asset.id,
                start = OffsetDateTime.parse("2050-04-04T10:00:00+00:00"),
                end = OffsetDateTime.parse("2050-04-04T10:15:00+00:00"))

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(reservation.userID)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.asset.id", Matchers.`is`(reservation.assetID)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a reservation if interval is in the past`() {
        val gym = this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(City(CityEnum.MIAMI))))
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val asset = this.mockAsset(gym)

        val reservation = ReservationDTOInput(
                userID = this.mockUser().id,
                assetID = asset.id,
                start = OffsetDateTime.parse("2019-04-22T10:00:00+00:00"),
                end = OffsetDateTime.parse("2019-04-22T11:00:00+00:00"))

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a reservation if the gym is closed`() {
        val gym = this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(MockEntities.mockCities[0])))
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val asset = this.mockAsset(gym)

        val reservation = ReservationDTOInput(
                userID = this.mockUser().id,
                assetID = asset.id,
                start = OffsetDateTime.parse("2050-04-05T12:00:00+00:00"),
                end = OffsetDateTime.parse("2050-04-05T12:15:00+00:00"))

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isForbidden)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a reservation if there is another one at the same time`() {
        this.userDAO.deleteAll()
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val asset = this.mockAsset(gym)
        val user = this.mockUser()

        this.reservationDAO.save(Reservation(asset, user, OffsetDateTime.parse("2050-04-04T11:00:00+00:00"),
                OffsetDateTime.parse("2050-04-04T11:10:00+00:00")))

        val reservation = ReservationDTOInput(
                userID = user.id,
                assetID = asset.id,
                start = OffsetDateTime.parse("2050-04-04T10:55:00+00:00"),
                end = OffsetDateTime.parse("2050-04-04T11:05:00+00:00"))

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a reservation if the duration exceed the maximum for that kind`() {
        this.userDAO.deleteAll()
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val asset = this.mockAsset(gym)

        val start = OffsetDateTime.parse("2050-04-04T11:00:00+00:00")
        val end = start.plusMinutes((asset.kind.maxReservationTime + 1).toLong())
        val reservation = ReservationDTOInput(
                userID = this.mockUser().id,
                assetID = asset.id,
                start = start,
                end = end)

        val expectedMessage = "reservation duration exceeds maximum (max=${asset.kind.maxReservationTime} minutes)"
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`(expectedMessage)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not be able to make 3 reservations per day`() {
        this.userDAO.deleteAll()
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val asset = this.mockAsset(gym)
        val user = this.mockUser()

        // create 3 reservations for the same asset in different days
        val start = OffsetDateTime.parse("2050-04-04T10:55:00+00:00")
        val reservations = (1..3).map(Int::toLong).map {
            ReservationDTOInput(
                    userID = user.id,
                    assetID = asset.id,
                    start = start.plusDays(it),
                    end = start.plusDays(it).plusMinutes(20))
        }

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservations[0])))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andDo(MockMvcResultHandlers.print())

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservations[1])))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andDo(MockMvcResultHandlers.print())

        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservations[2])))
                .andExpect(MockMvcResultMatchers.status().isForbidden)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return bad request if the reservation is beyond the threshold`() {
        this.userDAO.deleteAll()
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.wildcardOpenings)) // create a gym that is always open
        val asset = this.mockAsset(gym) // create an asset in the gym
        val user = this.mockUser() // create a mock user

        // create an interval beyond the threshold
        val start = OffsetDateTime.now().plusDays(this.reservationThresholdInDays).plusMinutes(10)
        val end = start.plusMinutes(5)
        val reservation = ReservationDTOInput(
                userID = user.id,
                assetID = asset.id,
                start = start,
                end = end)

        // make REST API call
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("The reservation is beyond the threshold")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a reservation`() {
        val savedId = this.reservationDAO.save(
                Reservation(this.mockAsset(this.mockGym()), this.mockUser(),
                        OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T12:00:00+00:00"))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("reservation -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    /************************************** MY RESERVATIONS *****************************************************/

    @Test
    fun `Should get all my reservations`() {
        this.userDAO.deleteAll()
        val user1 = this.mockUser("gabrigiunchi")
        val user2 = this.mockUser("fragiunchi")
        val gym = this.mockGym()
        val assets = listOf(
                this.mockAsset(gym, "a1"),
                this.mockAsset(gym, "a2"),
                this.mockAsset(gym, "a3"),
                this.mockAsset(gym, "a4")
        )

        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(assets[0], user1, OffsetDateTime.parse("2017-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T12:00:00+00:00")),
                Reservation(assets[1], user1, OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2019-01-01T12:00:00+00:00")),
                Reservation(assets[0], user1, OffsetDateTime.parse("2019-02-01T10:00:00+00:00"), OffsetDateTime.parse("2019-02-01T12:00:00+00:00")),
                Reservation(-1, assets[3], user1,
                        OffsetDateTime.parse("2020-03-01T10:00:00+00:00"),
                        OffsetDateTime.parse("2019-03-01T12:00:00+00:00"),
                        OffsetDateTime.now(), false),
                Reservation(assets[0], user2, OffsetDateTime.parse("2019-02-01T10:00:00+00:00"), OffsetDateTime.parse("2019-02-01T12:00:00+00:00")),
                Reservation(assets[3], user2, OffsetDateTime.parse("2019-03-01T10:00:00+00:00"), OffsetDateTime.parse("2019-03-01T12:00:00+00:00"))
        )).toList()

        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/page/0/size/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`(reservations[2].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.`is`(reservations[1].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return the number reservations I made`() {
        this.userDAO.deleteAll()
        val user = this.mockUser("gabrigiunchi")
        val asset = this.mockAsset(this.mockGym())
        val reservations = this.reservationDAO.saveAll(
                (1..4).map { Reservation(asset, user, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(1)) })

        val inactive = reservations.first()
        inactive.active = false
        this.reservationDAO.save(inactive)
        Assertions.assertThat(this.reservationDAO.findById(inactive.id).get().active).isFalse()

        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get all my future reservations`() {
        this.userDAO.deleteAll()
        val user1 = this.mockUser("gabrigiunchi")
        val user2 = this.mockUser("fragiunchi")
        val gym = this.mockGym()
        val assets = listOf(
                this.mockAsset(gym, "a1"),
                this.mockAsset(gym, "a2"),
                this.mockAsset(gym, "a3"),
                this.mockAsset(gym, "a4")
        )

        val start = OffsetDateTime.now().plusDays(1)
        val end = start.plusMinutes(1)
        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(assets[0], user1, OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T12:00:00+00:00")),
                Reservation(assets[0], user1, start, end),
                Reservation(assets[3], user1, start, end),
                Reservation(assets[3], user1, start, end, false),

                Reservation(assets[3], user2, start, end),
                Reservation(assets[3], user2, start, end),
                Reservation(assets[3], user2, start, end, false)
        )).toList()

        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/future")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(reservations[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(reservations[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get one of my reservations by id`() {
        this.userDAO.deleteAll()
        val user1 = this.mockUser("gabrigiunchi")
        val user2 = this.mockUser("fragiunchi")
        val gym = this.mockGym()
        val assets = listOf(
                this.mockAsset(gym, "a1"),
                this.mockAsset(gym, "a2"),
                this.mockAsset(gym, "a3"),
                this.mockAsset(gym, "a4")
        )

        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(assets[0], user1, OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T12:00:00+00:00")),
                Reservation(assets[1], user1, OffsetDateTime.parse("2022-01-01T10:00:00+00:00"), OffsetDateTime.parse("2022-01-01T12:00:00+00:00")),
                Reservation(assets[0], user2, OffsetDateTime.parse("2019-02-01T10:00:00+00:00"), OffsetDateTime.parse("2019-02-01T12:00:00+00:00")),
                Reservation(assets[3], user2, OffsetDateTime.parse("2019-03-01T10:00:00+00:00"), OffsetDateTime.parse("2019-03-01T12:00:00+00:00"))
        )).toList()

        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/${reservations[0].id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(reservations[0].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 when requesting one of my reservations by id if it does not exist`() {
        this.userDAO.deleteAll()
        val user = this.mockUser("gabrigiunchi")
        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("reservation -1 does not exist or is not owned by user ${user.id}")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 when requesting one of my reservations by id if I do not own it`() {
        this.userDAO.deleteAll()
        val user1 = this.mockUser("gabrigiunchi")
        val user2 = this.mockUser("fragiunchi")
        val gym = this.mockGym()
        val assets = listOf(
                this.mockAsset(gym, "a1"),
                this.mockAsset(gym, "a2"),
                this.mockAsset(gym, "a3"),
                this.mockAsset(gym, "a4")
        )

        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(assets[0], user1, OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T12:00:00+00:00")),
                Reservation(assets[1], user1, OffsetDateTime.parse("2022-01-01T10:00:00+00:00"), OffsetDateTime.parse("2022-01-01T12:00:00+00:00")),
                Reservation(assets[0], user2, OffsetDateTime.parse("2019-02-01T10:00:00+00:00"), OffsetDateTime.parse("2019-02-01T12:00:00+00:00")),
                Reservation(assets[3], user2, OffsetDateTime.parse("2019-03-01T10:00:00+00:00"), OffsetDateTime.parse("2019-03-01T12:00:00+00:00"))
        )).toList()

        val id = reservations[2].id
        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/$id")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("reservation $id does not exist or is not owned by user ${user1.id}")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a reservation with the 'me' REST API`() {
        this.userDAO.deleteAll()
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val asset = this.mockAsset(gym)

        val reservation = ReservationDTOInput(
                userID = this.mockUser().id,
                assetID = asset.id,
                start = OffsetDateTime.parse("2050-04-04T10:00:00+00:00"),
                end = OffsetDateTime.parse("2050-04-04T10:10:00+00:00"))

        mockMvc.perform(MockMvcRequestBuilders.post("${ApiUrls.RESERVATIONS}/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(reservation)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(reservation.userID)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.asset.id", Matchers.`is`(reservation.assetID)))
                .andDo(MockMvcResultHandlers.print())
    }


    @Test
    fun `Should delete one of my reservations`() {
        this.userDAO.deleteAll()
        val savedId = this.reservationDAO.save(
                Reservation(this.mockAsset(this.mockGym()), this.mockUser(),
                        OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T10:15:00+00:00"))
        ).id

        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/me/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.reservationDAO.findById(savedId).get().active).isFalse()
    }

    @Test
    fun `Should not delete one of my reservations if it does not exist`() {
        this.userDAO.deleteAll()
        val user = this.mockUser("gabrigiunchi")
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/me/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`(
                        "reservation -1 does not exist or is not owned by user ${user.id}")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete one of my reservations if I do not own it`() {
        this.userDAO.deleteAll()
        val user1 = this.mockUser("gabrigiunchi")
        val user2 = this.mockUser("fragiunchi")
        val gym = this.mockGym()
        val assets = listOf(
                this.mockAsset(gym, "a1"),
                this.mockAsset(gym, "a2"),
                this.mockAsset(gym, "a3"),
                this.mockAsset(gym, "a4")
        )

        val reservations = this.reservationDAO.saveAll(listOf(
                Reservation(assets[0], user1, OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T12:00:00+00:00")),
                Reservation(assets[1], user1, OffsetDateTime.parse("2022-01-01T10:00:00+00:00"), OffsetDateTime.parse("2022-01-01T12:00:00+00:00")),
                Reservation(assets[0], user2, OffsetDateTime.parse("2019-02-01T10:00:00+00:00"), OffsetDateTime.parse("2019-02-01T12:00:00+00:00")),
                Reservation(assets[3], user2, OffsetDateTime.parse("2019-03-01T10:00:00+00:00"), OffsetDateTime.parse("2019-03-01T12:00:00+00:00"))
        )).toList()

        val id = reservations[2].id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/me/$id")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("reservation $id does not exist or is not owned by user ${user1.id}")))
                .andDo(MockMvcResultHandlers.print())
    }

    /********************************** AVAILABLE ASSETS ************************************************************/

    @Test
    fun `Should get available assets`() {
        this.reservationDAO.deleteAll()
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val assets = this.assetDAO.saveAll((1..4).map { Asset("ciclette$it", kind, gym) }).toList()
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(4)
        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)

        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/$from/to/$to"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id", Matchers.`is`(assets.first().id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id", Matchers.`is`(assets[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].id", Matchers.`is`(assets[2].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[3].id", Matchers.`is`(assets[3].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the available assets in a gym`() {
        this.reservationDAO.deleteAll()
        val gym = this.mockGym()
        val anotherGym = this.gymDAO.save(Gym("another gym", "address2", gym.city))
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))

        // create 4 assets, 2 in the first gym and 2 in the second one
        val assets = this.assetDAO.saveAll((0..3).map { Asset("ciclette$it", kind, if (it % 2 == 0) gym else anotherGym) }).toList()
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(4)
        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)

        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/$from/to/$to/gym/${gym.id}"

        val expectedResult = listOf(assets[0], assets[2])
        Assertions.assertThat(expectedResult.all { it.gym.id == gym.id }).isTrue()
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id", Matchers.`is`(assets[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id", Matchers.`is`(assets[2].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].gym.id", Matchers.`is`(gym.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].gym.id", Matchers.`is`(gym.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return empty collection if the gym is closed in the interval provided when searching for free assets`() {
        this.reservationDAO.deleteAll()
        val gym = this.mockGym()

        // timetable: from Monday to Friday from 08 to 12
        this.timetableDAO.save(Timetable(gym, MockEntities.mockOpenings))
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetDAO.saveAll((1..4).map { Asset("ciclette$it", kind, gym) }).toList()
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(4)
        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)

        // search when the gym is closed
        val from = OffsetDateTime.parse("2050-04-04T13:00:00+00:00")
        val to = from.plusMinutes(10)
        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/$from/to/$to")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(0)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 if the kind does not exist when searching for available assets`() {
        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/kind/-1/from/$from/to/$to"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 if the kind does not exist when searching for available assets and filtering by gym`() {
        val gym = this.mockGym()
        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/kind/-1/from/$from/to/$to/gym/${gym.id}"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 if the kind does not exist when searching for available assets and filtering by city`() {
        val gym = this.mockGym()
        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/kind/-1/from/$from/to/$to/city/${gym.city.id}"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 if the gym does not exist when searching for available assets and filtering by gym`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.BENCH, 20))
        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/$from/to/$to/gym/-1"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return 404 if the city does not exist when searching for available assets and filtering by city`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.BENCH, 20))
        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/$from/to/$to/city/-1"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    /************************* CHECK SPECIFIC ASSET AVAILABILITY ************************************************/

    @Test
    fun `Should return 404 if the asset does not exist when checking its availability`() {
        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/asset/-1/from/$from/to/$to"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should check the availability of an asset and return true if everything's ok`() {
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.wildcardOpenings))
        val asset = this.mockAsset(gym)
        val from = OffsetDateTime.parse("2050-04-04T10:00:00+00:00")
        val to = from.plusMinutes(10)
        val url = "${ApiUrls.RESERVATIONS}/available/asset/${asset.id}/from/$from/to/$to"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(true)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should check the availability of an asset and return false if it is not`() {
        val gym = this.mockGym()
        this.timetableDAO.save(Timetable(gym, MockEntities.wildcardOpenings))
        val asset = this.mockAsset(gym)
        var from = OffsetDateTime.parse("2010-04-04T10:00:00+00:00")
        var to = from.plusMinutes(10)
        var url = "${ApiUrls.RESERVATIONS}/available/asset/${asset.id}/from/$from/to/$to"

        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(false)))
                .andDo(MockMvcResultHandlers.print())

        from = OffsetDateTime.parse("2005-04-04T10:20:00+00:00")
        to = from.plusMinutes(10)
        url = "${ApiUrls.RESERVATIONS}/available/asset/${asset.id}/from/$from/to/$to"
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(false)))
                .andDo(MockMvcResultHandlers.print())
    }


    /************************************** UTILS *******************************************************************/

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(MockEntities.mockCities[0])))
    }

    private fun mockUser(username: String = "gabrigiunchi"): User {
        return this.userDAO.save(User(username, "aaaa", "Gabriele", "Giunchi", "mail@mail.com"))
    }

    private fun mockAsset(gym: Gym, name: String = "asset"): Asset {
        return this.assetDAO.save(Asset(name, this.assetKindDAO.save(MockEntities.assetKinds.first()), gym))
    }

    private fun mockReservations(): List<Reservation> {
        val user = this.mockUser()
        val gym = this.mockGym()
        val assets = listOf(
                this.mockAsset(gym, "a1"),
                this.mockAsset(gym, "a2"),
                this.mockAsset(gym, "a3"),
                this.mockAsset(gym, "a4")
        )

        return this.reservationDAO.saveAll(listOf(
                Reservation(assets[0], user, OffsetDateTime.parse("2018-01-01T10:00:00+00:00"), OffsetDateTime.parse("2018-01-01T12:00:00+00:00")),
                Reservation(assets[1], user, OffsetDateTime.parse("2019-01-01T10:00:00+00:00"), OffsetDateTime.parse("2019-01-01T12:00:00+00:00")),
                Reservation(assets[0], user, OffsetDateTime.parse("2019-02-01T10:00:00+00:00"), OffsetDateTime.parse("2019-02-01T12:00:00+00:00")),
                Reservation(assets[3], user, OffsetDateTime.parse("2019-03-01T10:00:00+00:00"), OffsetDateTime.parse("2019-03-01T12:00:00+00:00"))
        )).toList()
    }
}