package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Comment
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class GymServiceTest : AbstractControllerTest() {

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Autowired
    private lateinit var commentDAO: CommentDAO

    @Autowired
    private lateinit var gymService: GymService

    @Autowired
    private lateinit var userFactory: UserFactory

    @Autowired
    private lateinit var userDAO: UserDAO

    @Before
    fun clearDB() {
        this.gymDAO.deleteAll()
        this.cityDAO.deleteAll()
        this.commentDAO.deleteAll()
    }

    @Test
    fun `Should calculate the rating of a gym`() {
        val user = this.createUser()
        val gym = this.createGym()

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
        Assertions.assertThat(this.gymService.calculateRatingOfGym(this.createGym().id)).isEqualTo(-1.0)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should throw an exception when calculating the rating of a gym if the gym does not exist`() {
        this.gymService.calculateRatingOfGym(-1)
    }

    private fun createUser(): User {
        return this.userDAO.save(this.userFactory.createRegularUser("adsa", "jns", "jnj", "njnj"))
    }

    private fun createGym(): Gym {
        val city = this.cityDAO.save(MockEntities.mockCities[0])
        return this.gymDAO.save(Gym("gym1", "address1", city))
    }
}