package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.RegionDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.model.Region
import org.slf4j.LoggerFactory
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/regions")
class RegionController(private val regionDAO: RegionDAO) {

    private val logger = LoggerFactory.getLogger(RegionController::class.java)

    @GetMapping
    fun getAllRegions(): ResponseEntity<Iterable<Region>> {
        this.logger.info("GET all regions")
        return ResponseEntity(this.regionDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getRegionById(@PathVariable id: Int): ResponseEntity<Region> {
        this.logger.info("GET region #$id")
        return this.regionDAO.findById(id)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("region #$id not found") }
    }

    @GetMapping("/by_name/{name}")
    fun getRegionByName(@PathVariable name: String): ResponseEntity<Region> {
        this.logger.info("GET region $name")
        return this.regionDAO.findByName(name)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("region $name not found") }
    }

    @PostMapping
    fun createRegion(@Valid @RequestBody region: Region): ResponseEntity<Region> {
        this.logger.info("CREATE region")

        if (this.regionDAO.findById(region.id).isPresent || this.regionDAO.findByName(region.name).isPresent) {
            throw ResourceAlreadyExistsException("region already exists")
        }

        return ResponseEntity(this.regionDAO.save(region), HttpStatus.CREATED)
    }

    @DeleteMapping("/{id}")
    fun deleteRegionById(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE region #$id")

        if (this.regionDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("region #$id not found")
        }

        this.regionDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}