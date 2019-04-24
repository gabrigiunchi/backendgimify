package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.service.AssetService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    @GetMapping
    fun getAssets(): ResponseEntity<Iterable<Asset>> {
        this.logger.info("GET all assets")
        return ResponseEntity(this.assetDAO.findAll(), HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAssetByInd(@PathVariable id: Int): ResponseEntity<Asset> {
        this.logger.info("GET asset #$id")
        return this.assetDAO.findById(id)
                .map { kind -> ResponseEntity(kind, HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException(id) }
    }

    @GetMapping("/by_gym/{gymId}")
    fun getAssetsByGym(@PathVariable gymId: Int): ResponseEntity<Collection<Asset>> {
        this.logger.info("GET all assets in gym $gymId")
        return this.gymDAO.findById(gymId)
                .map { ResponseEntity(this.assetDAO.findByGym(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
    }

    @GetMapping("/by_gym/{gymId}/by_kind/{kindId}")
    fun getAssetsByGymAndKind(@PathVariable gymId: Int, @PathVariable kindId: Int): ResponseEntity<Collection<Asset>> {
        this.logger.info("GET all assets in gym $gymId of kind $kindId")

        if (this.assetKindDAO.findById(kindId).isEmpty) {
            throw ResourceNotFoundException("asset kind $kindId does not exist")
        }

        if (this.gymDAO.findById(gymId).isEmpty) {
            throw ResourceNotFoundException("gym $gymId does not exist")
        }

        return ResponseEntity(
                this.assetDAO.findByGymAndKind(this.gymDAO.findById(gymId).get(), this.assetKindDAO.findById(kindId).get()),
                HttpStatus.OK)
    }

    @GetMapping("/by_kind/{kindId}")
    fun getAssetsByKind(@PathVariable kindId: Int): ResponseEntity<Collection<Asset>> {
        this.logger.info("GET all assets of kind $kindId")
        return this.assetKindDAO.findById(kindId)
                .map { ResponseEntity(this.assetDAO.findByKind(it), HttpStatus.OK) }
                .orElseThrow { ResourceNotFoundException("kind $kindId does not exist") }
    }

    @PostMapping
    fun createAsset(@Valid @RequestBody asset: Asset): ResponseEntity<Asset> {
        this.logger.info("CREATE asset")
        return ResponseEntity(this.assetService.createAsset(asset), HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updateAsset(@Valid @RequestBody asset: Asset, @PathVariable id: Int): ResponseEntity<Asset> {
        this.logger.info("PUT asset #${asset.id}")
        return ResponseEntity(assetService.updateAsset(asset, id), HttpStatus.OK)
    }

    @DeleteMapping("/{id}")
    fun deleteAsset(@PathVariable id: Int): ResponseEntity<Void> {
        this.logger.info("DELETE asset #$id")

        if (this.assetDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(id)
        }

        this.assetDAO.deleteById(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}

