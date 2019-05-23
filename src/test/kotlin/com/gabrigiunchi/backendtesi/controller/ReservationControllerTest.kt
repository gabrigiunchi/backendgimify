//package com.gabrigiunchi.backendtesi.controller
//
//import com.gabrigiunchi.backendtesi.AbstractControllerTest
//import com.gabrigiunchi.backendtesi.MockEntities
//import com.gabrigiunchi.backendtesi.constants.ApiUrls
//import com.gabrigiunchi.backendtesi.dao.*
//import com.gabrigiunchi.backendtesi.model.*
//import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
//import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
//import com.gabrigiunchi.backendtesi.model.type.CityEnum
//import com.gabrigiunchi.backendtesi.util.DateDecorator
//import org.assertj.core.api.Assertions
//import org.hamcrest.Matchers
//import org.junit.Before
//import org.junit.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers
//import java.time.LocalDateTime
//import java.util.*
//
//class ReservationControllerTest : AbstractControllerTest() {
//
//    @Autowired
//    private lateinit var reservationDAO: ReservationDAO
//
//    @Autowired
//    private lateinit var assetDAO: AssetDAO
//
//    @Autowired
//    private lateinit var userDAO: UserDAO
//
//    @Autowired
//    private lateinit var assetKindDAO: AssetKindDAO
//
//    @Autowired
//    private lateinit var gymDAO: GymDAO
//
//    @Autowired
//    private lateinit var cityDAO: CityDAO
//
//    @Autowired
//    private lateinit var timetableDAO: TimetableDAO
//
//    @Autowired
//    private lateinit var reservationLogDAO: ReservationLogDAO
//
//    @Value("\${application.reservationThresholdInDays}")
//    private var reservationThresholdInDays: Int = 0
//
//
//    @Before
//    fun clearDB() {
//        this.reservationDAO.deleteAll()
//        this.gymDAO.deleteAll()
//        this.assetDAO.deleteAll()
//        this.reservationLogDAO.deleteAll()
//        this.timetableDAO.deleteAll()
//    }
//
//    @Test
//    fun `Should get all reservations`() {
//        this.mockReservations()
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/page/0/size/20")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.greaterThanOrEqualTo(4)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should get a reservation by its id`() {
//        val reservation = this.reservationDAO.save(
//                Reservation(this.mockAsset(this.mockGym()), this.mockUser(),
//                        DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date))
//
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/${reservation.id}")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(reservation.id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should get all reservations of an asset`() {
//        val reservations = this.mockReservations()
//        val asset = reservations[0].asset
//
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/${asset.id}")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(reservations[0].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(reservations[2].id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should get all future reservations of an asset`() {
//        val r1 = this.mockReservations().first()
//        val asset = r1.asset
//        val user = r1.user
//        val reservations = this.reservationDAO.saveAll(listOf(
//                Reservation(user = user,
//                        asset = asset,
//                        start = LocalDateTime.now().minusMinutes(20),
//                        end = LocalDateTime.now().minusMinutes(2)),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = LocalDateTime.now().plusMinutes(1),
//                        end = LocalDateTime.now().plusMinutes(20)),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = LocalDateTime.now().plusMinutes(100),
//                        end = LocalDateTime.now().plusMinutes(102))
//        )).toList()
//
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/${asset.id}/future")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(reservations[1].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(reservations[2].id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should get all the reservations of today of an asset`() {
//        val asset = this.mockAsset(this.mockGym())
//        val user = this.mockUser("pippo")
//        val reservations = this.reservationDAO.saveAll(listOf(
//                Reservation(user = user,
//                        asset = asset,
//                        start = DateDecorator.now().minusMinutes(20).date,
//                        end = DateDecorator.now().minusMinutes(2).date),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = DateDecorator.startOfToday().date,
//                        end = DateDecorator.startOfToday().plusMinutes(2).date),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = DateDecorator.startOfToday().plusMinutes(1438).date,
//                        end = DateDecorator.startOfToday().plusMinutes(1440).date),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = DateDecorator.of("2018-01-01T10:00:00+0000").date,
//                        end = DateDecorator.of("2018-01-01T10:30:00+0000").date),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = DateDecorator.of("2019-01-01T10:00:00+0000").date,
//                        end = DateDecorator.of("2019-01-01T10:30:00+0000").date),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = DateDecorator.startOfToday().minusDays(1).plusMinutes(1).date,
//                        end = DateDecorator.startOfToday().minusDays(1).plusMinutes(2).date),
//
//                Reservation(user = user,
//                        asset = asset,
//                        start = DateDecorator.startOfToday().minusMinutes(2).date,
//                        end = DateDecorator.startOfToday().minusMinutes(1).date)
//        )).toList()
//
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/${asset.id}/today")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(3)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(reservations[0].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(reservations[1].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[2].id", Matchers.`is`(reservations[2].id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 when requesting the reservations of an asset that does not exist`() {
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/-1")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 when requesting the future reservations of an asset that does not exist`() {
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/-1/today")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 when requesting the reservations of today of an asset that does not exist`() {
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/of_asset/-1/future")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should not get a reservation if it does not exist`() {
//        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/-1")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("reservation -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should create a reservation`() {
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val asset = this.mockAsset(gym)
//
//        val reservation = ReservationDTOInput(
//                userID = this.mockUser().id,
//                assetID = asset.id,
//                start = DateDecorator.of("2050-04-04T10:00:00+0000").date,
//                end = DateDecorator.of("2050-04-04T10:15:00+0000").date)
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservation)))
//                .andExpect(MockMvcResultMatchers.status().isCreated)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(reservation.userID)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.asset.id", Matchers.`is`(reservation.assetID)))
//                .andDo(MockMvcResultHandlers.print())
//
//        val logs = this.reservationLogDAO.findByUser(this.userDAO.findById(reservation.userID).get())
//        Assertions.assertThat(logs.size).isEqualTo(1)
//        Assertions.assertThat(DateDecorator.of(logs.first().date).format("yyyy-MM-dd"))
//                .isEqualTo(DateDecorator.now().format("yyyy-MM-dd"))
//    }
//
//    @Test
//    fun `Should not create a reservation if interval is in the past`() {
//        val gym = this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(City(CityEnum.MIAMI))))
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val asset = this.mockAsset(gym)
//
//        val reservation = ReservationDTOInput(
//                userID = this.mockUser().id,
//                assetID = asset.id,
//                start = DateDecorator.of("2019-04-22T10:00:00+0000").date,
//                end = DateDecorator.of("2019-04-22T11:00:00+0000").date)
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservation)))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest)
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should not create a reservation if the gym is closed`() {
//        val gym = this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(City(CityEnum.MIAMI))))
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val asset = this.mockAsset(gym)
//
//        val reservation = ReservationDTOInput(
//                userID = this.mockUser().id,
//                assetID = asset.id,
//                start = DateDecorator.of("2050-04-05T10:00:00+0000").date,
//                end = DateDecorator.of("2050-04-05T10:15:00+0000").date)
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservation)))
//                .andExpect(MockMvcResultMatchers.status().isForbidden)
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should not create a reservation if there is another one at the same time`() {
//        this.userDAO.deleteAll()
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val asset = this.mockAsset(gym)
//        val user = this.mockUser()
//
//        this.reservationDAO.save(Reservation(asset, user, DateDecorator.of("2050-04-04T11:00:00+0000").date,
//                DateDecorator.of("2050-04-04T11:10:00+0000").date))
//
//        val reservation = ReservationDTOInput(
//                userID = this.mockUser().id,
//                assetID = asset.id,
//                start = DateDecorator.of("2050-04-04T10:55:00+0000").date,
//                end = DateDecorator.of("2050-04-04T11:05:00+0000").date)
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservation)))
//                .andExpect(MockMvcResultMatchers.status().isConflict)
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should not be able to make 3 reservations per day`() {
//        this.userDAO.deleteAll()
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val asset = this.mockAsset(gym)
//        val user = this.mockUser()
//
//        val reservations = listOf(
//                ReservationDTOInput(
//                        userID = user.id,
//                        assetID = asset.id,
//                        start = DateDecorator.of("2050-04-04T10:55:00+0000").date,
//                        end = DateDecorator.of("2050-04-04T11:05:00+0000").date),
//
//                ReservationDTOInput(
//                        userID = user.id,
//                        assetID = asset.id,
//                        start = DateDecorator.of("2050-04-11T10:55:00+0000").date,
//                        end = DateDecorator.of("2050-04-11T11:05:00+0000").date),
//
//                ReservationDTOInput(
//                        userID = user.id,
//                        assetID = asset.id,
//                        start = DateDecorator.of("2050-04-18T10:55:00+0000").date,
//                        end = DateDecorator.of("2050-04-18T11:05:00+0000").date))
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservations[0])))
//                .andExpect(MockMvcResultMatchers.status().isCreated)
//                .andDo(MockMvcResultHandlers.print())
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservations[1])))
//                .andExpect(MockMvcResultMatchers.status().isCreated)
//                .andDo(MockMvcResultHandlers.print())
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservations[2])))
//                .andExpect(MockMvcResultMatchers.status().isForbidden)
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return bad request if the reservation is beyond the threshold`() {
//        this.userDAO.deleteAll()
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.wildcardSchedules))
//        val asset = this.mockAsset(gym)
//        val user = this.mockUser()
//        val start = DateDecorator.now().plusDays(this.reservationThresholdInDays).plusMinutes(1)
//        val end = start.plusMinutes(5)
//        val reservation = ReservationDTOInput(
//                userID = user.id,
//                assetID = asset.id,
//                start = start.date,
//                end = end.date)
//
//        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.RESERVATIONS)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservation)))
//                .andExpect(MockMvcResultMatchers.status().isBadRequest)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("The reservation is beyond the threshold")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should delete a reservation`() {
//        val savedId = this.reservationDAO.save(
//                Reservation(this.mockAsset(this.mockGym()), this.mockUser(),
//                        DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date)
//        ).id
//
//        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/$savedId")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNoContent)
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should not delete a reservation if it does not exist`() {
//        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/-1")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("reservation -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    /************************************** MY RESERVATIONS *****************************************************/
//
//    @Test
//    fun `Should get all my reservations`() {
//        this.userDAO.deleteAll()
//        val user1 = this.mockUser("gabrigiunchi")
//        val user2 = this.mockUser("fragiunchi")
//        val gym = this.mockGym()
//        val assets = listOf(
//                this.mockAsset(gym, "a1"),
//                this.mockAsset(gym, "a2"),
//                this.mockAsset(gym, "a3"),
//                this.mockAsset(gym, "a4")
//        )
//
//        val reservations = this.reservationDAO.saveAll(listOf(
//                Reservation(assets[0], user1, DateDecorator.of("2017-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
//                Reservation(assets[1], user1, DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2019-01-01T12:00:00+0000").date),
//                Reservation(assets[0], user1, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
//                Reservation(assets[3], user1, DateDecorator.of("2020-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date),
//                Reservation(assets[0], user2, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
//                Reservation(assets[3], user2, DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
//        )).toList()
//
//        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/page/0/size/2")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(2)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`(reservations[3].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.`is`(reservations[2].id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return the number reservations I made`() {
//        this.userDAO.deleteAll()
//        val user = this.mockUser("gabrigiunchi")
//        this.reservationLogDAO.saveAll((1..4).map { ReservationLog(-1, user, -1, Date()) })
//
//        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/count")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(4)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should get all my future reservations`() {
//        this.userDAO.deleteAll()
//        val user1 = this.mockUser("gabrigiunchi")
//        val user2 = this.mockUser("fragiunchi")
//        val gym = this.mockGym()
//        val assets = listOf(
//                this.mockAsset(gym, "a1"),
//                this.mockAsset(gym, "a2"),
//                this.mockAsset(gym, "a3"),
//                this.mockAsset(gym, "a4")
//        )
//
//        val reservations = this.reservationDAO.saveAll(listOf(
//                Reservation(assets[0], user1, DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
//                Reservation(assets[1], user1, DateDecorator.of("2022-01-01T10:00:00+0000").date, DateDecorator.of("2022-01-01T12:00:00+0000").date),
//                Reservation(assets[0], user2, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
//                Reservation(assets[3], user2, DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
//        )).toList()
//
//        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/future")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(1)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(reservations[1].id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should get one of my reservations by id`() {
//        this.userDAO.deleteAll()
//        val user1 = this.mockUser("gabrigiunchi")
//        val user2 = this.mockUser("fragiunchi")
//        val gym = this.mockGym()
//        val assets = listOf(
//                this.mockAsset(gym, "a1"),
//                this.mockAsset(gym, "a2"),
//                this.mockAsset(gym, "a3"),
//                this.mockAsset(gym, "a4")
//        )
//
//        val reservations = this.reservationDAO.saveAll(listOf(
//                Reservation(assets[0], user1, DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
//                Reservation(assets[1], user1, DateDecorator.of("2022-01-01T10:00:00+0000").date, DateDecorator.of("2022-01-01T12:00:00+0000").date),
//                Reservation(assets[0], user2, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
//                Reservation(assets[3], user2, DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
//        )).toList()
//
//        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/${reservations[0].id}")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(reservations[0].id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 when requesting one of my reservations by id if it does not exist`() {
//        this.userDAO.deleteAll()
//        val user = this.mockUser("gabrigiunchi")
//        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/-1")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath(
//                        "$[0].message", Matchers.`is`("reservation -1 does not exist or is not owned by user ${user.id}")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 when requesting one of my reservations by id if I do not own it`() {
//        this.userDAO.deleteAll()
//        val user1 = this.mockUser("gabrigiunchi")
//        val user2 = this.mockUser("fragiunchi")
//        val gym = this.mockGym()
//        val assets = listOf(
//                this.mockAsset(gym, "a1"),
//                this.mockAsset(gym, "a2"),
//                this.mockAsset(gym, "a3"),
//                this.mockAsset(gym, "a4")
//        )
//
//        val reservations = this.reservationDAO.saveAll(listOf(
//                Reservation(assets[0], user1, DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
//                Reservation(assets[1], user1, DateDecorator.of("2022-01-01T10:00:00+0000").date, DateDecorator.of("2022-01-01T12:00:00+0000").date),
//                Reservation(assets[0], user2, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
//                Reservation(assets[3], user2, DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
//        )).toList()
//
//        val id = reservations[2].id
//        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.RESERVATIONS}/me/$id")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath(
//                        "$[0].message", Matchers.`is`("reservation $id does not exist or is not owned by user ${user1.id}")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should create a reservation with the 'me' REST API`() {
//        this.userDAO.deleteAll()
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val asset = this.mockAsset(gym)
//
//        val reservation = ReservationDTOInput(
//                userID = this.mockUser().id,
//                assetID = asset.id,
//                start = DateDecorator.of("2050-04-04T10:00:00+0000").date,
//                end = DateDecorator.of("2050-04-04T10:10:00+0000").date)
//
//        mockMvc.perform(MockMvcRequestBuilders.post("${ApiUrls.RESERVATIONS}/me")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json(reservation)))
//                .andExpect(MockMvcResultMatchers.status().isCreated)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(reservation.userID)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.asset.id", Matchers.`is`(reservation.assetID)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//
//    @Test
//    fun `Should delete one of my reservations`() {
//        this.userDAO.deleteAll()
//        val savedId = this.reservationDAO.save(
//                Reservation(this.mockAsset(this.mockGym()), this.mockUser(),
//                        DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T10:15:00+0000").date)
//        ).id
//
//        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/me/$savedId")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNoContent)
//                .andDo(MockMvcResultHandlers.print())
//
//        Assertions.assertThat(this.reservationDAO.findById(savedId).isEmpty).isTrue()
//    }
//
//    @Test
//    fun `Should not delete one of my reservations if it does not exist`() {
//        this.userDAO.deleteAll()
//        val user = this.mockUser("gabrigiunchi")
//        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/me/-1")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`(
//                        "reservation -1 does not exist or is not owned by user ${user.id}")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should not delete one of my reservations if I do not own it`() {
//        this.userDAO.deleteAll()
//        val user1 = this.mockUser("gabrigiunchi")
//        val user2 = this.mockUser("fragiunchi")
//        val gym = this.mockGym()
//        val assets = listOf(
//                this.mockAsset(gym, "a1"),
//                this.mockAsset(gym, "a2"),
//                this.mockAsset(gym, "a3"),
//                this.mockAsset(gym, "a4")
//        )
//
//        val reservations = this.reservationDAO.saveAll(listOf(
//                Reservation(assets[0], user1, DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
//                Reservation(assets[1], user1, DateDecorator.of("2022-01-01T10:00:00+0000").date, DateDecorator.of("2022-01-01T12:00:00+0000").date),
//                Reservation(assets[0], user2, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
//                Reservation(assets[3], user2, DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
//        )).toList()
//
//        val id = reservations[2].id
//        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.RESERVATIONS}/me/$id")
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath(
//                        "$[0].message", Matchers.`is`("reservation $id does not exist or is not owned by user ${user1.id}")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    /********************************** AVAILABLE ASSETS ************************************************************/
//
//    @Test
//    fun `Should get available assets`() {
//        this.reservationDAO.deleteAll()
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
//        val assets = this.assetDAO.saveAll((1..4).map { Asset("ciclette$it", kind, gym) }).toList()
//        Assertions.assertThat(this.assetDAO.count()).isEqualTo(4)
//        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
//
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/${from.format()}/to/${to.format()}"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(4)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id", Matchers.`is`(assets.first().id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id", Matchers.`is`(assets[1].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].id", Matchers.`is`(assets[2].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[3].id", Matchers.`is`(assets[3].id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should get available assets and filter by gym`() {
//        this.reservationDAO.deleteAll()
//        val gym = this.mockGym()
//        val anotherGym = this.gymDAO.save(Gym("another gym", "address2", gym.city))
//        this.timetableDAO.save(Timetable(gym, MockEntities.mockSchedules))
//        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
//        val assets = this.assetDAO.saveAll((0..3).map { Asset("ciclette$it", kind, if (it % 2 == 0) gym else anotherGym) }).toList()
//        Assertions.assertThat(this.assetDAO.count()).isEqualTo(4)
//        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
//
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/${from.format()}/to/${to.format()}/gym/${gym.id}"
//
//        val expectedResult = listOf(assets[0], assets[2])
//        Assertions.assertThat(expectedResult.all { it.gym.id == gym.id }).isTrue()
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id", Matchers.`is`(assets[0].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id", Matchers.`is`(assets[2].id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].gym.id", Matchers.`is`(gym.id)))
//                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].gym.id", Matchers.`is`(gym.id)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 if the kind does not exist when searching for available assets`() {
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/kind/-1/from/${from.format()}/to/${to.format()}"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 if the kind does not exist when searching for available assets and filtering by gym`() {
//        val gym = this.mockGym()
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/kind/-1/from/${from.format()}/to/${to.format()}/gym/${gym.id}"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 if the kind does not exist when searching for available assets and filtering by city`() {
//        val gym = this.mockGym()
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/kind/-1/from/${from.format()}/to/${to.format()}/city/${gym.city.id}"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 if the gym does not exist when searching for available assets and filtering by gym`() {
//        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.BENCH, 20))
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/${from.format()}/to/${to.format()}/gym/-1"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should return 404 if the city does not exist when searching for available assets and filtering by city`() {
//        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.BENCH, 20))
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/kind/${kind.id}/from/${from.format()}/to/${to.format()}/city/-1"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    /************************* CHECK SPECIFIC ASSET AVAILABILITY ************************************************/
//
//    @Test
//    fun `Should return 404 if the asset does not exist when checking it availability`() {
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/asset/-1/from/${from.format()}/to/${to.format()}"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isNotFound)
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should check the availability of an asset and return true if everything's ok`() {
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.wildcardSchedules))
//        val asset = this.mockAsset(gym)
//        val from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        val to = from.plusMinutes(10)
//        val url = "${ApiUrls.RESERVATIONS}/available/asset/${asset.id}/from/${from.format()}/to/${to.format()}"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(true)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//    @Test
//    fun `Should check the availability of an asset and return false if it is not`() {
//        val gym = this.mockGym()
//        this.timetableDAO.save(Timetable(gym, MockEntities.wildcardSchedules))
//        val asset = this.mockAsset(gym)
//        var from = DateDecorator.of("2050-04-04T10:00:00+0000")
//        var to = from.minusMinutes(10)
//        var url = "${ApiUrls.RESERVATIONS}/available/asset/${asset.id}/from/${from.format()}/to/${to.format()}"
//
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(false)))
//                .andDo(MockMvcResultHandlers.print())
//
//        from = DateDecorator.of("2005-04-04T10:20:00+0000")
//        to = from.plusMinutes(10)
//        url = "${ApiUrls.RESERVATIONS}/available/asset/${asset.id}/from/${from.format()}/to/${to.format()}"
//        mockMvc.perform(MockMvcRequestBuilders.get(url)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(MockMvcResultMatchers.status().isOk)
//                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(false)))
//                .andDo(MockMvcResultHandlers.print())
//    }
//
//
//    /************************************** UTILS *******************************************************************/
//
//    private fun mockGym(): Gym {
//        return this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(MockEntities.mockCities[0])))
//    }
//
//    private fun mockUser(username: String = "gabrigiunchi"): User {
//        return this.userDAO.save(User(username, "aaaa", "Gabriele", "Giunchi", "mail@mail.com"))
//    }
//
//    private fun mockAsset(gym: Gym, name: String = "asset"): Asset {
//        return this.assetDAO.save(Asset(name, this.assetKindDAO.save(MockEntities.assetKinds.first()), gym))
//    }
//
//    private fun mockReservations(): List<Reservation> {
//        val user = this.mockUser()
//        val gym = this.mockGym()
//        val assets = listOf(
//                this.mockAsset(gym, "a1"),
//                this.mockAsset(gym, "a2"),
//                this.mockAsset(gym, "a3"),
//                this.mockAsset(gym, "a4")
//        )
//
//        return this.reservationDAO.saveAll(listOf(
//                Reservation(assets[0], user, DateDecorator.of("2018-01-01T10:00:00+0000").date, DateDecorator.of("2018-01-01T12:00:00+0000").date),
//                Reservation(assets[1], user, DateDecorator.of("2019-01-01T10:00:00+0000").date, DateDecorator.of("2019-01-01T12:00:00+0000").date),
//                Reservation(assets[0], user, DateDecorator.of("2019-02-01T10:00:00+0000").date, DateDecorator.of("2019-02-01T12:00:00+0000").date),
//                Reservation(assets[3], user, DateDecorator.of("2019-03-01T10:00:00+0000").date, DateDecorator.of("2019-03-01T12:00:00+0000").date)
//        )).toList()
//    }
//}