package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.Comment
import com.gabrigiunchi.backendtesi.model.Gym
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.model.dto.CommentDTO
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CommentControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var commentDAO: CommentDAO

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Before
    fun clearDB() {
        this.gymDAO.deleteAll()
        this.cityDAO.deleteAll()
        this.userDAO.deleteAll()
        this.commentDAO.deleteAll()
    }

    @Test
    fun `Should get all the comments`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title$it", "message$it", it) })
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get a comment by id`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val comments = this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title$it", "message$it", it) }).toList()
        val target = comments[0]
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/${target.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(target.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.`is`(target.title)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.`is`(target.message)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rating", Matchers.`is`(target.rating)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(target.user.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gym.id", Matchers.`is`(target.gym.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a comment by id if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the comments of a gym`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherGym = this.gymDAO.save(Gym("gym2", "another address", gym.city))
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(user, anotherGym, "title2", "message2", 2),
                Comment(user, anotherGym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(user, anotherGym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        )).toList()

        val expectedResult = listOf(comments[0], comments[3], comments[5])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/by_gym/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(expectedResult[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].id", Matchers.`is`(expectedResult[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the comments of a gym if the gym does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/by_gym/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the comments of a user`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherUser = this.userDAO.save(this.userFactory.createRegularUser("mmm", "m", "A", "B"))
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        )).toList()

        val expectedResult = listOf(comments[0], comments[3], comments[5])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/by_user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(expectedResult[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].id", Matchers.`is`(expectedResult[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get not the comments of a user if the user does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/by_user/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the comments filtered by user and gym`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherGym = this.gymDAO.save(Gym("gym2", "another address", gym.city))
        val anotherUser = this.userDAO.save(this.userFactory.createRegularUser("mmm", "m", "A", "B"))
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, anotherGym, "title6", "message6", 2)
        )).toList()

        val expectedResult = listOf(comments[0], comments[3])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/by_user/${user.id}/by_gym/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(expectedResult[1].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the comments filtered by user and gym if the user does not exist`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherUser = this.userDAO.save(this.userFactory.createRegularUser("mmm", "m", "A", "B"))
        this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        ))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/by_user/-1/by_gym/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the comments filtered by user and gym if the gym does not exist`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherUser = this.userDAO.save(this.userFactory.createRegularUser("mmm", "m", "A", "B"))
        this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        ))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/by_user/${user.id}/by_gym/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a comment`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val commentDTO = CommentDTO(user.id, gym.id, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.`is`(commentDTO.title)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.`is`(commentDTO.message)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rating", Matchers.`is`(commentDTO.rating)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(commentDTO.userId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gym.id", Matchers.`is`(commentDTO.gymId)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the user does not exist`() {
        val gym = this.mockGym()
        val commentDTO = CommentDTO(-1, gym.id, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the gym does not exist`() {
        val user = this.mockUser()
        val commentDTO = CommentDTO(user.id, -1, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the rating is less than 1`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val commentDTO = CommentDTO(user.id, gym.id, "wow this is a title", "wow this is a message", 0)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the rating is more than 5`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val commentDTO = CommentDTO(user.id, gym.id, "wow this is a title", "wow this is a message", 6)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete a comment by id`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val comments = this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title$it", "message$it", it) }).toList()
        val target = comments[0]
        this.mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.COMMENTS}/${target.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.commentDAO.findById(target.id).isEmpty).isTrue()
    }

    @Test
    fun `Should not delete a comment by id if it does not exist`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title$it", "message$it", it) }).toList()
        this.mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.COMMENTS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    /******************************* MY COMMENTS ***********************************************************************/
    @Test
    fun `Should get all my comments`() {
        val user = this.mockUser()
        val anotherUser = this.userDAO.save(this.userFactory.createRegularUser("jn", "Jn", "j", "km"))
        val gym = this.mockGym()
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        )).toList()

        val expectedResult = listOf(comments[0], comments[3], comments[5])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id", Matchers.`is`(expectedResult[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].id", Matchers.`is`(expectedResult[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get my comment by id`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val comments = this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title$it", "message$it", it) }).toList()
        val target = comments[0]
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/${target.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(target.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.`is`(target.title)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.`is`(target.message)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rating", Matchers.`is`(target.rating)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(target.user.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gym.id", Matchers.`is`(target.gym.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get one of my comment by id if it does not exist`() {
        this.mockUser()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get my comments by gym`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherGym = this.gymDAO.save(Gym("gym2", "another address", gym.city))
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(user, anotherGym, "title2", "message2", 2),
                Comment(user, anotherGym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(user, anotherGym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        )).toList()

        val expectedResult = listOf(comments[0], comments[3], comments[5])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/by_gym/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.`is`(expectedResult[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].id", Matchers.`is`(expectedResult[2].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user.username", Matchers.`is`(expectedResult[0].user.username)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get my comments by gym if the gym does not exist`() {
        this.mockUser()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/by_gym/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a comment for the logged user`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val commentDTO = CommentDTO(user.id, gym.id, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post("${ApiUrls.COMMENTS}/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.`is`(commentDTO.title)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.`is`(commentDTO.message)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rating", Matchers.`is`(commentDTO.rating)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(commentDTO.userId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gym.id", Matchers.`is`(commentDTO.gymId)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment for the logged user if the gym does not exist`() {
        val user = this.mockUser()
        val commentDTO = CommentDTO(user.id, -1, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post("${ApiUrls.COMMENTS}/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete one of my comment by id`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val comments = this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title$it", "message$it", it) }).toList()
        val target = comments[0]
        this.mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.COMMENTS}/me/${target.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())

        Assertions.assertThat(this.commentDAO.findById(target.id).isEmpty).isTrue()
    }

    @Test
    fun `Should not delete one of my comment by id if it does not exist`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title$it", "message$it", it) }).toList()
        this.mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.COMMENTS}/me/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }


    /************************************** UTILS *********************************************************************/

    private fun mockUser(username: String = "gabrigiunchi"): User {
        return this.userDAO.save(this.userFactory.createRegularUser(username, "aaaa", "Gabriele", "Giunchi"))
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("gym1", "address", this.cityDAO.save(MockEntities.mockCities[0])))
    }
}