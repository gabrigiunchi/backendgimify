package com.gabrigiunchi.backendtesi

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabrigiunchi.backendtesi.model.City
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.RepeatedInterval
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.dto.input.TimetableDTO
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
    protected fun json(o: Any): String {
        return ObjectMapper().writeValueAsString(o)
    }

    @Throws(IOException::class)
    protected fun json(reservation: ReservationDTOInput): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("userID", reservation.userID),
                Pair("assetID", reservation.assetID),
                Pair("start", reservation.start.toString()),
                Pair("end", reservation.end.toString())))
    }

    @Throws(IOException::class)
    protected fun json(timetable: TimetableDTO): String {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("gymId", timetable.gymId.toString()),
                Pair("closingDays", timetable.closingDays),
                Pair("openings", timetable.openings.map { toMap(it) }),
                Pair("closingDays", timetable.closingDays.map { toMap(it) }))
        )
    }

    fun toMap(repeatedInterval: RepeatedInterval): Map<String, String> {
        return mapOf(
                Pair("id", repeatedInterval.id.toString()),
                Pair("start", repeatedInterval.start.toString()),
                Pair("end", repeatedInterval.end.toString()),
                Pair("repetitionType", repeatedInterval.repetitionType.toString()),
                Pair("repetitionEnd", repeatedInterval.repetitionEnd.toString())
        )
    }

    @Autowired
    internal fun setConverters(converters: Array<HttpMessageConverter<Any>>) {
        this.mappingJackson2HttpMessageConverter = Arrays.asList(*converters).stream()
                .filter { hmc -> hmc is MappingJackson2HttpMessageConverter }.findAny().get()
        Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter)
    }
}