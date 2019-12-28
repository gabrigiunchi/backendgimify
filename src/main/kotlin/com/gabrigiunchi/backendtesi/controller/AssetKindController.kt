package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.exceptions.BadRequestException
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.entities.AssetKind
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/asset_kinds")
class AssetKindController(
        private val assetKindDAO: AssetKindDAO) {

    private val logger = LoggerFactory.getLogger(AssetKindController::class.java)

    @GetMapping
    fun getAssetKinds(): ResponseEntity<Iterable<AssetKind>> {
        this.logger.info("GET all asset kinds")
        return ResponseEntity.ok(this.assetKindDAO.findAll())
    }

    @GetMapping("/{id}")
    fun getAssetKindByInd(@PathVariable id: Int): ResponseEntity<AssetKind> {
        this.logger.info("GET asset kind #$id")
        return this.assetKindDAO.findById(id)
                .map { ResponseEntity.ok(it) }
                .orElseThrow { ResourceNotFoundException(AssetKind::class.java, id) }
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createAssetKind(@Valid @RequestBody assetKind: AssetKind): ResponseEntity<AssetKind> {
        this.logger.info("CREATE asset kind: ${assetKind.name}")
        if (this.assetKindDAO.findById(assetKind.id).isPresent || this.assetKindDAO.findByName(assetKind.name).isPresent) {
            throw ResourceAlreadyExistsException("asset kind ${assetKind.id} already exists")
        }

        return ResponseEntity(this.assetKindDAO.save(assetKind), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    fun updateAssetkind(@Valid @RequestBody assetKind: AssetKind, @PathVariable id: Int): ResponseEntity<AssetKind> {
        this.logger.info("PUT asset kind: ${assetKind.name}")
        return this.assetKindDAO.findById(id)
                .map { ResponseEntity.ok(this.assetKindDAO.save(assetKind)) }
                .orElseThrow { ResourceNotFoundException(AssetKind::class.java, id) }
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PatchMapping("/{id}/maxTime/{time}")
    fun updateAssetKindMaxReservationTime(@PathVariable id: Int, @PathVariable time: Int): ResponseEntity<AssetKind> {
        this.logger.info("PATCH max reservation time of asset kind #$id")

        if (time <= 0) {
            throw BadRequestException("time must be positive")
        }

        return this.assetKindDAO.findById(id)
                .map {
                    it.maxReservationTime = time
                    ResponseEntity.ok(this.assetKindDAO.save(it))
                }
                .orElseThrow { ResourceNotFoundException(AssetKind::class.java, id) }
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteAssetKind(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE asset kind #$id")

        val kind = this.assetKindDAO.findById(id)
                .orElseThrow { ResourceNotFoundException(AssetKind::class.java, id) }
        this.assetKindDAO.delete(kind)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

}