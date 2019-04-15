package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.service.AssetService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/assets")
class AssetController {

    private val logger = LoggerFactory.getLogger(AssetKindController::class.java)

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var assetService: AssetService

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

