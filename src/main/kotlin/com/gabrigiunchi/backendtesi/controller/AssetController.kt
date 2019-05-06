package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.dto.input.AssetDTOInput
import com.gabrigiunchi.backendtesi.model.dto.output.AssetDTOOutput
import com.gabrigiunchi.backendtesi.service.AssetService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/assets")
class AssetController(
        private val assetKindDAO: AssetKindDAO,
        private val assetDAO: AssetDAO,
        private val assetService: AssetService,
        private val gymDAO: GymDAO) {

    private val logger = LoggerFactory.getLogger(AssetController::class.java)

    @GetMapping("/page/{page}/size/{size}")
    fun getAssets(@PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<AssetDTOOutput>> {
        this.logger.info("GET all assets, page=$page size=$size")
        return ResponseEntity(this.assetDAO.findAll(this.pageRequest(page, size)).map { AssetDTOOutput(it) }, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAssetById(@PathVariable id: Int): ResponseEntity<Asset> {
        this.logger.info("GET asset #$id")
        return this.assetDAO.findById(id)
                .map { ResponseEntity(it, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("asset $id does not exist") }
    }

    @GetMapping("/by_gym/{gymId}/page/{page}/size/{size}")
    fun getAssetsByGym(@PathVariable gymId: Int, @PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<AssetDTOOutput>> {
        this.logger.info("GET all assets in gym $gymId, page=$page size=$size")
        return this.gymDAO.findById(gymId)
                .map {
                    ResponseEntity(
                            this.assetDAO.findByGym(it, this.pageRequest(page, size)).map { asset -> AssetDTOOutput(asset) },
                            HttpStatus.OK)
                }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    @GetMapping("/by_gym/{gymId}/by_kind/{kindId}")
    fun getAssetsByGymAndKind(@PathVariable gymId: Int, @PathVariable kindId: Int): ResponseEntity<Collection<AssetDTOOutput>> {
        this.logger.info("GET all assets in gym $gymId of kind $kindId")
        val kind = this.assetKindDAO.findById(kindId).orElseThrow { ResourceNotFoundException("asset kind $kindId does not exist") }
        val gym = this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
        return ResponseEntity(this.assetDAO.findByGymAndKind(gym, kind).map { AssetDTOOutput(it) }, HttpStatus.OK)
    }

    @GetMapping("/by_kind/{kindId}/page/{page}/size/{size}")
    fun getAssetsByKind(@PathVariable kindId: Int, @PathVariable page: Int, @PathVariable size: Int): ResponseEntity<Page<AssetDTOOutput>> {
        this.logger.info("GET all assets of kind $kindId")
        return this.assetKindDAO.findById(kindId)
                .map { kind ->
                    ResponseEntity(
                            this.assetDAO.findByKind(kind, this.pageRequest(page, size)).map { asset -> AssetDTOOutput(asset) },
                            HttpStatus.OK)
                }
                .orElseThrow { ResourceNotFoundException("asset kind $kindId does not exist") }
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
        val asset = this.assetDAO.findById(id).orElseThrow { ResourceNotFoundException("asset $id does not exist") }
        this.assetDAO.delete(asset)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }

    private fun pageRequest(page: Int, size: Int, sort: Sort = Sort.by("name")) = PageRequest.of(page, size, sort)
}

