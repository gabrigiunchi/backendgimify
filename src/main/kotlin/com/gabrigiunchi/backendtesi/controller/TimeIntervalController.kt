package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.TimeIntervalDAO
import com.gabrigiunchi.backendtesi.model.TimeInterval
import com.gabrigiunchi.backendtesi.model.dto.TimeIntervalDTO
import org.slf4j.LoggerFactory
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/timeIntervals")
class TimeIntervalController(private val timeIntervalDAO: TimeIntervalDAO) {

    private val logger = LoggerFactory.getLogger(TimeIntervalController::class.java)

    @GetMapping
    fun getAllIntervals(): ResponseEntity<Iterable<TimeInterval>> {
        this.logger.info("GET all timeIntervals")
        return ResponseEntity(this.timeIntervalDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findIntervalById(@PathVariable id: Int): ResponseEntity<TimeInterval> {
        this.logger.info("GET interval #$id")
        return ResponseEntity(
                this.timeIntervalDAO.findById(id).map { it }.orElseThrow { ResourceNotFoundException("interval #$id not found") },
                HttpStatus.OK)
    }

    @PostMapping
    fun createInterval(@Valid @RequestBody timeIntervalDTO: TimeIntervalDTO): ResponseEntity<TimeInterval> {
        this.logger.info("POST interval")
        return ResponseEntity(this.timeIntervalDAO.save(TimeInterval(timeIntervalDTO)), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteInterval(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE interval #$id")

        if (this.timeIntervalDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("interval #$id not found")
        }

        this.timeIntervalDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}