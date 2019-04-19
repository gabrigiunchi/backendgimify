package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.DateTimeIntervalDAO
import com.gabrigiunchi.backendtesi.model.DateTimeInterval
import com.gabrigiunchi.backendtesi.model.dto.DateTimeIntervalDTO
import org.slf4j.LoggerFactory
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/date_intervals")
class DateIntervalController(private val dateTimeIntervalDAO: DateTimeIntervalDAO) {

    private val logger = LoggerFactory.getLogger(DateIntervalController::class.java)

    @GetMapping
    fun getAllDateIntervals(): ResponseEntity<Iterable<DateTimeInterval>> {
        this.logger.info("GET all date intervals")
        return ResponseEntity(this.dateTimeIntervalDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findDateIntervalById(@PathVariable id: Int): ResponseEntity<DateTimeInterval> {
        this.logger.info("GET date interval #$id")
        return ResponseEntity(
                this.dateTimeIntervalDAO.findById(id).map { it }.orElseThrow { ResourceNotFoundException("date interval #$id not found") },
                HttpStatus.OK)
    }

    @PostMapping
    fun createDateInterval(@Valid @RequestBody intervalDTO: DateTimeIntervalDTO): ResponseEntity<DateTimeInterval> {
        this.logger.info("POST date interval")
        return ResponseEntity(this.dateTimeIntervalDAO.save(DateTimeInterval(intervalDTO)), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteDateInterval(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE date interval #$id")

        if (this.dateTimeIntervalDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("date interval #$id not found")
        }

        this.dateTimeIntervalDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}