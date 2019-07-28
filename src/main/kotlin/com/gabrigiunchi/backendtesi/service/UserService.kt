package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.UserDAO
import com.gabrigiunchi.backendtesi.dao.UserRoleDAO
import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.ChangePasswordDTO
import com.gabrigiunchi.backendtesi.model.dto.input.UserDTOInput
import com.gabrigiunchi.backendtesi.model.entities.User
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
        private val userDAO: UserDAO,
        private val userRoleDAO: UserRoleDAO) {

    fun createAdminUser(username: String, password: String, name: String, surname: String, email: String = "prova@server.com"): User {
        val role = this.userRoleDAO.findByName(UserRoleEnum.ADMINISTRATOR.toString()).get()
        return User(username, BCryptPasswordEncoder().encode(password), name, surname, email, mutableListOf(role))
    }

    fun createRegularUser(username: String, password: String, name: String, surname: String, email: String = "prova@server.com"): User {
        val role = this.userRoleDAO.findByName(UserRoleEnum.USER.toString()).get()
        return User(username, BCryptPasswordEncoder().encode(password), name, surname, email, mutableListOf(role))
    }

    fun createUser(dto: UserDTOInput): User {
        val roles = dto.roles.map { this.userRoleDAO.findByName(it).orElseThrow { BadRequestException("user role $it does not exist") } }
        val user = User(dto.username, BCryptPasswordEncoder().encode(dto.password), dto.name, dto.surname, dto.email, roles.toMutableList())
        user.isActive = dto.isActive
        user.notificationsEnabled = dto.notificationsEnabled
        return user
    }

    fun modifyUser(dto: UserDTOInput, id: Int): User {
        val roles = dto.roles.map { this.userRoleDAO.findByName(it).orElseThrow { BadRequestException("user role $it does not exist") } }
        val savedUser = this.userDAO.findById(id).orElseThrow { ResourceNotFoundException("user $id does not exist") }
        savedUser.username = dto.username
        savedUser.notificationsEnabled = dto.notificationsEnabled
        savedUser.isActive = dto.isActive
        savedUser.email = dto.email
        savedUser.name = dto.name
        savedUser.surname = dto.surname
        savedUser.password = BCryptPasswordEncoder().encode(dto.password)
        savedUser.roles.clear()
        savedUser.roles.addAll(roles)
        return this.userDAO.save(savedUser)
    }

    fun modifyPasswordOfUser(user: User, dto: ChangePasswordDTO): User {
        val encoder = BCryptPasswordEncoder()
        if (!encoder.matches(dto.oldPassword, user.password)) {
            throw BadRequestException("Old password is incorrect")
        }

        user.password = encoder.encode(dto.newPassword)
        return this.userDAO.save(user)
    }
}