package com.gabrigiunchi.backendtesi.config

import com.gabrigiunchi.backendtesi.config.security.SHA256PasswordEncoder
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DBInitializer {

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userRoleDAO: UserRoleDAO

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    private var cities = listOf<City>()

    private val maxReservationTimes = mapOf(
            Pair(AssetKindEnum.CICLETTE, 60),
            Pair(AssetKindEnum.PANCA, 20),
            Pair(AssetKindEnum.PRESSA, 20),
            Pair(AssetKindEnum.TAPIS_ROULANT, 60))

    fun initDB() {
        this.initCities()
        this.initUserRole()
        this.initUsers()
        this.initGyms()
        this.initAssetKinds()
        this.initAssets()
    }

    fun initUserRole() {
        this.userRoleDAO.saveAll(
                UserRoleEnum.values().map { value -> UserRole(-1, value.toString()) }
        )
    }

    fun initUsers() {
        val roles = this.userRoleDAO.findAll().toList()

        val users = listOf(
                User("gabrigiunchi", SHA256PasswordEncoder().encode("aaaa"), "Gabriele", "Giunchi", mutableListOf(roles[0])),
                User("baseuser", SHA256PasswordEncoder().encode("bbbb"), "User", "Anonimo", mutableListOf(roles[1]))
        )

        this.userDAO.saveAll(users)
    }

    fun initGyms() {
        this.gymDAO.saveAll(listOf(
                Gym("gym1", "Via1", this.cities[0]),
                Gym("gym2", "Via2", this.cities[0]),
                Gym("gym3", "Via3", this.cities[1]),
                Gym("gym4", "Via4", this.cities[2])))
    }

    fun initAssetKinds() {
        this.assetKindDAO.saveAll(AssetKindEnum.values().map { AssetKind(it, this.maxReservationTimes[it] ?: 20) })
    }

    fun initAssets() {
        val gyms = this.gymDAO.findAll().toList()
        this.assetDAO.saveAll(listOf(
                Asset("tr01", this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get(), gyms[0]),
                Asset("c01", this.assetKindDAO.findByName(AssetKindEnum.CICLETTE.name).get(), gyms[0]),
                Asset("p01", this.assetKindDAO.findByName(AssetKindEnum.PANCA.name).get(), gyms[1]),
                Asset("tr01", this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get(), gyms[2])
        ))
    }

    fun initCities() {
        this.cities = this.cityDAO.saveAll(CityEnum.values().map { City(it) }).toList()
    }
}