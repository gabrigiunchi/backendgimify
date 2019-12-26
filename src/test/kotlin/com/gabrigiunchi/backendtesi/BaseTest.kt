package com.gabrigiunchi.backendtesi

import com.fasterxml.jackson.databind.ObjectMapper
import com.gabrigiunchi.backendtesi.model.dto.input.ReservationDTOInput
import com.gabrigiunchi.backendtesi.model.dto.input.TimetableDTO
import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.time.RepeatedLocalInterval
import com.gabrigiunchi.backendtesi.service.MailService
import org.junit.Assert
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import java.io.IOException
import javax.transaction.Transactional

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = [BackendtesiApplication::class])
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "gabrigiunchi", password = "aaaa", authorities = ["ADMINISTRATOR"])
abstract class BaseTest
{

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @MockBean
    protected lateinit var mailService: MailService

    protected lateinit var mappingJackson2HttpMessageConverter: HttpMessageConverter<Any>

    @Autowired
    internal fun setConverters(converters: Array<HttpMessageConverter<Any>>)
    {
        this.mappingJackson2HttpMessageConverter = listOf(*converters).stream()
                .filter { hmc -> hmc is MappingJackson2HttpMessageConverter }.findAny().get()
        Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter)
    }

    @Before
    fun mockEmail()
    {
        Mockito.`when`(this.mailService.sendEmail(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
        ).thenReturn(true)
    }

    @Throws(IOException::class)
    protected fun json(o: Any): String
    {
        return ObjectMapper().writeValueAsString(o)
    }

    @Throws(IOException::class)
    protected fun json(gym: Gym): String
    {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("id", gym.id.toString()),
                Pair("name", gym.name),
                Pair("address", gym.address),
                Pair("city", this.toMap(gym.city))))
    }


    @Throws(IOException::class)
    protected fun json(reservation: ReservationDTOInput): String
    {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("userID", reservation.userID),
                Pair("assetID", reservation.assetID),
                Pair("start", reservation.start.toString()),
                Pair("end", reservation.end.toString())))
    }

    @Throws(IOException::class)
    protected fun json(city: City): String
    {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("id", city.id.toString()),
                Pair("name", city.name),
                Pair("zoneId", city.zoneId.toString())))
    }

    @Throws(IOException::class)
    protected fun json(timetable: TimetableDTO): String
    {
        return ObjectMapper().writeValueAsString(mapOf(
                Pair("gymId", timetable.gymId.toString()),
                Pair("closingDays", timetable.closingDays),
                Pair("openings", timetable.openings.map { toMap(it) }),
                Pair("closingDays", timetable.closingDays.map { toMap(it) }))
        )
    }

    fun toMap(repeatedInterval: RepeatedLocalInterval): Map<String, String>
    {
        val map = mutableMapOf(
                Pair("id", repeatedInterval.id.toString()),
                Pair("start", repeatedInterval.start.toString()),
                Pair("end", repeatedInterval.end.toString()),
                Pair("repetitionType", repeatedInterval.repetitionType.toString()))

        if (repeatedInterval.repetitionEnd != null)
        {
            map += Pair("repetitionEnd", repeatedInterval.repetitionEnd.toString())
        }

        return map
    }


    fun toMap(city: City): Map<String, String>
    {
        return mapOf(
                Pair("id", city.id.toString()),
                Pair("zoneId", city.zoneId.toString()),
                Pair("name", city.name))
    }
}