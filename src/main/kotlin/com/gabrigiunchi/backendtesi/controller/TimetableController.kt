package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.model.Timetable
import com.gabrigiunchi.backendtesi.model.dto.TimetableDTO
import org.slf4j.LoggerFactory
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/timetables")
class TimetableController(
        private val timetableDAO: TimetableDAO,
        private val gymDAO: GymDAO
) {

    private val logger = LoggerFactory.getLogger(TimetableController::class.java)

    @GetMapping
    fun getAllTimetables(): ResponseEntity<Iterable<Timetable>> {
        this.logger.info("GET all timetables")
        return ResponseEntity(this.timetableDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getTimetableById(@PathVariable id: Int): ResponseEntity<Timetable> {
        this.logger.info("GET timetable #$id")
        return this.timetableDAO.findById(id)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("timetable #$id does not exist") }
    }

    @GetMapping("/by_gym/{gymId}")
    fun getTimetableByGym(@PathVariable gymId: Int): ResponseEntity<Timetable> {
        this.logger.info("GET timetable of gym #$gymId")
        return this.gymDAO.findById(gymId)
                .map {
                    this.timetableDAO.findByGym(it)
                            .map { ResponseEntity(it, HttpStatus.OK) }
                            .orElseThrow { ResourceNotFoundException("timetable does not exist for gym #$gymId") }
                }
                .orElseThrow { ResourceNotFoundException("gym #$gymId does not exist") }
    }

    @PostMapping
    fun createTimetable(@Valid @RequestBody timetable: TimetableDTO): ResponseEntity<Timetable> {
        this.logger.info("CREATE timetable")

        if (this.gymDAO.findById(timetable.gymId).isEmpty) {
            throw ResourceNotFoundException("gym #${timetable.gymId} does not exist")
        }

        val gym = this.gymDAO.findById(timetable.gymId).get()

        return ResponseEntity(this.timetableDAO.save(Timetable(-1, timetable, gym)), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateTimetable(@PathVariable id: Int, @Valid @RequestBody timetableDTO: TimetableDTO): ResponseEntity<Timetable> {
        this.logger.info("PUT timetable $id")

        if (this.timetableDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("timetable $id does not exist")
        }

        if (this.gymDAO.findById(timetableDTO.gymId).isEmpty) {
            throw ResourceNotFoundException("gym #${timetableDTO.gymId} does not exist")
        }

        return ResponseEntity(this.timetableDAO.save(Timetable(id, timetableDTO, this.gymDAO.findById(timetableDTO.gymId).get())),
                HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteTimetableById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE timetable #$id")

        if (this.timetableDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("timetable #$id does not exist")
        }

        this.timetableDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    @DeleteMapping("/by_gym/{gymId}")
    fun deleteTimetableByGymId(@PathVariable gymId: Int): ResponseEntity<Void> {
        this.logger.info("DELETE timetable of gym #$gymId")

        if (this.gymDAO.findById(gymId).isEmpty) {
            throw ResourceNotFoundException("gym #$gymId does not exist")
        }

        val timetable = this.timetableDAO.findByGym(this.gymDAO.findById(gymId).get())

        if (timetable.isEmpty) {
            throw ResourceNotFoundException("no timetable for gym #$gymId")
        }

        this.timetableDAO.delete(timetable.get())
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}