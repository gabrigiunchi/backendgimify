package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.model.dto.input.AssetDTOInput
import com.gabrigiunchi.backendtesi.model.dto.output.AssetDTOOutput
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.service.AssetService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/assets")
class AssetController(private val assetService: AssetService) {

    private val logger = LoggerFactory.getLogger(AssetController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAssets(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<AssetDTOOutput>> {
        this.logger.info("GET all assets, page=$page size=$size")
        return ResponseEntity(this.assetService.getAssets(page, size).map { AssetDTOOutput(it) }, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAssetById(@PathVariable id: Int): ResponseEntity<Asset> {
        this.logger.info("GET asset #$id")
        return ResponseEntity(this.assetService.getAssetById(id), HttpStatus.OK)
    }

    @GetMapping("/by_gym/{gymId}/page/{page}/size/{size}")
    fun getAssetsByGym(@PathVariable gymId: Int, @PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<AssetDTOOutput>> {
        this.logger.info("GET all assets in gym $gymId, page=$page size=$size")
        return ResponseEntity(
                this.assetService.getAssetsByGym(gymId, page, size).map { asset -> AssetDTOOutput(asset) },
                HttpStatus.OK)
    }

    @GetMapping("/by_gym/{gymId}/by_kind/{kindId}")
    fun getAssetsByGymAndKind(@PathVariable gymId: Int, @PathVariable kindId: Int): ResponseEntity<Collection<AssetDTOOutput>> {
        this.logger.info("GET all assets in gym $gymId of kind $kindId")
        return ResponseEntity(
                this.assetService.getAssetsByGymAndKind(gymId, kindId).map { AssetDTOOutput(it) },
                HttpStatus.OK)
    }

    @GetMapping("/by_kind/{kindId}/page/{page}/size/{size}")
    fun getAssetsByKind(@PathVariable kindId: Int, @PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<AssetDTOOutput>> {
        this.logger.info("GET all assets of kind $kindId")
        return ResponseEntity(
                this.assetService.getAssetsByKind(kindId, page, size).map { AssetDTOOutput(it) },
                HttpStatus.OK
        )
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping
    fun createAsset(@Valid @RequestBody asset: AssetDTOInput): ResponseEntity<Asset> {
        this.logger.info("CREATE asset")
        return ResponseEntity(this.assetService.createAsset(asset), HttpStatus.CREATED)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PutMapping("/{id}")
    fun updateAsset(@Valid @RequestBody asset: AssetDTOInput, @PathVariable id: Int): ResponseEntity<Asset> {
        this.logger.info("PUT asset #$id")
        return ResponseEntity(assetService.updateAsset(asset, id), HttpStatus.OK)
    }

    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    fun deleteAsset(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE asset #$id")
        this.assetService.deleteAsset(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

