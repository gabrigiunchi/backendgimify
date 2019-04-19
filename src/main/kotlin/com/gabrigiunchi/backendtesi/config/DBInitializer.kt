package com.gabrigiunchi.backendtesi.config

import com.gabrigiunchi.backendtesi.config.security.SHA256PasswordEncoder
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.RegionEnum
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
    private lateinit var regionDAO: RegionDAO

    private var regions = listOf<Region>()

    fun initDB() {
        this.initRegions()
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
                Gym("gym1", "Via1", this.regions[0]),
                Gym("gym2", "Via2", this.regions[0]),
                Gym("gym3", "Via3", this.regions[1]),
                Gym("gym4", "Via4", this.regions[2])))
    }

    fun initAssetKinds() {
        this.assetKindDAO.saveAll(AssetKindEnum.values().map { e -> AssetKind(e) })
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

    fun initRegions() {
        this.regions = this.regionDAO.saveAll(RegionEnum.values().map { Region(it) }).toList()
    }
}