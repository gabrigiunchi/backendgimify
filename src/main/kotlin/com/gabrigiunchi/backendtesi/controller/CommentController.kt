package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.CommentDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.Comment
import com.gabrigiunchi.backendtesi.model.dto.CommentDTO
import org.slf4j.LoggerFactory
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/comments")
class CommentController(
        private val commentDAO: CommentDAO,
        private val userDAO: UserDAO,
        private val gymDAO: GymDAO
) {

    private val logger = LoggerFactory.getLogger(CommentController::class.java)

    @GetMapping
    fun getAllComments(): ResponseEntity<Iterable<Comment>> {
        this.logger.info("GET all comments")
        return ResponseEntity(this.commentDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getCommentById(@PathVariable id: Int): ResponseEntity<Comment> {
        this.logger.info("GET comment $id")
        return this.commentDAO.findById(id)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("comment $id does not exist") }
    }

    @GetMapping("/by_gym/{gymId}")
    fun getCommentsByGym(@PathVariable gymId: Int): ResponseEntity<Collection<Comment>> {
        this.logger.info("GET comments of gym $gymId")
        return this.gymDAO.findById(gymId)
                .map { ResponseEntity(this.commentDAO.findByGym(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    @GetMapping("/by_user/{userId}")
    fun getCommentsByUser(@PathVariable userId: Int): ResponseEntity<Collection<Comment>> {
        this.logger.info("GET comments of user $userId")
        return this.userDAO.findById(userId)
                .map { ResponseEntity(this.commentDAO.findByUser(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("user $userId does not exist") }
    }

    @GetMapping("/by_user/{userId}/by_gym/{gymId}")
    fun getCommentsByUserAndGym(@PathVariable userId: Int, @PathVariable gymId: Int): ResponseEntity<Collection<Comment>> {
        this.logger.info("GET comments of user $userId of gym $gymId")

        if (this.userDAO.findById(userId).isEmpty) {
            throw ResourceNotFoundException("user $userId does not exist")
        }

        if (this.gymDAO.findById(gymId).isEmpty) {
            throw ResourceNotFoundException("gym $gymId does not exist")
        }

        return ResponseEntity(
                this.commentDAO.findByUserAndGym(this.userDAO.findById(userId).get(), this.gymDAO.findById(gymId).get()),
                HttpStatus.OK)
    }

    @PostMapping
    fun createComment(@Valid @RequestBody commentDTO: CommentDTO): ResponseEntity<Comment> {
        this.logger.info("POST comment")

        if (this.userDAO.findById(commentDTO.userId).isEmpty) {
            throw ResourceNotFoundException("user ${commentDTO.userId} does not exist")
        }

        if (this.gymDAO.findById(commentDTO.gymId).isEmpty) {
            throw ResourceNotFoundException("gym ${commentDTO.gymId} does not exist")
        }

        return ResponseEntity(this.commentDAO.save(
                Comment(
                        this.userDAO.findById(commentDTO.userId).get(),
                        this.gymDAO.findById(commentDTO.gymId).get(),
                        commentDTO.title,
                        commentDTO.message,
                        commentDTO.rating)
        ), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteCommentById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE comment $id")

        if (this.commentDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("comment $id does not exist")
        }

        this.commentDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}