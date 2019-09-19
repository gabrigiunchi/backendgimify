package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.CommentDTOInput
import com.gabrigiunchi.backendtesi.model.dto.output.CommentDTOOutput
import com.gabrigiunchi.backendtesi.model.entities.Comment
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/comments")
class CommentController(
        private val commentDAO: CommentDAO,
        private val userDAO: UserDAO,
        private val gymDAO: GymDAO) : BaseController(userDAO) {

    private val logger = LoggerFactory.getLogger(CommentController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAllComments(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<CommentDTOOutput>> {
        this.logger.info("GET all comments, page $page and size $size")
        return ResponseEntity.ok(this.commentDAO.findAll(this.pageRequest(page, size)).map { CommentDTOOutput(it) })
    }

    @GetMapping("/{id}")
    fun getCommentById(@PathVariable id: Int): ResponseEntity<CommentDTOOutput> {
        this.logger.info("GET comment $id")
        return this.commentDAO.findById(id)
                .map { ResponseEntity.ok(CommentDTOOutput(it)) }
                .orElseThrow { ResourceNotFoundException("comment $id does not exist") }
    }

    @GetMapping("/gym/{gymId}/page/{page}/size/{size}")
    fun getCommentsByGym(@PathVariable gymId: Int, @PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<CommentDTOOutput>> {
        this.logger.info("GET comments of gym $gymId")
        return this.gymDAO.findById(gymId)
                .map { gym ->
                    ResponseEntity.ok(this.commentDAO.findByGym(gym, this.pageRequest(page, size)).map { CommentDTOOutput(it) })
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    @GetMapping("/user/{userId}/page/{page}/size/{size}")
    fun getCommentsByUser(@PathVariable userId: Int, @PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<CommentDTOOutput>> {
        this.logger.info("GET comments of user $userId")
        return this.userDAO.findById(userId)
                .map { user ->
                    ResponseEntity.ok(this.commentDAO.findByUser(user, this.pageRequest(page, size)).map { CommentDTOOutput(it) })
                }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    @GetMapping("/user/{userId}/gym/{gymId}")
    fun getCommentsByUserAndGym(@PathVariable userId: Int, @PathVariable gymId: Int): ResponseEntity<Collection<CommentDTOOutput>> {
        this.logger.info("GET comments of user $userId of gym $gymId")
        val user = this.userDAO.findById(userId).orElseThrow { ResourceNotFoundException("user $userId does not exist") }
        val gym = this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
        return ResponseEntity.ok(this.commentDAO.findByUserAndGym(user, gym).map { CommentDTOOutput(it) })
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createComment(@Valid @RequestBody commentDTO: CommentDTOInput): ResponseEntity<CommentDTOOutput> {
        this.logger.info("POST comment")
        val user = this.userDAO.findById(commentDTO.userId).orElseThrow { ResourceNotFoundException("user ${commentDTO.userId} does not exist") }
        val gym = this.gymDAO.findById(commentDTO.gymId).orElseThrow { ResourceNotFoundException("gym ${commentDTO.gymId} does not exist") }
        return ResponseEntity(
                CommentDTOOutput(this.commentDAO.save(Comment(user, gym, commentDTO.title, commentDTO.message, commentDTO.rating))),
                HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteCommentById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE comment $id")
        val comment = this.commentDAO.findById(id).orElseThrow { ResourceNotFoundException("comment $id does not exist") }
        this.commentDAO.delete(comment)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    /************************************ MY COMMENTS ***************************************************************/

    @GetMapping("/me/page/{page}/size/{size}")
    fun getAllMyComments(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<CommentDTOOutput>> {
        val user = this.getLoggedUser()
        this.logger.info("GET all comments of logged user (${user.id}), page=$page size=$size")
        return ResponseEntity.ok(this.commentDAO.findByUser(user, this.pageRequest(page, size)).map { CommentDTOOutput(it) })
    }

    @GetMapping("/me/count")
    fun countMyComments(): ResponseEntity<Long> {
        val user = this.getLoggedUser()
        this.logger.info("GET number of comments made by logged user (#${user.id})")
        return ResponseEntity.ok(this.commentDAO.findByUser(user, this.pageRequest(0, 1)).totalElements)
    }

    @GetMapping("/me/{id}")
    fun getMyCommentById(@PathVariable id: Int): ResponseEntity<CommentDTOOutput> {
        return this.commentDAO.findByIdAndUser(id, this.getLoggedUser())
                .map { ResponseEntity.ok(CommentDTOOutput(it)) }
                .orElseThrow { ResourceNotFoundException("comment $id of user ${this.getLoggedUser().id} does not exist") }
    }

    @GetMapping("/me/gym/{gymId}")
    fun getMyCommentsByGym(@PathVariable gymId: Int): ResponseEntity<Collection<CommentDTOOutput>> {
        val user = this.getLoggedUser()
        this.logger.info("GET comments of gym $gymId of logged user ${user.id})")
        return this.getCommentsByUserAndGym(user.id, gymId)
    }

    @PostMapping("/me")
    fun createCommentForLoggedUser(@Valid @RequestBody commentDTO: CommentDTOInput): ResponseEntity<CommentDTOOutput> {
        val user = this.getLoggedUser()
        this.logger.info("POST comment for logged user ${user.id})")

        val gym = this.gymDAO.findById(commentDTO.gymId)
                .orElseThrow { ResourceNotFoundException("gym ${commentDTO.gymId} does not exist") }

        return ResponseEntity(
                CommentDTOOutput(this.commentDAO.save(Comment(user, gym, commentDTO.title, commentDTO.message, commentDTO.rating))),
                HttpStatus.CREATED)
    }

    @DeleteMapping("/me/{id}")
    fun deleteMyCommentById(@PathVariable id: Int): ResponseEntity<Void> {
        val user = this.getLoggedUser()
        this.logger.info("DELETE comment $id of logged user (${user.id})")

        if (this.commentDAO.findByIdAndUser(id, user).isEmpty) {
            throw ResourceNotFoundException("comment $id of logged user (${user.id}) does not exist")
        }

        this.commentDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    private fun pageRequest(page: Int, size: Int, sort: Sort = Sort.by("date").descending()): PageRequest {
        return PageRequest.of(page, size, sort)
    }

}