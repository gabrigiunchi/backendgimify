package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.User
import com.gabrigiunchi.backendtesi.model.dto.output.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/users")
class UserController(
        @Autowired val userDAO: UserDAO
) {

    val logger = LoggerFactory.getLogger(UserController::class.java)!!

    @GetMapping
    fun getAllUsers(): ResponseEntity<Iterable<UserDTO>> {
        this.logger.info("GET REQUEST for all users")
        val users = this.userDAO.findAll()
                .map { e -> UserDTO(e) }
                .toList()

        return ResponseEntity(users, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getUserByid(@PathVariable id: Int): ResponseEntity<UserDTO> {
        this.logger.info("GET REQUEST for user #$id")
        return this.userDAO.findById(id)
                .map { user -> ResponseEntity(UserDTO(user), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @GetMapping("/{id}/details")
    fun getUserDetails(@PathVariable id: Int): ResponseEntity<User> {
        this.logger.info("GET REQUEST for user #$id")
        return this.userDAO.findById(id)
                .map { user -> ResponseEntity(user, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @PostMapping
    fun createUser(@Valid @RequestBody user: User): ResponseEntity<UserDTO> {
        this.logger.info("GET REQUEST to create a new user")
        this.logger.info(user.toString())

        if (this.userDAO.findById(user.id).isPresent || this.userDAO.findByUsername(user.username).isPresent) {
            throw ResourceAlreadyExistsException(user.id)
        }

        return ResponseEntity(UserDTO(this.userDAO.save(user)), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Int, @Valid @RequestBody user: User): ResponseEntity<User> {
        this.logger.info("PUT user #$id")

        if (this.userDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(id)
        }

        return ResponseEntity(this.userDAO.save(user), HttpStatus.OK)
    }


    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE user #$id")
        if (this.userDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(id)
        }

        this.userDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}