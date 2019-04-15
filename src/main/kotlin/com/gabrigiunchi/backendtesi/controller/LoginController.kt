package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.config.security.JwtTokenProvider
import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.exceptions.AccessDeniedException
import com.gabrigiunchi.backendtesi.model.UserRole
import com.gabrigiunchi.backendtesi.model.dto.Token
import com.gabrigiunchi.backendtesi.model.dto.ValidateUserDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
class LoginController {

    private val logger = LoggerFactory.getLogger(LoginController::class.java)

    @Autowired
    internal var authenticationManager: AuthenticationManager? = null

    @Autowired
    internal var jwtTokenProvider: JwtTokenProvider? = null

    @Autowired
    internal var users: UserDAO? = null

    @Autowired
    private lateinit var tokenProvider: JwtTokenProvider

    @PostMapping
    fun login(@RequestBody @Valid credentials: ValidateUserDTO): ResponseEntity<Token> {
        this.logger.info("Login request: {username:" + credentials.username + ", password:" + credentials.password + "}")
        try {
            val username = credentials.username
            authenticationManager!!.authenticate(UsernamePasswordAuthenticationToken(username, credentials.password))

            val optionalUser = this.users!!.findByUsername(username)

            if (optionalUser.isEmpty) {
                throw BadCredentialsException("Invalid username/password supplied")
            }

            val user = optionalUser.get()
            val roles = user.roles.map(UserRole::name)
            val token = jwtTokenProvider!!.createToken(username, roles)

            return ResponseEntity(Token(credentials.username, token), HttpStatus.OK)
        } catch (e: AuthenticationException) {
            throw BadCredentialsException("Invalid username/password supplied")
        }
    }

    @PostMapping("/token")
    fun loginWithToken(@RequestBody token: String): ResponseEntity<Boolean> {
        this.logger.info("Login with token")
        val valid = try {
            this.tokenProvider.validateToken(token)
        } catch (e: AccessDeniedException) {
            false
        }

        return ResponseEntity(valid, HttpStatus.OK)
    }
}
