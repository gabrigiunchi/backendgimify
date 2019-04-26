package com.gabrigiunchi.backendtesi.config

import com.gabrigiunchi.backendtesi.config.security.SHA256PasswordEncoder
import com.gabrigiunchi.backendtesi.constants.Constants
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.DayOfWeek

@Service
class DBInitializer {

    private val logger = LoggerFactory.getLogger(DBInitializer::class.java)

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

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    private var cities = listOf<City>()
    private var gyms = listOf<Gym>()
    private var roles = listOf<UserRole>()

    private val maxReservationTimes = mapOf(
            Pair(AssetKindEnum.CICLETTE, 60),
            Pair(AssetKindEnum.PANCA, 20),
            Pair(AssetKindEnum.PRESSA, 20),
            Pair(AssetKindEnum.TAPIS_ROULANT, 60))

    fun initDB() {
        this.logger.info("Initializing DB")
        this.initCities()
        this.initUsers()
        this.initGyms()
        this.initAssetKinds()
        this.initAssets()
        this.initTimetables()
        this.logger.info("DB initialized")
    }

    fun initUserRole() {
        this.roles = this.userRoleDAO.saveAll(UserRoleEnum.values().map { value -> UserRole(-1, value.toString()) }).toList()
        this.logger.info("Init user roles")
    }

    fun initUsers() {
        this.initUserRole()
        this.userDAO.saveAll(listOf(
                User("gabrigiunchi", SHA256PasswordEncoder().encode("aaaa"),
                        "Gabriele", "Giunchi", "gabriele.giunchi1994@gmail.com", mutableListOf(roles[0])),

                User("baseuser", SHA256PasswordEncoder().encode("bbbb"), "User",
                        "Anonimo", "prova@gmail.com", mutableListOf(roles[1])))
        )

        this.logger.info("Init users")
    }

    fun initGyms() {
        this.gyms = this.gymDAO.saveAll(listOf(
                Gym("gym1", "Via1", this.cities[0]),
                Gym("gym2", "Via2", this.cities[0]),
                Gym("gym3", "Via3", this.cities[1]),
                Gym("gym4", "Via4", this.cities[2]))).toList()

        this.logger.info("Init gyms")
    }

    fun initAssetKinds() {
        this.assetKindDAO.saveAll(AssetKindEnum.values().map { AssetKind(it, this.maxReservationTimes[it] ?: 20) })
        this.logger.info("Init asset kinds")
    }

    fun initAssets() {
        this.assetDAO.saveAll(listOf(
                Asset("tr01", this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get(), gyms[0]),
                Asset("c01", this.assetKindDAO.findByName(AssetKindEnum.CICLETTE.name).get(), gyms[0]),
                Asset("p01", this.assetKindDAO.findByName(AssetKindEnum.PANCA.name).get(), gyms[1]),
                Asset("tr01", this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get(), gyms[2])
        ))
        this.logger.info("Init assets")
    }

    fun initCities() {
        this.cities = this.cityDAO.saveAll(CityEnum.values().map { City(it) }).toList()
        this.logger.info("Init cities")
    }

    fun initTimetables() {
        this.gyms.forEach { this.timetableDAO.save(Timetable(it, this.openings, emptySet(), emptySet(), Constants.holidays)) }
        this.logger.info("Init timetables")
    }

    private val openings: Set<Schedule>
        get() = DayOfWeek.values().map { Schedule(it, this.timeIntervals) }.toSet()

    private val timeIntervals: Set<TimeInterval>
        get() = setOf(TimeInterval("07:00+00:00", "20:00+00:00"))
}