package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.GymDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Comment
import com.google.maps.model.LatLng
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

class GymServiceTest : BaseTest() {
    @Autowired
    private lateinit var commentDAO: CommentDAO

    @Autowired
    private lateinit var gymService: GymService

    @MockBean
    private lateinit var mockMapsService: MapsService


    @Before
    fun clearDB() {
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(LatLng(10.0, 10.0))
        this.gymDAO.deleteAll()
        this.cityDAO.deleteAll()
        this.commentDAO.deleteAll()
    }

    @Test
    fun `Should calculate the rating of a gym`() {
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
        Assertions.assertThat(this.gymService.calculateRatingOfGym(gym.id)).isEqualTo(expectedResult)
    }

    @Test
    fun `Should calculate the rating of a gym and return -1 if no comments are present`() {
        Assertions.assertThat(this.gymService.calculateRatingOfGym(this.mockGym().id)).isEqualTo(-1.0)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when calculating the rating of a gym if the gym does not exist`() {
        this.gymService.calculateRatingOfGym(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should not create a gym if the address is not found`() {
        Mockito.`when`(mockMapsService.geocode(Mockito.anyString())).thenReturn(null)
        this.gymService.saveGym(GymDTOInput("gym", "dnsadas", this.mockCity.id))
    }
}