package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.config.security.JwtTokenProvider
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.AccessDeniedException
import com.gabrigiunchi.backendtesi.model.dto.input.ValidateUserDTO
import com.gabrigiunchi.backendtesi.model.dto.output.Token
import com.gabrigiunchi.backendtesi.model.dto.output.UserDTO
import com.gabrigiunchi.backendtesi.model.entities.UserRole
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/login")
class LoginController(
        private val authenticationManager: AuthenticationManager,
        private val userDAO: UserDAO,
        private val jwtTokenProvider: JwtTokenProvider
) {

    private val logger = LoggerFactory.getLogger(LoginController::class.java)

    @PostMapping
    fun login(@RequestBody @Valid credentials: ValidateUserDTO): ResponseEntity<Token> {
        this.logger.info("Login request: {username:" + credentials.username + ", password:" + credentials.password + "}")
        try {
            val username = credentials.username
            this.authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, credentials.password))

            val optionalUser = this.userDAO.findByUsername(username)

            if (optionalUser.isEmpty) {
                throw BadCredentialsException("Invalid username/password supplied")
            }

            val user = optionalUser.get()
            val roles = user.roles.map(UserRole::name)
            val token = this.jwtTokenProvider.createToken(username, roles)

            return ResponseEntity(Token(UserDTO(user), token), HttpStatus.OK)
        } catch (e: AuthenticationException) {
            throw BadCredentialsException("Invalid username/password supplied")
        }
    }

    @PostMapping("/token")
    fun loginWithToken(@RequestBody token: String): ResponseEntity<Boolean> {
        this.logger.info("Login with token")
        val valid = try {
            this.jwtTokenProvider.validateToken(token)
        } catch (e: AccessDeniedException) {
            false
        }

        return ResponseEntity(valid, HttpStatus.OK)
    }
}
