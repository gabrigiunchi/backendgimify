package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BackendtesiApplication
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import javax.transaction.Transactional

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = [BackendtesiApplication::class])
@AutoConfigureMockMvc
@Transactional
class ResourceControllerAdviceTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Test
    fun `Should not be able to access resources without authorization`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden)
                .andDo(MockMvcResultHandlers.print())
    }
}