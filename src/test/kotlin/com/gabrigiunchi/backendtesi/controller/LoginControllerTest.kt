package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.config.security.JwtTokenProvider
import com.gabrigiunchi.backendtesi.config.security.SHA256PasswordEncoder
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.UserRole
import com.gabrigiunchi.backendtesi.model.dto.ValidateUserDTO
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class LoginControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Autowired
    private lateinit var tokenProvider: JwtTokenProvider

    @Before
    fun clearDB() {
        this.userDAO.deleteAll()
    }

    @Test
    fun `Should log in a user with valid username and password`() {
        val password = "aaaa"
        val user = this.userFactory.createRegularUser("gabrigiunchi", SHA256PasswordEncoder().encode(password),
                "Gabriele", "Giunchi")
        val credentials = ValidateUserDTO(user.username, password)
        this.userDAO.save(user)

        Assertions.assertThat(this.userDAO.findByUsername(user.username).isPresent).isTrue()
        Assertions.assertThat(this.userDAO.findByUsername(user.username).get().password).isEqualTo(user.password)

        this.mockMvc.perform(post(ApiUrls.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(credentials)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`(user.username)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token", Matchers.notNullValue()))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT log in a user with invalid username`() {
        val credentials = ValidateUserDTO("mario", "djksnkan")

        Assertions.assertThat(this.userDAO.findByUsername(credentials.username).isEmpty).isTrue()

        this.mockMvc.perform(post(ApiUrls.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(credentials)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should NOT log in a user with invalid password`() {
        val password = "aaaa"
        val user = this.userFactory.createRegularUser("djkasnjdna", SHA256PasswordEncoder().encode(password),
                "dasdsa", "dsdadsa")

        this.userDAO.save(user)
        val credentials = ValidateUserDTO(user.username, "mmkldamaldmak")

        this.mockMvc.perform(post(ApiUrls.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(credentials)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should say if a token is valid`() {
        val user = this.userFactory.createRegularUser(
                "gabrigiunchi",
                SHA256PasswordEncoder().encode("aaaa"),
                "Gabriele",
                "Giunchi"
        )
        this.userDAO.save(user)
        val token = this.tokenProvider.createToken(user.username, user.roles.map(UserRole::name))

        this.mockMvc.perform(post("${ApiUrls.LOGIN}/token")
                .contentType(MediaType.TEXT_PLAIN)
                .content(token))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(true)))
    }

    @Test
    fun `Should say if a token is NOT valid`() {
        val token = "dajdjadjasdnaj"
        this.mockMvc.perform(post("${ApiUrls.LOGIN}/token")
                .contentType(MediaType.TEXT_PLAIN)
                .content(token))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(false)))
    }

}