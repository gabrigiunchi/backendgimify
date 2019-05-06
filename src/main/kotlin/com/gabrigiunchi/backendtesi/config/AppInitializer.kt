package com.gabrigiunchi.backendtesi.config

import com.gabrigiunchi.backendtesi.config.security.SHA256PasswordEncoder
import com.gabrigiunchi.backendtesi.constants.Constants
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import com.gabrigiunchi.backendtesi.util.DateDecorator
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.util.*

@Service
class AppInitializer {

    private val logger = LoggerFactory.getLogger(AppInitializer::class.java)

    @Autowired
    private lateinit var userDAO: UserDAO

    @Autowired
    private lateinit var userRoleDAO: UserRoleDAO

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var commentDAO: CommentDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Autowired
    private lateinit var timetableDAO: TimetableDAO

    @Autowired
    private lateinit var userFactory: UserFactory

    @Value("\${application.zoneId}")
    private var zoneId: String = "UTC"

    @Value("\${application.initDB}")
    private var initDB = false

    private var cities = listOf<City>()
    private var gyms = listOf<Gym>()
    private var roles = listOf<UserRole>()
    private var randomUsers = listOf<User>()
    private val randomComments = listOf(
            listOf("The best gym in the city!", "Clean, lots of assets and very nice people. You must stop by", 5),
            listOf("OK", "The experience was overall good", 3),
            listOf("Good gym", "I like going to this gym because it is always empty, you can train without annoying noises", 4),
            listOf("Awful", "Too crowded, the personnel was not nice", 1),
            listOf("Incredible", "Nothing to say, keep up the good work!", 5),
            listOf("Very nice gym", "I always go to this gym and could not think of anything nicer", 5)
    )

    private val maxReservationTimes = mapOf(
            Pair(AssetKindEnum.CICLETTE, 60),
            Pair(AssetKindEnum.PANCA, 20),
            Pair(AssetKindEnum.PRESSA, 20),
            Pair(AssetKindEnum.TAPIS_ROULANT, 60))

    fun initApp() {
        this.initTimezone()

        if (this.initDB) {
            this.initDB()
        } else {
            this.initUsers()
        }
    }

    private fun initTimezone() {
        this.logger.info("Setting timezone to $zoneId")
        DateDecorator.DEFAULT_TIMEZONE = this.zoneId
    }

    private fun initDB() {
        this.logger.info("Initializing DB")
        this.initCities()
        this.initUsers()
        this.initGyms()
        this.initAssetKinds()
        this.initAssets()
        this.initTimetables()
        this.initComments()
        this.logger.info("DB initialized")
    }

    private fun initUserRole() {
        this.roles = this.userRoleDAO.saveAll(UserRoleEnum.values().map { value -> UserRole(-1, value.toString()) }).toList()
        this.logger.info("Init user roles")
    }

    private fun initUsers() {
        this.initUserRole()
        this.userDAO.saveAll(listOf(
                User("gabrigiunchi", SHA256PasswordEncoder().encode("aaaa"),
                        "Gabriele", "Giunchi", "gabriele.giunchi1994@gmail.com", mutableListOf(roles[0])),

                User("baseuser", SHA256PasswordEncoder().encode("bbbb"), "User",
                        "Anonimo", "prova@gmail.com", mutableListOf(roles[1])))
        )
        this.randomUsers = this.userDAO.saveAll(
                (0..20).map {
                    this.userFactory.createRegularUser(this.randomUsername(), "password", "Name", "Surname")
                }
        ).toList()

        this.logger.info("Init users")
    }

    private fun initGyms() {
        this.gyms = this.gymDAO.saveAll(listOf(
                Gym("gym1", "Via1", this.cities[0], this.zoneId),
                Gym("gym2", "Via2", this.cities[0], this.zoneId),
                Gym("gym3", "Via3", this.cities[1], this.zoneId),
                Gym("gym4", "Via4", this.cities[2], this.zoneId))).toList()

        this.logger.info("Init gyms")
    }

    private fun initAssetKinds() {
        this.assetKindDAO.saveAll(AssetKindEnum.values().map { AssetKind(it, this.maxReservationTimes[it] ?: 20) })
        this.logger.info("Init asset kinds")
    }

    private fun initAssets() {
        val kinds = this.assetKindDAO.findAll()
        val random = Random()
        val assets: List<Asset> = kinds
                .flatMap { kind ->
                    this.gyms.flatMap { gym ->
                        (1..4).map {
                            val name = "${kind.name.substring(0, 2)}${gym.name.reversed().substring(0, 2)}$it${random.nextInt(100)}"
                            Asset(name, kind, gym)
                        }
                    }
                }

        this.assetDAO.saveAll(assets)
        this.logger.info("Init assets")
    }

    private fun initCities() {
        this.cities = this.cityDAO.saveAll(CityEnum.values().map { City(it) }).toList()
        this.logger.info("Init cities")
    }

    private fun initTimetables() {
        this.gyms.forEach { this.timetableDAO.save(Timetable(it, this.openings, emptySet(), emptySet(), Constants.holidays)) }
        this.logger.info("Init timetables")
    }

    private fun initComments() {
        this.randomUsers.forEach { user ->
            val comments = this.gyms.map { gym ->
                val random = this.randomComments[Random().nextInt(this.randomComments.size)]
                Comment(user, gym, random[0] as String, random[1] as String, random[2] as Int)
            }
            this.commentDAO.saveAll(comments)
        }
        this.logger.info("Init comments")
    }

    private val openings: Set<Schedule>
        get() = DayOfWeek.values().map { Schedule(it, this.timeIntervals) }.toSet()

    private val timeIntervals: Set<TimeInterval>
        get() = setOf(TimeInterval("09:00", "21:00", this.zoneId))

    private fun randomUsername(): String = "user${Random().nextInt(100000)}"
}