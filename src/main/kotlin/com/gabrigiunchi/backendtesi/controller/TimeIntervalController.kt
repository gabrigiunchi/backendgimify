package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.TimeIntervalDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.TimeInterval
import com.gabrigiunchi.backendtesi.model.dto.input.TimeIntervalDTO
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/time_intervals")
class TimeIntervalController(private val timeIntervalDAO: TimeIntervalDAO) {

    private val logger = LoggerFactory.getLogger(TimeIntervalController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAllIntervals(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<TimeInterval>> {
        this.logger.info("GET all timeIntervals, page=$page size=$size")
        return ResponseEntity(this.timeIntervalDAO.findAll(PageRequest.of(page, size)), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findIntervalById(@PathVariable id: Int): ResponseEntity<TimeInterval> {
        this.logger.info("GET interval #$id")
        return ResponseEntity(
                this.timeIntervalDAO.findById(id).orElseThrow { ResourceNotFoundException("time interval $id does not exist") },
                HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createInterval(@Valid @RequestBody timeIntervalDTO: TimeIntervalDTO): ResponseEntity<TimeInterval> {
        this.logger.info("POST interval")
        return ResponseEntity(this.timeIntervalDAO.save(TimeInterval(timeIntervalDTO)), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteInterval(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE interval #$id")
        val timeInterval = this.timeIntervalDAO.findById(id).orElseThrow { ResourceNotFoundException("time interval $id does not exist") }
        this.timeIntervalDAO.delete(timeInterval)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}