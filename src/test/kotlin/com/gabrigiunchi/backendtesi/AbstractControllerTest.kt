package com.gabrigiunchi.backendtesi

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import java.io.IOException
import java.util.*
import javax.transaction.Transactional

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = [BackendtesiApplication::class])
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
abstract class AbstractControllerTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    protected lateinit var mappingJackson2HttpMessageConverter: HttpMessageConverter<Any>

    @Throws(IOException::class)
    protected fun json(o: Any): String
    {
        return ObjectMapper().writeValueAsString(o)
    }

    @Autowired
    internal fun setConverters(converters: Array<HttpMessageConverter<Any>>)
    {
        this.mappingJackson2HttpMessageConverter = Arrays.asList(*converters).stream()
                .filter { hmc -> hmc is MappingJackson2HttpMessageConverter }.findAny().get()
        Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter)
    }
}