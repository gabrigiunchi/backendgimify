package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.dao.UserRoleDAO
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


class UserControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userRoleDAO: UserRoleDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Before
    fun clearDB() {
        this.userDAO.deleteAll()
    }

    @Test
    fun `Should get all users`() {
        this.userDAO.saveAll(listOf(
                User("gabrigiunchi", "dsndja", "Gabriele", "Giunchi", "mail@mail.com"),
                User("fragiunchi", "dsndja", "Francesco", "Giunchi", "mail@mail.com"),
                User("fabiogiunchi", "dsndja", "Fabio", "Giunchi", "mail@mail.com")))

        this.mockMvc.perform(get("${ApiUrls.USERS}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(3)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a user given its id`() {
        val user = this.userDAO.save(User("giggi", "ddnsakjn", "Gianni", "Riccio", "mail@mail.com"))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a the details of a user`() {
        val user = this.userDAO.save(User("giggi", "ddnsakjn", "Gianni", "Riccio", "mail@mail.com"))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a user`() {
        val roles = this.userRoleDAO.findByName("ADMINISTRATOR")
        val user = User("giggi", "ddnsakjn", "", "", "mail@mail.com", roles.toMutableList())
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
        val user = User("giggi", "ddnsakjn", "", "", "mail@mail.com", roles.toMutableList())
        userDAO.save(user)
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a user if its username already exists`() {
        val user = this.userFactory.createRegularUser("gab", "aaaa", "Gab", "Giunchi")
        val saved = this.userDAO.save(user)
        Assertions.assertThat(user.id).isNotEqualTo(saved.id)
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a user`() {
        val roles = this.userRoleDAO.findByName("ADMINISTRATOR")
        var user = User("giggi", "ddnsakjn", "", "", "mail@mail.com", roles.toMutableList())
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the logged user`() {
        this.userDAO.deleteAll()
        val user = this.userDAO.save(
                this.userFactory.createRegularUser(
                        "gabrigiunchi", "aaaa", "Gabriele", "Giunchi", "gabri@gmail.com"))

        mockMvc.perform(get("${ApiUrls.USERS}/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(user.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname", Matchers.`is`(user.surname)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.`is`(user.email)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["USER"])
    fun `Should deactivate a user`() {
        this.userDAO.deleteAll()
        val user = this.userDAO.save(this.userFactory.createRegularUser("gabrigiunchi", "aaaa", "", "", ""))
        Assertions.assertThat(user.isActive).isTrue()

        mockMvc.perform(get("${ApiUrls.USERS}/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())

        mockMvc.perform(patch("${ApiUrls.USERS}/${user.id}/active/false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.userDAO.findByUsername(user.username).get().isActive).isFalse()
    }

    @Test
    fun `Should not deactivate a user if it does not exist`() {
        mockMvc.perform(patch("${ApiUrls.USERS}/-1/active/false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }
}