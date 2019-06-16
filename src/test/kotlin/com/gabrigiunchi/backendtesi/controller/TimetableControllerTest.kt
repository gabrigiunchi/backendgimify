package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.model.dto.input.TimetableDTO
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.time.RepeatedLocalInterval
import com.gabrigiunchi.backendtesi.model.time.Timetable
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.DayOfWeek

class TimetableControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Before
    fun clearDB() {
        this.cityDAO.deleteAll()
        this.timetableDAO.deleteAll()
        this.gymDAO.deleteAll()
    }

    @Test
    fun `Should get all timetables`() {
        val timetable = this.createTimetable()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/page/0/size/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`(timetable.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a timetable by its id`() {
        val timetable = this.createTimetable()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/${timetable.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.openings.length()", Matchers.`is`(timetable.openings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.closingDays.length()", Matchers.`is`(timetable.closingDays.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(timetable.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a timetable by its gym id`() {
        val timetable = this.createTimetable()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/by_gym/${timetable.gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.openings.length()", Matchers.`is`(timetable.openings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.closingDays.length()", Matchers.`is`(timetable.closingDays.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(timetable.id)))
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
        val gymId = this.mockGym().id
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.TIMETABLES}/by_gym/$gymId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("timetable does not exist for gym $gymId")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a timetable`() {
        val timetableDTO = TimetableDTO(
                gymId = this.mockGym().id,
                closingDays = emptySet(),
                openings = MockEntities.mockOpenings.take(2).toSet())
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.TIMETABLES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(timetableDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should NOT create a timetable if the gym does not exist`() {
        this.gymDAO.deleteAll()
        val gymId = -1
        val timetableDTO = TimetableDTO(gymId, emptySet(), emptySet())
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.TIMETABLES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(timetableDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("gym $gymId does not exist")))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(0)
    }

    @Test
    fun `Should update a timetable`() {
        val timetable = this.createTimetable()
        val openings = MockEntities.mockOpenings.take(2)
        val timetableDTO = TimetableDTO(
                gymId = timetable.gym.id,
                openings = openings.toSet(),
                closingDays = emptySet()
        )

        Assertions.assertThat(timetable.id).isNotEqualTo(-1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.TIMETABLES}/${timetable.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(timetableDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.openings.length()", Matchers.`is`(timetableDTO.openings.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.closingDays.length()", Matchers.`is`(timetableDTO.closingDays.size)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(timetable.id)))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(1)
        val updatedTimetable = this.timetableDAO.findById(timetable.id).get()
        Assertions.assertThat(updatedTimetable.openings.size).isEqualTo(timetableDTO.openings.size)
    }

    @Test
    fun `Should NOT update a timetable if it does not exist`() {
        val timetable = this.createTimetable()
        val timetableDTO = TimetableDTO(
                gymId = timetable.gym.id,
                openings = MockEntities.mockOpenings.take(2).toSet(),
                closingDays = emptySet()
        )

        Assertions.assertThat(timetable.id).isNotEqualTo(-1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.TIMETABLES}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(timetableDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("timetable -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT update a timetable if the gym does not exist`() {
        val timetable = this.createTimetable()
        val openings = MockEntities.mockOpenings.take(2)
        val timetableDTO = TimetableDTO(
                gymId = -1,
                openings = openings.toSet(),
                closingDays = emptySet()
        )

        Assertions.assertThat(timetable.id).isNotEqualTo(-1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.TIMETABLES}/${timetable.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(timetableDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath(
                        "$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a timetable by its id`() {
        val savedId = this.createTimetable().id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.TIMETABLES}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.findById(savedId).isEmpty).isTrue()
    }

    @Test
    fun `Should delete a timetable by its gym id`() {
        val saved = this.createTimetable()
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
        val gymId = this.mockGym().id
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

    /**************************************** UTILS *************************************************************************/

    private fun createTimetable(): Timetable {
        val gym = this.mockGym()
        val openings = setOf(
                RepeatedLocalInterval.create(DayOfWeek.MONDAY, "08:00", "12:00"),
                RepeatedLocalInterval.create(DayOfWeek.MONDAY, "13:00", "19:00"),
                RepeatedLocalInterval.create(DayOfWeek.WEDNESDAY, "08:00", "12:00"),
                RepeatedLocalInterval.create(DayOfWeek.WEDNESDAY, "13:00", "19:00"),
                RepeatedLocalInterval.create(DayOfWeek.FRIDAY, "08:00", "12:00"),
                RepeatedLocalInterval.create(DayOfWeek.FRIDAY, "13:00", "19:00")
        )
        val closingDays = setOf(
                // Monday 29 April 2019
                RepeatedLocalInterval("2019-04-29T00:00:00", "2019-04-30T00:00:00")
        )
        return this.timetableDAO.save(Timetable(gym = gym, openings = openings, closingDays = closingDays))
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("Gym1", "Via 2", this.cityDAO.save(MockEntities.mockCities[0])))
    }

}