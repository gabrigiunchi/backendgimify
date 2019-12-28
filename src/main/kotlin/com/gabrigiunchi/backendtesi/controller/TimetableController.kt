package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.TimetableDTO
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.time.Timetable
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/timetables")
class TimetableController(
        private val timetableDAO: TimetableDAO,
        private val gymDAO: GymDAO
) {

    private val logger = LoggerFactory.getLogger(TimetableController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAllTimetables(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<Timetable>> {
        this.logger.info("GET all timetables")
        return ResponseEntity.ok(this.timetableDAO.findAll(PageRequest.of(page, size)))
    }

    @GetMapping("/{id}")
    fun getTimetableById(@PathVariable id: Int): ResponseEntity<Timetable> {
        this.logger.info("GET timetable #$id")
        return this.timetableDAO.findById(id)
                .map { ResponseEntity.ok(it) }
                .orElseThrow { ResourceNotFoundException(Timetable::class.java, id) }
    }

    @GetMapping("/gym/{gymId}")
    fun getTimetableByGym(@PathVariable gymId: Int): ResponseEntity<Timetable> {
        this.logger.info("GET timetable of gym #$gymId")
        return this.gymDAO.findById(gymId)
                .map { gym ->
                    this.timetableDAO.findByGym(gym)
                            .map { timetable -> ResponseEntity.ok(timetable) }
                            .orElseThrow { ResourceNotFoundException("timetable does not exist for gym $gymId") }
                }
                .orElseThrow { ResourceNotFoundException(Gym::class.java, gymId) }
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createTimetable(@Valid @RequestBody timetable: TimetableDTO): ResponseEntity<Timetable> {
        this.logger.info("CREATE timetable")
        val gym = this.gymDAO.findById(timetable.gymId)
                .orElseThrow { ResourceNotFoundException(Gym::class.java, timetable.gymId) }

        return ResponseEntity(this.timetableDAO.save(Timetable(-1, gym, timetable.openings, timetable.closingDays)), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    fun updateTimetable(@PathVariable id: Int, @Valid @RequestBody timetableDTO: TimetableDTO): ResponseEntity<Timetable> {
        this.logger.info("PUT timetable $id")

        if (this.timetableDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(Timetable::class.java, id)
        }

        val gym = this.gymDAO.findById(timetableDTO.gymId)
                .orElseThrow { ResourceNotFoundException(Gym::class.java, timetableDTO.gymId) }

        return ResponseEntity.ok(this.timetableDAO.save(Timetable(id, gym, timetableDTO.openings, timetableDTO.closingDays)))
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteTimetableById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE timetable #$id")
        val timetable = this.timetableDAO.findById(id)
                .orElseThrow { ResourceNotFoundException(Timetable::class.java, id) }
        this.timetableDAO.delete(timetable)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/gym/{gymId}")
    fun deleteTimetableByGymId(@PathVariable gymId: Int): ResponseEntity<Void> {
        this.logger.info("DELETE timetable of gym #$gymId")
        val gym = this.gymDAO.findById(gymId)
                .orElseThrow { ResourceNotFoundException(Gym::class.java, gymId) }
        val timetable = this.timetableDAO.findByGym(gym).orElseThrow { ResourceNotFoundException("no timetable for gym $gymId") }
        this.timetableDAO.delete(timetable)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}