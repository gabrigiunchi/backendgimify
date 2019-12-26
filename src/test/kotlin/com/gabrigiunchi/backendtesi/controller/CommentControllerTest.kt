package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.model.dto.input.CommentDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Comment
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.OffsetDateTime

class CommentControllerTest : BaseTest() {

    @Autowired
    private lateinit var commentDAO: CommentDAO

    @Before
    fun clearDB() {
        this.gymDAO.deleteAll()
        this.cityDAO.deleteAll()
        this.userDAO.deleteAll()
        this.commentDAO.deleteAll()
    }

    @Test
    fun `Should get all the comments paged`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        this.commentDAO
                .saveAll((1..30).map { Comment(user, gym, "title$it", "m$it", 1) })
                .toList()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/page/0/size/20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(20)))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.gymId", Matchers.`is`(target.gym.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get a comment by id if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("comment -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the comments of a gym`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherGym = this.gymDAO.save(Gym("gym2", "another address", gym.city))
        val d = OffsetDateTime.now()
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1, d),
                Comment(user, anotherGym, "title2", "message2", 2, d.minusMinutes(10)),
                Comment(user, anotherGym, "title3", "message3", 3, d.minusMinutes(15)),
                Comment(user, gym, "title4", "message4", 5, d.minusMinutes(20)),
                Comment(user, anotherGym, "title5", "message5", 4, d.minusMinutes(25)),
                Comment(user, gym, "title6", "message6", 2, d.minusMinutes(30))
        )).toList()

        val expectedResult = listOf(comments[0], comments[3], comments[5])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/gym/${gym.id}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.`is`(expectedResult[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id", Matchers.`is`(expectedResult[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the comments of a gym if the gym does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/gym/-1/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the comments of a user`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherUser = this.userDAO.save(this.userService.createRegularUser("mmm", "m", "A", "B"))
        val d = OffsetDateTime.now()
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1, d),
                Comment(anotherUser, gym, "title2", "message2", 2, d.minusMinutes(10)),
                Comment(anotherUser, gym, "title3", "message3", 3, d.minusMinutes(15)),
                Comment(user, gym, "title4", "message4", 5, d.minusMinutes(20)),
                Comment(anotherUser, gym, "title5", "message5", 4, d.minusMinutes(25)),
                Comment(user, gym, "title6", "message6", 2, d.minusMinutes(30))
        )).toList()

        val expectedResult = listOf(comments[0], comments[3], comments[5])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/user/${user.id}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id", Matchers.`is`(expectedResult[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[2].id", Matchers.`is`(expectedResult[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get not the comments of a user if the user does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/user/-1/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get the comments filtered by user and gym`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherGym = this.gymDAO.save(Gym("gym2", "another address", gym.city))
        val anotherUser = this.userDAO.save(this.userService.createRegularUser("mmm", "m", "A", "B"))
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, anotherGym, "title6", "message6", 2)
        )).toList()

        val expectedResult = listOf(comments[0], comments[3])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/user/${user.id}/gym/${gym.id}")
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
        val anotherUser = this.userDAO.save(this.userService.createRegularUser("mmm", "m", "A", "B"))
        this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        ))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/user/-1/gym/${gym.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get the comments filtered by user and gym if the gym does not exist`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val anotherUser = this.userDAO.save(this.userService.createRegularUser("mmm", "m", "A", "B"))
        this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1),
                Comment(anotherUser, gym, "title2", "message2", 2),
                Comment(anotherUser, gym, "title3", "message3", 3),
                Comment(user, gym, "title4", "message4", 5),
                Comment(anotherUser, gym, "title5", "message5", 4),
                Comment(user, gym, "title6", "message6", 2)
        ))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/user/${user.id}/gym/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create a comment`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val commentDTO = CommentDTOInput(user.id, gym.id, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.`is`(commentDTO.title)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.`is`(commentDTO.message)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rating", Matchers.`is`(commentDTO.rating)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(commentDTO.userId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gymId", Matchers.`is`(commentDTO.gymId)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the user does not exist`() {
        val gym = this.mockGym()
        val commentDTO = CommentDTOInput(-1, gym.id, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("user -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the gym does not exist`() {
        val user = this.mockUser()
        val commentDTO = CommentDTOInput(user.id, -1, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the rating is less than 1`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val commentDTO = CommentDTOInput(user.id, gym.id, "wow this is a title", "wow this is a message", 0)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("rating must be between 1 and 5")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the rating is more than 5`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val commentDTO = CommentDTOInput(user.id, gym.id, "wow this is a title", "wow this is a message", 6)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("rating must be between 1 and 5")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the message is more than 140 characters`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val message = "a".repeat(141)
        Assertions.assertThat(message.length).isEqualTo(141)
        val commentDTO = CommentDTOInput(user.id, gym.id, "wow this is a title", message, 3)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("message must be at most 140 characters")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment if the title is more than 40 characters`() {
        val user = this.mockUser()
        val gym = this.mockGym()
        val title = "a".repeat(41)
        Assertions.assertThat(title.length).isEqualTo(41)
        val commentDTO = CommentDTOInput(user.id, gym.id, title, "message", 3)
        this.mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.COMMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("title must be at most 40 characters")))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("comment -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    /******************************* MY COMMENTS ***********************************************************************/
    @Test
    fun `Should get all my comments`() {
        val user = this.mockUser()
        val anotherUser = this.userDAO.save(this.userService.createRegularUser("jn", "Jn", "j", "km"))
        val gym = this.mockGym()
        val d = OffsetDateTime.now()
        val comments = this.commentDAO.saveAll(listOf(
                Comment(user, gym, "title1", "message1", 1, d),
                Comment(anotherUser, gym, "title2", "message2", 2, d.minusMinutes(10)),
                Comment(anotherUser, gym, "title3", "message3", 3, d.minusMinutes(15)),
                Comment(user, gym, "title4", "message4", 5, d.minusMinutes(20)),
                Comment(anotherUser, gym, "title5", "message5", 4, d.minusMinutes(25)),
                Comment(user, gym, "title6", "message6", 2, d.minusMinutes(30))
        )).toList()

        val expectedResult = listOf(comments[0], comments[3], comments[5])
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[0].id", Matchers.`is`(expectedResult[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[1].id", Matchers.`is`(expectedResult[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[2].id", Matchers.`is`(expectedResult[2].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should return the number comments I made`() {
        this.userDAO.deleteAll()
        val user = this.mockUser("gabrigiunchi")
        val gym = this.mockGym()
        this.commentDAO.saveAll((1..4).map { Comment(user, gym, "title", "message", 1) })

        mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.`is`(4)))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.gymId", Matchers.`is`(target.gym.id)))
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
    fun `Should not get one of my comment by id if it does not belong to me`() {
        val gym = this.mockGym()
        this.mockUser("gabrigiunchi")
        val user2 = this.userDAO.save(this.userService.createRegularUser("another user", "aaaa", "n", "m"))
        val comment = this.commentDAO.save(Comment(user2, gym, "", "", 1))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/${comment.id}")
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
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.COMMENTS}/me/gym/${gym.id}")
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
        val commentDTO = CommentDTOInput(user.id, gym.id, "wow this is a title", "wow this is a message", 2)
        this.mockMvc.perform(MockMvcRequestBuilders.post("${ApiUrls.COMMENTS}/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(commentDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.`is`(commentDTO.title)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.`is`(commentDTO.message)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rating", Matchers.`is`(commentDTO.rating)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id", Matchers.`is`(commentDTO.userId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gymId", Matchers.`is`(commentDTO.gymId)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create a comment for the logged user if the gym does not exist`() {
        val user = this.mockUser()
        val commentDTO = CommentDTOInput(user.id, -1, "wow this is a title", "wow this is a message", 2)
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

    @Test
    fun `Should not delete one of my comment by id if it does not belong to me`() {
        val gym = this.mockGym()
        this.mockUser("gabrigiunchi")
        val user2 = this.userDAO.save(this.userService.createRegularUser("another user", "aaaa", "n", "m"))
        val comment = this.commentDAO.save(Comment(user2, gym, "", "", 1))
        this.mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.COMMENTS}/me/${comment.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}