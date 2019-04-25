package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.ScheduleDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Schedule
import com.gabrigiunchi.backendtesi.model.dto.input.ScheduleDTO
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/schedules")
class ScheduleController(private val scheduleDAO: ScheduleDAO) {

    private val logger = LoggerFactory.getLogger(ScheduleController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAllSchedules(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Iterable<Schedule>> {
        this.logger.info("GET all schedules, page=$page size=$size")
        return ResponseEntity(this.scheduleDAO.findAll(PageRequest.of(page, size)), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findScheduleById(@PathVariable id: Int): ResponseEntity<Schedule> {
        this.logger.info("GET schedule #$id")
        return ResponseEntity(
                this.scheduleDAO.findById(id).map { it }.orElseThrow { ResourceNotFoundException("schedule $id does not exist") },
                HttpStatus.OK)
    }

    @PostMapping
    fun createSchedule(@Valid @RequestBody scheduleDTO: ScheduleDTO): ResponseEntity<Schedule> {
        this.logger.info("POST schedule")
        return ResponseEntity(this.scheduleDAO.save(Schedule(scheduleDTO)), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteSchedule(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE schedule #$id")

        if (this.scheduleDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("schedule $id does not exist")
        }

        this.scheduleDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}