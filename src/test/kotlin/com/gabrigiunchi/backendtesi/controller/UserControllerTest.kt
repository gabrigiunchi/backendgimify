package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.dao.UserRoleDAO
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


class UserControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userRoleDAO: UserRoleDAO


    @Test
    fun `Should get all users`() {
        this.userDAO.deleteAll()
        this.userDAO.saveAll(listOf(
                User("gabrigiunchi", "dsndja", "Gabriele", "Giunchi"),
                User("fragiunchi", "dsndja", "Francesco", "Giunchi"),
                User("fabiogiunchi", "dsndja", "Fabio", "Giunchi")))

        this.mockMvc.perform(get(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(3)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a user given its id`() {
        val user = this.userDAO.save(User("giggi", "ddnsakjn", "Gianni", "Riccio"))
        this.mockMvc.perform(get("${ApiUrls.USERS}/${user.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname", Matchers.`is`(user.surname)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a user if it does not exist`() {
        this.mockMvc.perform(get("${ApiUrls.USERS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a the details of a user`() {
        val user = this.userDAO.save(User("giggi", "ddnsakjn", "Gianni", "Riccio"))
        this.mockMvc.perform(get("${ApiUrls.USERS}/${user.id}/details")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname", Matchers.`is`(user.surname)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active", Matchers.`is`(user.isActive)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the details of a user if it does not exist`() {
        this.mockMvc.perform(get("${ApiUrls.USERS}/-1/details")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a user`() {
        val roles = this.userRoleDAO.findByName("ADMINISTRATOR")
        val user = User("giggi", "ddnsakjn", "", "", roles.toMutableList())
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a user if it already exists`() {
        val roles = this.userRoleDAO.findByName("ADMINISTRATOR")
        val user = User("giggi", "ddnsakjn", "", "", roles.toMutableList())
        userDAO.save(user)
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a user if its username already exists`() {
        val user = this.userDAO.findAll().first()
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a user`() {
        val roles = this.userRoleDAO.findByName("ADMINISTRATOR")
        var user = User("giggi", "ddnsakjn", "", "", roles.toMutableList())
        user = this.userDAO.save(user)
        mockMvc.perform(delete("${ApiUrls.USERS}/${user.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete a user if it does not exist`() {
        mockMvc.perform(delete("${ApiUrls.USERS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update a user`() {
        val roles = this.userRoleDAO.findByName("ADMINISTRATOR")
        var user = User("giggi", "ddnsakjn", "", "", roles.toMutableList())
        user = this.userDAO.save(user)
        Assertions.assertThat(user.isActive).isTrue()
        user.isActive = false
        mockMvc.perform(put("${ApiUrls.USERS}/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.active", Matchers.`is`(false)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update a user if it does not exist`() {
        val roles = this.userRoleDAO.findByName("ADMINISTRATOR")
        var user = User("giggi", "ddnsakjn", "", "", roles.toMutableList())
        mockMvc.perform(put("${ApiUrls.USERS}/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}