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
class UserController(val userDAO: UserDAO) : BaseController(userDAO) {

    val logger = LoggerFactory.getLogger(UserController::class.java)!!

    @GetMapping("/page/{page}/size/{size}")
    fun getAllUsers(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<UserDTO>> {
        this.logger.info("GET all users, page=$page size=$size")
        return ResponseEntity(this.userDAO.findAll(PageRequest.of(page, size)).map { e -> UserDTO(e) }, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getUserByid(@PathVariable id: Int): ResponseEntity<User> {
        this.logger.info("GET user #$id")
        return this.userDAO.findById(id)
                .map { ResponseEntity(it, HttpStatus.OK) }
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

    @PatchMapping("/{id}/active/{active}")
    fun enableUser(@PathVariable id: Int, @PathVariable active: Boolean): ResponseEntity<Void> {
        this.logger.info("PATCH to set user #$id active=$active")
        val user = this.userDAO.findById(id).orElseThrow { ResourceNotFoundException("user $id does not exist") }
        user.isActive = active
        this.userDAO.save(user)
        return ResponseEntity(HttpStatus.OK)
    }

    @PatchMapping("/{id}/notifications/active/{active}")
    fun enableUserNotifications(@PathVariable id: Int, @PathVariable active: Boolean): ResponseEntity<UserDTO> {
        this.logger.info("PATCH to set user #$id notifications=$active")
        val user = this.userDAO.findById(id).orElseThrow { ResourceNotFoundException("user $id does not exist") }
        user.notificationsEnabled = active
        return ResponseEntity(UserDTO(this.userDAO.save(user)), HttpStatus.OK)
    }

    /*************************************** ME ********************************************************************/

    @GetMapping("/me")
    fun getMyDetails(): ResponseEntity<User> {
        val loggedUser = this.getLoggedUser()
        this.logger.info("GET logged user (#${loggedUser.id})")
        return ResponseEntity(loggedUser, HttpStatus.OK)
    }

    @PatchMapping("/me/notifications/active/{active}")
    fun enableMyNotifications(@PathVariable active: Boolean): ResponseEntity<UserDTO> {
        val user = this.getLoggedUser()
        this.logger.info("PATCH to set logged user #${user.id} notifications=$active")
        user.notificationsEnabled = active
        return ResponseEntity(UserDTO(this.userDAO.save(user)), HttpStatus.OK)
    }
}