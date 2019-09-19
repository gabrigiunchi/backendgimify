package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BackendtesiApplication
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.transaction.Transactional


@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = [BackendtesiApplication::class])
@AutoConfigureMockMvc
@Transactional
class AliveControllerTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Value("\${application.version}")
    private var version: String = ""

    @Test
    fun `Should return OK and be accessible to anyone`() {
        this.mockMvc.perform(get(ApiUrls.ALIVE))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.`is`("Everything's fine")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.version", Matchers.`is`(this.version)))
    }

    @Test
    @WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
    fun `Should get the logged user`() {
        this.mockMvc.perform(get("${ApiUrls.ALIVE}/me"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Matchers.`is`("gabrigiunchi")))
    }

    @Test
    @WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
    fun `Should say if I am an admin`() {
        this.mockMvc.perform(get("${ApiUrls.ALIVE}/me/admin"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(true)))
    }

    @Test
    @WithMockUser(username = "baseuser", password = "bbbb", authorities = ["USER"])
    fun `Should say if I am NOT an admin`() {
        this.mockMvc.perform(get("${ApiUrls.ALIVE}/me/admin"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(false)))
    }

    @Test
    @WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
    fun `Should allow administrators to access secured endpoints`() {
        this.mockMvc.perform(get("${ApiUrls.ALIVE}/secret"))
                .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @WithMockUser(username = "baseuser", password = "bbbb", authorities = ["USER"])
    fun `Should forbid regular users to access secured endpoints`() {
        this.mockMvc.perform(get("${ApiUrls.ALIVE}/secret"))
                .andExpect(MockMvcResultMatchers.status().isForbidden)
    }
}