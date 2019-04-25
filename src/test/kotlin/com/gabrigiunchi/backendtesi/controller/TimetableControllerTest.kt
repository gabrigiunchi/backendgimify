package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.Timetable
import com.gabrigiunchi.backendtesi.model.dto.input.TimetableDTO
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class TimetableControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    private var gyms = emptyList<Gym>()

    private val dateIntervals = MockEntities.mockDateIntervals
    private val schedules = MockEntities.mockSchedules
    private var timetables = listOf<Timetable>()

    @Before
    fun clearDB() {
        this.cityDAO.deleteAll()
        this.timetableDAO.deleteAll()
        this.gymDAO.deleteAll()

        val city = this.cityDAO.save(City(CityEnum.BERGAMO))
        this.gyms = this.gymDAO.saveAll(listOf(
                Gym("gym1", "via1", city),
                Gym("gym2", "via2", city),
                Gym("gym3", "via3", city),
                Gym("gym4", "via4", city)
        )).toList()

        this.timetables = listOf(
                Timetable(
                        gym = gyms[0],
                        closingDays = this.dateIntervals.take(1).toSet(),
                        openings = this.schedules.take(2).toSet(),
                        recurringExceptions = MockEntities.mockHolidays,
                        exceptionalOpenings = emptySet()),

                Timetable(gym = gyms[1]),
                Timetable(gym = gyms[2]),
                Timetable(gym = gyms[3])
        )
    }

    @Test
    fun `Should get all timetables`() {
        this.timetableDAO.saveAll(this.timetables)
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.TIMETABLES)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a timetable by its id`() {
        val timetable = this.timetableDAO.save(this.timetables[0])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/${timetable.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.openings.length()", Matchers.`is`(timetable.openings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionalOpenings.length()", Matchers.`is`(timetable.exceptionalOpenings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.closingDays.length()", Matchers.`is`(timetable.closingDays.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(timetable.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurringExceptions.length()",
                        Matchers.`is`(MockEntities.mockHolidays.size)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a timetable by its gym id`() {
        val timetable = this.timetableDAO.save(this.timetables[0])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/by_gym/${timetable.gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.openings.length()", Matchers.`is`(timetable.openings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionalOpenings.length()", Matchers.`is`(timetable.exceptionalOpenings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.closingDays.length()", Matchers.`is`(timetable.closingDays.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(timetable.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurringExceptions.length()",
                        Matchers.`is`(MockEntities.mockHolidays.size)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a timetable if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("timetable -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }


    @Test
    fun `Should not get a timetable by gym if the gym does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/by_gym/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a timetable by gym if the gym exists but the timetable does not`() {
        val gymId = this.gyms[0].id
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/by_gym/$gymId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("timetable does not exist for gym $gymId")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a timetable`() {
        val timetableDTO = TimetableDTO(this.gyms[0].id, emptySet(), emptySet(), emptySet(), MockEntities.mockHolidays)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.TIMETABLES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(timetableDTO.toJson()))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should NOT create a timetable if the gym does not exist`() {
        this.gymDAO.deleteAll()
        val gymId = this.gyms[0].id
        val timetableDTO = TimetableDTO(gymId, emptySet(), emptySet(), emptySet(), MockEntities.mockHolidays)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.TIMETABLES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(timetableDTO.toJson()))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("gym $gymId does not exist")))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(0)
    }

    @Test
    fun `Should update a timetable`() {
        val timetable = this.timetableDAO.save(this.timetables[0])
        val openings = this.schedules.toList().takeLast(2)
        val timetableDTO = TimetableDTO(
                gymId = timetable.gym.id,
                openings = openings.toSet(),
                exceptionalOpenings = emptySet(),
                closingDays = emptySet(),
                recurringExceptions = MockEntities.mockHolidays
        )

        Assertions.assertThat(timetable.id).isNotEqualTo(-1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.TIMETABLES}/${timetable.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(timetableDTO.toJson()))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.openings.length()", Matchers.`is`(timetable.openings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionalOpenings.length()", Matchers.`is`(timetable.exceptionalOpenings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.closingDays.length()", Matchers.`is`(timetable.closingDays.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(timetable.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.recurringExceptions.length()",
                        Matchers.`is`(MockEntities.mockHolidays.size)))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should NOT update a timetable if it does not exist`() {
        val timetable = this.timetableDAO.save(this.timetables[0])
        val openings = this.schedules.toList().takeLast(2)
        val timetableDTO = TimetableDTO(
                gymId = timetable.gym.id,
                openings = openings.toSet(),
                exceptionalOpenings = emptySet(),
                closingDays = emptySet(),
                recurringExceptions = MockEntities.mockHolidays
        )

        Assertions.assertThat(timetable.id).isNotEqualTo(-1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.TIMETABLES}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(timetableDTO.toJson()))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("timetable -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT update a timetable if the gym does not exist`() {
        val timetable = this.timetableDAO.save(this.timetables[0])
        val openings = this.schedules.toList().takeLast(2)
        val timetableDTO = TimetableDTO(
                gymId = -1,
                openings = openings.toSet(),
                exceptionalOpenings = emptySet(),
                closingDays = emptySet(),
                recurringExceptions = MockEntities.mockHolidays
        )

        Assertions.assertThat(timetable.id).isNotEqualTo(-1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.TIMETABLES}/${timetable.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(timetableDTO.toJson()))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a timetable by its id`() {
        val savedId = this.timetableDAO.save(this.timetables[0]).id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.TIMETABLES}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.findById(savedId).isEmpty).isTrue()
    }

    @Test
    fun `Should delete a timetable by its gym id`() {
        val saved = this.timetableDAO.save(this.timetables[0])
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.TIMETABLES}/by_gym/${saved.gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.findById(saved.id).isEmpty).isTrue()
    }

    @Test
    fun `Should NOT delete a timetable by its gym id if the gym does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.TIMETABLES}/by_gym/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT delete a timetable by its gym id if the timetable does not exist`() {
        val gymId = this.gyms[0].id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.TIMETABLES}/by_gym/$gymId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("no timetable for gym $gymId")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete a timetable if its id does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.TIMETABLES}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("timetable -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

}