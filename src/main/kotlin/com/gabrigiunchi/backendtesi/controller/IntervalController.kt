package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.IntervalDAO
import com.gabrigiunchi.backendtesi.model.Interval
import com.gabrigiunchi.backendtesi.model.dto.IntervalDTO
import org.slf4j.LoggerFactory
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/intervals")
class IntervalController(private val intervalDAO: IntervalDAO) {

    private val logger = LoggerFactory.getLogger(IntervalController::class.java)

    @GetMapping
    fun getAllIntervals(): ResponseEntity<Iterable<Interval>> {
        this.logger.info("GET all intervals")
        return ResponseEntity(this.intervalDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun findIntervalById(@PathVariable id: Int): ResponseEntity<Interval> {
        this.logger.info("GET interval #$id")
        return ResponseEntity(
                this.intervalDAO.findById(id).map { it }.orElseThrow { ResourceNotFoundException("interval #$id not found") },
                HttpStatus.OK)
    }

    @PostMapping
    fun createInterval(@Valid @RequestBody intervalDTO: IntervalDTO): ResponseEntity<Interval> {
        this.logger.info("POST interval")
        return ResponseEntity(this.intervalDAO.save(Interval(intervalDTO)), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteInterval(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE interval #$id")

        if (this.intervalDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("interval #$id not found")
        }

        this.intervalDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}