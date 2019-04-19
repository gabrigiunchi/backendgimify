package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.TimeInterval
import com.gabrigiunchi.backendtesi.model.Region
import com.gabrigiunchi.backendtesi.model.Schedule
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.DayOfWeek
import java.time.OffsetTime

class GymControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var regionDAO: RegionDAO

    private var region = Region(RegionEnum.ABRUZZO)

    @Before
    fun clearDB() {
        this.regionDAO.deleteAll()
        this.region = this.regionDAO.save(this.region)
    }

    private val intervals = listOf(
            TimeInterval(OffsetTime.parse("10:00:00+00:00"), OffsetTime.parse("12:00:00+00:00")),
            TimeInterval(OffsetTime.parse("12:00:00+00:00"), OffsetTime.parse("14:00:00+00:00")),
            TimeInterval(OffsetTime.parse("14:00:00+00:00"), OffsetTime.parse("16:00:00+00:00")),
            TimeInterval(OffsetTime.parse("16:00:00+00:00"), OffsetTime.parse("18:00:00+00:00")))

    private val schedules = listOf(
            Schedule(DayOfWeek.MONDAY, this.intervals.take(2).toSet()),
            Schedule(DayOfWeek.TUESDAY, setOf(this.intervals[2], this.intervals[3])),
            Schedule(DayOfWeek.FRIDAY),
            Schedule(DayOfWeek.WEDNESDAY))

    @Test
    fun `Should get all gyms`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.greaterThanOrEqualTo(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a gym by its id`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address", this.region))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.region.name", Matchers.`is`(gym.region.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the gyms in specific region`() {
        this.gymDAO.deleteAll()
        val regions = this.regionDAO.saveAll(RegionEnum.values().map { Region(it) }).toList()

        val gyms = this.gymDAO.saveAll(listOf(
                Gym("gym1", "Via1", regions[0]),
                Gym("gym2", "Via2", regions[0]),
                Gym("gym3", "Via3", regions[1]),
                Gym("gym4", "Via4", regions[2]))).toList()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/by_region/${regions[0].id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(gyms[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(gyms[1].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the gyms in specific region if the region does not exist`() {
        this.gymDAO.deleteAll()
        val regions = this.regionDAO.saveAll(RegionEnum.values().map { Region(it) }).toList()

        this.gymDAO.saveAll(listOf(
                Gym("gym1", "Via1", regions[0]),
                Gym("gym2", "Via2", regions[0]),
                Gym("gym3", "Via3", regions[1]),
                Gym("gym4", "Via4", regions[2]))).toList()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/by_region/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a gym if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a gym`() {
        val gym = Gym("gym dnjsnjdaj", "Via Pacchioni 43", region, this.schedules.take(2))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gym.toJson()))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.region.name", Matchers.`is`(gym.region.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.openings.length()", Matchers.`is`(2)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update a gym`() {
        val gym = this.gymDAO.save(Gym("gymaaa1", "Via Pacchioni 43", this.region, this.schedules.take(1)))
        val savedGym = this.gymDAO.save(gym)
        gym.name = "newName"
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/${savedGym.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gym.toJson()))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.region.name", Matchers.`is`(gym.region.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update a gym if it does not exist`() {
        val gym = this.gymDAO.save(Gym("gymnnnn1", "address", this.region))
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a gym if its id already exist`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address", this.region))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a gym`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address", this.region))
        val savedId = this.gymDAO.save(gym).id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.GYMS}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete a gym if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.GYMS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}