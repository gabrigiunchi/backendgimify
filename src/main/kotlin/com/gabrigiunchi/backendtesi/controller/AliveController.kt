package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.model.entities.User
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/alive")
class AliveController(userDAO: UserDAO) : BaseController(userDAO) {

    @GetMapping
    fun alive(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(Pair("message", "Everything's fine")))
    }

    @GetMapping("/me")
    fun whoAmI(): ResponseEntity<User> {
        return ResponseEntity.ok(this.getLoggedUser())
    }

    @GetMapping("/me/am_I_admin")
    fun amIdAmin(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(this.isAdmin())
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @GetMapping("/secret")
    fun secret(): ResponseEntity<String> {
        return ResponseEntity.ok("This endpoint is for administrators only. If you are not this is a problem")
    }

}