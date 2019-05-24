package com.gabrigiunchi.backendtesi.config

import com.gabrigiunchi.backendtesi.constants.Constants
import com.gabrigiunchi.backendtesi.dao.*
import com.gabrigiunchi.backendtesi.model.*
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import com.gabrigiunchi.backendtesi.model.type.RepetitionType
import com.gabrigiunchi.backendtesi.model.type.UserRoleEnum
import com.gabrigiunchi.backendtesi.util.DateDecorator
import com.gabrigiunchi.backendtesi.util.UserFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
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
    private lateinit var gymImageDAO: GymImageDAO

    @Autowired
    private lateinit var userFactory: UserFactory

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
            listOf("Very nice gym", "I always go to this gym and could not think of anything nicer", 5),
            listOf("Incompetent trainer", "The trainer was distracted and thinking about his business instead of caring for us", 1),
            listOf("Disgusting", "The locker rooms smell and aren't cleaned properly. Not coming back", 1),
            listOf("Very nice staff", "The trainers are so nice and well prepared! They always give good advice and it's so uplifting to come here. Keep up the good work!", 5),
            listOf("Perfect", "The cutting edge equipments and the expertise of the trainers make this gym one of the best I've ever seen. Simply another level", 5),
            listOf("Very good for aerobics", "This beautiful gym features the latest exercising equipments and offers spinning classes and aerobics classes", 4)
    )

    private val maxReservationTimes = mapOf(
            Pair(AssetKindEnum.CICLE, 60),
            Pair(AssetKindEnum.CRUNCH_BENCH, 20),
            Pair(AssetKindEnum.BENCH, 20),
            Pair(AssetKindEnum.TREADMILLS, 60))

    fun initApp() {
        if (this.initDB) {
            this.initDB()
        } else {
            this.initUsers()
        }
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
        this.initImages()
        this.logger.info("DB initialized")
    }

    private fun initUserRole() {
        this.logger.info("Init user roles")
        this.roles = this.userRoleDAO.saveAll(UserRoleEnum.values().map { value -> UserRole(-1, value.toString()) }).toList()
    }

    private fun initUsers() {
        this.logger.info("Init users")
        this.initUserRole()
        this.userDAO.saveAll(listOf(
                this.userFactory.createAdminUser("gabrigiunchi", "aaaa", "Gabriele", "Giunchi", "gabriele.giunchi1994@gmail.com"),
                this.userFactory.createAdminUser("tonan", "pinguino", "Antonella", "Tondi", "giutondi@alice.it"),
                this.userFactory.createRegularUser("baseuser", "bbbb", "User", "Anonimo", "prova@gmail.com"))
        )
        this.randomUsers = this.userDAO.saveAll(
                (1..15).map {
                    this.userFactory.createRegularUser("user$it", "password", "Name", "Surname")
                }
        ).toList()
    }

    private fun initGyms() {
        this.logger.info("Init gyms")
        this.gyms = this.gymDAO.saveAll(listOf(
                Gym("Ultimate Fitness", "136 Madison Ave", this.cities[0], 40.745766, -73.984540),
                Gym("The Wild Mustang", "10698 S Van Ness Ave", this.cities[1], 33.939874, -118.317831),
                Gym("Levitate Yoga", "1135 Dorchester Ave", this.cities[2], 42.311925, -71.057449),
                Gym("Sleek Physique", "239 14th St NW", this.cities[3], 38.893152, -77.031989),
                Gym("Sage Fitness", "2501 Pine Tree Dr", this.cities[4], 25.802514, -80.129455),
                Gym("Greek God Gym", "766 E 38th St", this.cities[5], 41.825801, -87.609050),
                Gym("Total Fitness", "740 Bacon St", this.cities[6], 37.725906, -122.410281),

                Gym("Yeah Fitness", "12 E 104th St", this.cities[0], 40.792719, -73.951434),
                Gym("Heart & Soul Fitness", "500 1st St", this.cities[1], 33.878970, -118.403720),
                Gym("Elite Body Fitness", "5 Bartlett Pl", this.cities[2], 42.364146, -71.055750),
                Gym("Xpose Gym", "3398-3300 Brothers Pl SE", this.cities[3], 38.842048, -77.004393),
                Gym("Muscle Up", "600 NE 38th St", this.cities[4], 25.812459, -80.186485),
                Gym("Actively Fit", "7271 S Exchange Ave", this.cities[5], 41.762757, -87.563190),
                Gym("The Fitness Institute", "1695 Beach St", this.cities[6], 37.804645, -122.436430),
                Gym("Giunchi Fit", "Via Pacchioni 43", this.cities[7], 44.231921, 12.062291)
        )).toList()
    }

    private fun initAssetKinds() {
        this.logger.info("Init asset kinds")
        this.assetKindDAO.saveAll(AssetKindEnum.values().map { AssetKind(it, this.maxReservationTimes[it] ?: 20) })
    }

    private fun initAssets() {
        this.logger.info("Init assets")
        val kinds = this.assetKindDAO.findAll()
        val random = Random()
        val assets: List<Asset> = kinds
                .flatMap { kind ->
                    this.gyms.flatMap { gym ->
                        (1..4).map {
                            val name = "${kind.name.substring(0, 2)}${gym.name.reversed().substring(0, 2)}$it${random.nextInt(100)}"
                            Asset(name.toLowerCase(), kind, gym)
                        }
                    }
                }

        this.assetDAO.saveAll(assets)
    }

    private fun initCities() {
        this.logger.info("Init cities")
        this.cities = this.cityDAO.saveAll(CityEnum.values().map { City(it) }).toList()
    }

    private fun initTimetables() {
        this.logger.info("Init timetables")
        this.gyms.forEach { this.timetableDAO.save(Timetable(it, this.openings, Constants.holidays)) }
    }

    private fun initComments() {
        this.logger.info("Init comments")
        this.randomUsers.forEach { user ->
            val comments = this.gyms.map { gym ->
                val random = this.randomComments[Random().nextInt(this.randomComments.size)]
                Comment(user, gym, random[0] as String, random[1] as String, random[2] as Int)
            }
            this.commentDAO.saveAll(comments)
        }
    }

    private fun initImages() {
        this.logger.info("Init gym images")
        this.gyms.forEach { gym -> this.gymImageDAO.saveAll(((1..4).map { GymImage(gym, "gym1_$it.jpg") })) }
    }

    private val openings: Set<RepeatedInterval>
        get() = setOf(
                RepeatedInterval("2019-01-07T09:00:00", "2019-01-07T21:00:00", RepetitionType.weekly)
        )
}