package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class GymControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

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
        val gym = this.gymDAO.save(Gym("gym dsjad", "address"))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
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
        val gym = Gym("gym dnjsnjdaj", "address")
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update a gym`() {
        val gym = this.gymDAO.save(Gym("gymaaa1", "address"))
        val savedGym = this.gymDAO.save(gym)
        gym.name = "newName"
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/${savedGym.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update a gym if it does not exist`() {
        val gym = this.gymDAO.save(Gym("gymnnnn1", "address"))
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a gym if its id already exist`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address"))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a gym`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address"))
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