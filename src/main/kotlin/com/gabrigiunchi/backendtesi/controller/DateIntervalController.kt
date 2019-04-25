package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.DateIntervalDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.DateInterval
import com.gabrigiunchi.backendtesi.model.dto.input.DateIntervalDTO
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/date_intervals")
class DateIntervalController(private val dateIntervalDAO: DateIntervalDAO) {

    private val logger = LoggerFactory.getLogger(DateIntervalController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAllDateIntervals(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Iterable<DateInterval>> {
        this.logger.info("GET all date timeIntervals, page=$page size=$size")
        return ResponseEntity(this.dateIntervalDAO.findAll(PageRequest.of(page, size)), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findDateIntervalById(@PathVariable id: Int): ResponseEntity<DateInterval> {
        this.logger.info("GET date interval #$id")
        return this.dateIntervalDAO.findById(id)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("date interval $id does not exist") }
    }

    @PostMapping
    fun createDateInterval(@Valid @RequestBody intervalDTO: DateIntervalDTO): ResponseEntity<DateInterval> {
        this.logger.info("POST date interval")
        return ResponseEntity(this.dateIntervalDAO.save(DateInterval(intervalDTO)), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteDateInterval(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE date interval #$id")
        val dateInterval = this.dateIntervalDAO.findById(id).orElseThrow { ResourceNotFoundException("date interval $id does not exist") }
        this.dateIntervalDAO.delete(dateInterval)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}