package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.model.dto.output.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
class UserController(private val userDAO: UserDAO) {

    val logger = LoggerFactory.getLogger(UserController::class.java)!!

    @GetMapping("/page/{page}/size/{size}")
    fun getAllUsers(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<UserDTO>> {
        this.logger.info("GET all users, page=$page size=$size")
        return ResponseEntity(this.userDAO.findAll(PageRequest.of(page, size)).map { e -> UserDTO(e) }, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getUserByid(@PathVariable id: Int): ResponseEntity<UserDTO> {
        this.logger.info("GET REQUEST for user #$id")
        return this.userDAO.findById(id)
                .map { user -> ResponseEntity(UserDTO(user), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("user $id does not exist") }
    }

    @GetMapping("/{id}/details")
    fun getUserDetails(@PathVariable id: Int): ResponseEntity<User> {
        this.logger.info("GET user #$id")
        return this.userDAO.findById(id)
                .map { user -> ResponseEntity(user, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("user $id does not exist") }
    }

    @PostMapping
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<UserDTO> {
        this.logger.info("POST a new user")
        this.logger.info(user.toString())

        if (this.userDAO.findById(user.id).isPresent || this.userDAO.findByUsername(user.username).isPresent) {
            throw ResourceAlreadyExistsException("user ${user.id} with username ${user.username} already exists")
        }

        return ResponseEntity(UserDTO(this.userDAO.save(user)), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE user #$id")
        val user = this.userDAO.findById(id).orElseThrow { ResourceNotFoundException("user $id does not exist") }
        this.userDAO.delete(user)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}