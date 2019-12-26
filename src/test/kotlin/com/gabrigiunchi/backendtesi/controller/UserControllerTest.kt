package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.UserRoleDAO
import com.gabrigiunchi.backendtesi.model.dto.input.ChangePasswordDTO
import com.gabrigiunchi.backendtesi.model.dto.input.UserDTOInput
import com.gabrigiunchi.backendtesi.model.entities.User
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


class UserControllerTest : BaseTest() {

    @Autowired
    private lateinit var userRoleDAO: UserRoleDAO

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
    fun `Should create a user`() {
        val user = UserDTOInput("giggi", "ddnsakjn", "", "", "mail@mail.com", mutableListOf(UserRoleEnum.ADMINISTRATOR.name))
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a user if its username already exists`() {
        val user = this.userService.createRegularUser("gab", "aaaa", "Gab", "Giunchi")
        val saved = this.userDAO.save(user)
        Assertions.assertThat(user.id).isNotEqualTo(saved.id)
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(UserDTOInput(user))))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a user if one of his roles does not exist`() {
        val user = UserDTOInput("gab", "aaaa", "Gab", "Giunchi", "mail@a.com", listOf(UserRoleEnum.USER.name, "dansda"))
        mockMvc.perform(post(ApiUrls.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(user)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user role dansda does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should modify a user`() {
        val existing = this.userDAO.save(this.userService.createRegularUser("gab", "aaaa", "Gab", "Giunchi"))
        val oldPassword = existing.password
        val modified = UserDTOInput("username", "password", "User", "Surname",
                "newmail", isActive = false, notificationsEnabled = false, roles = listOf(UserRoleEnum.ADMINISTRATOR.name))

        mockMvc.perform(put("${ApiUrls.USERS}/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(modified)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(modified.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(modified.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname", Matchers.`is`(modified.surname)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.`is`(modified.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.notificationsEnabled", Matchers.`is`(modified.notificationsEnabled)))
                .andDo(MockMvcResultHandlers.print())

        val result = this.userDAO.findById(existing.id).get()
        Assertions.assertThat(result.isActive).isFalse()
        Assertions.assertThat(result.password).isNotEqualTo(oldPassword)
    }

    @Test
    fun `Should not modify a user if it does not exist`() {
        val modified = UserDTOInput("username", "password", "User", "Surname",
                "newmail", isActive = false, notificationsEnabled = false, roles = listOf(UserRoleEnum.ADMINISTRATOR.name))
        mockMvc.perform(put("${ApiUrls.USERS}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(modified)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not modify a user if one of his roles does not exist`() {
        val existing = this.userDAO.save(this.userService.createRegularUser("gab", "aaaa", "Gab", "Giunchi"))
        val modified = UserDTOInput("username", "password", "User", "Surname",
                "newmail", isActive = false, notificationsEnabled = false, roles = listOf("aaaa"))
        mockMvc.perform(put("${ApiUrls.USERS}/${existing.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(modified)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user role aaaa does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a user`() {
        val role = this.userRoleDAO.findByName(UserRoleEnum.ADMINISTRATOR.name).get()
        var user = User("giggi", "ddnsakjn", "", "", "mail@mail.com", mutableListOf(role))
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
    fun `Should deactivate a user`() {
        this.userDAO.deleteAll()
        val user = this.mockUser
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

    @Test
    fun `Should enable the notifications of a users`() {
        this.userDAO.deleteAll()
        val user = this.mockUser
        mockMvc.perform(patch("${ApiUrls.USERS}/${user.id}/notifications/active/false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(user.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname", Matchers.`is`(user.surname)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.`is`(user.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.notificationsEnabled", Matchers.`is`(false)))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.userDAO.findById(user.id).get().notificationsEnabled).isFalse()
    }

    @Test
    fun `Should not enable the notifications of a user if it does not exist`() {
        mockMvc.perform(patch("${ApiUrls.USERS}/-1/notifications/active/false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    /************************************** ME ************************************************************************/

    @Test
    fun `Should get the logged user`() {
        this.userDAO.deleteAll()
        val user = this.mockUser
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
    fun `Should change my notifications preferences`() {
        this.userDAO.deleteAll()
        val user = this.mockUser
        mockMvc.perform(patch("${ApiUrls.USERS}/me/notifications/active/false")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(user.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(user.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname", Matchers.`is`(user.surname)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.`is`(user.email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.notificationsEnabled", Matchers.`is`(false)))
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.userDAO.findById(user.id).get().notificationsEnabled).isFalse()
    }

    @Test
    fun `Should change my password`() {
        this.userDAO.deleteAll()
        val user = this.mockUser
        val oldPassword = user.password
        val dto = ChangePasswordDTO("aaaa", "bbbb")
        mockMvc.perform(post("${ApiUrls.USERS}/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())

        val result = this.userDAO.findById(user.id).get()
        Assertions.assertThat(oldPassword).isNotEqualTo(result.password)
    }

    @Test
    fun `Should not change my password if the old password is incorrect`() {
        this.userDAO.deleteAll()
        val user = this.mockUser
        val oldPassword = user.password
        val dto = ChangePasswordDTO("acvd", "bbbb")
        mockMvc.perform(post("${ApiUrls.USERS}/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("Old password is incorrect")))
                .andDo(MockMvcResultHandlers.print())

        val result = this.userDAO.findById(user.id).get()
        Assertions.assertThat(oldPassword).isEqualTo(result.password)
    }


    private val mockUser: User
        get() = this.userDAO.save(this.userService.createRegularUser(
                "gabrigiunchi", "aaaa", "Gabriele", "Giunchi", "gabri@gmail.com"))
}