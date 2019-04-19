package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.DateIntervalDAO
import com.gabrigiunchi.backendtesi.model.DateInterval
import com.gabrigiunchi.backendtesi.model.dto.DateIntervalDTO
import org.slf4j.LoggerFactory
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/date_intervals")
class DateIntervalController(private val dateIntervalDAO: DateIntervalDAO) {

    private val logger = LoggerFactory.getLogger(DateIntervalController::class.java)

    @GetMapping
    fun getAllDateIntervals(): ResponseEntity<Iterable<DateInterval>> {
        this.logger.info("GET all date timeIntervals")
        return ResponseEntity(this.dateIntervalDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findDateIntervalById(@PathVariable id: Int): ResponseEntity<DateInterval> {
        this.logger.info("GET date interval #$id")
        return ResponseEntity(
                this.dateIntervalDAO.findById(id).map { it }.orElseThrow { ResourceNotFoundException("date interval #$id not found") },
                HttpStatus.OK)
    }

    @PostMapping
    fun createDateInterval(@Valid @RequestBody intervalDTO: DateIntervalDTO): ResponseEntity<DateInterval> {
        this.logger.info("POST date interval")
        return ResponseEntity(this.dateIntervalDAO.save(DateInterval(intervalDTO)), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteDateInterval(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE date interval #$id")

        if (this.dateIntervalDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("date interval #$id not found")
        }

        this.dateIntervalDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}