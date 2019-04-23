package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.AssetKind
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/asset_kinds")
class AssetKindController {

    private val logger = LoggerFactory.getLogger(AssetKindController::class.java)

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @GetMapping
    fun getAssetKinds(): ResponseEntity<Iterable<AssetKind>> {
        this.logger.info("GET all asset kinds")
        return ResponseEntity(this.assetKindDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAssetKindByInd(@PathVariable id: Int): ResponseEntity<AssetKind> {
        this.logger.info("GET asset kind #$id")
        return this.assetKindDAO.findById(id)
                .map { kind -> ResponseEntity(kind, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @PostMapping
    fun createAssetKind(@Valid @RequestBody assetKind: AssetKind): ResponseEntity<AssetKind> {
        this.logger.info("CREATE asset kind: ${assetKind.name}")
        if (this.assetKindDAO.findById(assetKind.id).isPresent || this.assetKindDAO.findByName(assetKind.name).isPresent) {
            throw ResourceAlreadyExistsException(assetKind.id)
        }

        return ResponseEntity(this.assetKindDAO.save(assetKind), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateAssetkind(@Valid @RequestBody assetKind: AssetKind, @PathVariable id: Int): ResponseEntity<AssetKind> {
        this.logger.info("PUT asset kind: ${assetKind.name}")
        return this.assetKindDAO.findById(id)
                .map { ResponseEntity(this.assetKindDAO.save(assetKind), HttpStatus.OK) }
                .orElseThrow {
                    throw ResourceNotFoundException(assetKind.id)
                }
    }

    @PatchMapping("/{id}/max_time/{time}")
    fun updateAssetKindMaxReservationTime(@PathVariable id: Int, @PathVariable time: Int): ResponseEntity<AssetKind> {
        this.logger.info("PATCH max reservation time of asset kind #$id")

        if (time <= 0) {
            throw BadRequestException("time must be positive")
        }

        return this.assetKindDAO.findById(id)
                .map {
                    it.maxReservationTime = time
                    ResponseEntity(this.assetKindDAO.save(it), HttpStatus.OK)
                }
                .orElseThrow { ResourceNotFoundException("asset kind #$id does not exist") }
    }

    @DeleteMapping("/{id}")
    fun deleteAssetKind(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE asset kind #$id")

        if (this.assetKindDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(id)
        }

        this.assetKindDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}