package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.dto.input.GymDTOInput
import com.gabrigiunchi.backendtesi.model.entities.City
import com.gabrigiunchi.backendtesi.model.entities.Comment
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.entities.User
import com.gabrigiunchi.backendtesi.model.time.Timetable
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.service.MapsService
import com.gabrigiunchi.backendtesi.service.UserService
import com.google.maps.model.LatLng
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class GymControllerTest : BaseTest()
{

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var commentDAO: CommentDAO

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @MockBean
    private lateinit var mockMapsService: MapsService


    private var city = City(CityEnum.NEW_YORK)

    @Before
    fun clearDB() {
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(LatLng(10.0, 10.0))
        this.gymDAO.deleteAll()
        this.timetableDAO.deleteAll()
        this.cityDAO.deleteAll()
        this.city = this.cityDAO.save(this.city)
        this.commentDAO.deleteAll()
    }

    @Test
    fun `Should get all the gyms in alphabetical order`() {
        this.gymDAO.saveAll(listOf(
                Gym("gym2", "address", this.city),
                Gym("gym1", "address", this.city),
                Gym("gym4", "address", this.city),
                Gym("gym3", "address", this.city)
        ))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name", Matchers.`is`("gym1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].name", Matchers.`is`("gym2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].name", Matchers.`is`("gym3")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[3].name", Matchers.`is`("gym4")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a gym by its id`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address", this.city, 43.0, 12.0))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.city.name", Matchers.`is`(gym.city.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.latitude", Matchers.`is`(gym.latitude)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.longitude", Matchers.`is`(gym.longitude)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the gyms in specific city`() {
        this.gymDAO.deleteAll()
        val cities = this.cityDAO.saveAll(CityEnum.values().map { City(it) }).toList()

        val gyms = this.gymDAO.saveAll(listOf(
                Gym("gym1", "Via1", cities[0]),
                Gym("gym2", "Via2", cities[0]),
                Gym("gym3", "Via3", cities[1]),
                Gym("gym4", "Via4", cities[2]))).toList()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/city/${cities[0].id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(gyms[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(gyms[1].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the gyms in specific city if the city does not exist`() {
        this.gymDAO.deleteAll()
        val cities = this.cityDAO.saveAll(CityEnum.values().map { City(it) }).toList()

        this.gymDAO.saveAll(listOf(
                Gym("gym1", "Via1", cities[0]),
                Gym("gym2", "Via2", cities[0]),
                Gym("gym3", "Via3", cities[1]),
                Gym("gym4", "Via4", cities[2]))).toList()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/city/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a gym if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a gym`() {
        val gym = GymDTOInput("gym dnjsnjdaj", "Via Pacchioni 43", this.city.id)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(gym.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.city.name", Matchers.`is`(this.city.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update a gym`() {
        val gym = this.gymDAO.save(Gym("gymaaa1", "Via Pacchioni 43", this.city))
        val savedGym = this.gymDAO.save(gym)
        val dto = GymDTOInput("new name", "new address", gym.city.id)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/${savedGym.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(dto.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.address", Matchers.`is`(dto.address)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.city.name", Matchers.`is`(gym.city.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update a gym if it does not exist`() {
        this.gymDAO.save(Gym("gym", "address", this.city))
        val gymDTO = GymDTOInput("dasa", "address", this.city.id)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gymDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update a gym if the city does not exist`() {
        val gym = this.gymDAO.save(Gym("gymnnnn1", "address", this.city))
        val dto = GymDTOInput(gym.name, gym.address, -1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update a gym if the address cannot be geocoded`() {
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(null)
        val gym = this.gymDAO.save(Gym("gymnnnn1", "address", this.city))
        val dto = GymDTOInput(gym.name, "new address", gym.city.id)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.GYMS}/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("cannot geocode address ${dto.address}")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a gym if its id already exist`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address", this.city))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a gym if its name already exist`() {
        val gym = Gym("A", "address", this.city)
        this.gymDAO.save(gym)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a gym if the city does not exist`() {
        this.cityDAO.deleteAll()
        val gym = GymDTOInput("A", "address", -1)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("city -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a gym if the address does not exist`() {
        this.cityDAO.deleteAll()
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(null)
        val address = "djksanjdna"
        val gym = GymDTOInput("A", address, this.mockCity.id)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.GYMS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(gym)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("cannot geocode address $address")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a gym`() {
        val gym = this.gymDAO.save(Gym("gym dsjad", "address", this.city))
        this.timetableDAO.save(Timetable(gym))
        val savedId = this.gymDAO.save(gym).id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.GYMS}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.timetableDAO.count()).isEqualTo(0)
    }

    @Test
    fun `Should not delete a gym if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.GYMS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    /************************************ RATING *************************************************************/
    @Test
    fun `Should calculate the rating of a gym`() {
        this.gymDAO.deleteAll()
        val user = this.mockUser()
        val gym = this.mockGym()

        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title", "message", 2),
                Comment(user, gym, "title", "message", 3),
                Comment(user, gym, "title", "message", 1),
                Comment(user, gym, "title", "message", 5),
                Comment(user, gym, "title", "message", 2)
        )).toList()

        val expectedResult = (2 + 3 + 1 + 5 + 2).toDouble() / comments.size.toDouble()
        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}/rating")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(expectedResult)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should calculate the rating of a gym and return -1 if no comments are present`() {
        this.gymDAO.deleteAll()
        val gym = this.mockGym()
        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/${gym.id}/rating")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(-1.0)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should throw an exception when calculating the rating of a gym if the gym does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.GYMS}/-1/rating")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    /******************************** UTILS ****************************************************/

    private fun mockUser(): User {
        return this.userDAO.save(this.userService.createRegularUser("adsa", "jns", "jnj", "njnj"))
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("gym1", "address1", this.mockCity))
    }

    private val mockCity: City
        get() = this.cityDAO.save(MockEntities.mockCities[0])
}