package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.AssetDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class AssetService(
        private val assetDAO: AssetDAO,
        private val gymDAO: GymDAO,
        private val assetKindDAO: AssetKindDAO
) {

    fun getAssets(page: Int, size: Int): Page<Asset> = this.assetDAO.findAll(this.pageRequest(page, size))

    fun getAssetById(id: Int): Asset = this.assetDAO.findById(id).orElseThrow { ResourceNotFoundException("asset $id does not exist") }

    fun getAssetsByGym(gymId: Int, page: Int, size: Int): Page<Asset> =
            this.gymDAO.findById(gymId)
                    .map { this.assetDAO.findByGym(it, this.pageRequest(page, size)) }
                    .orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }

    fun getAssetsByKind(kindId: Int, page: Int, size: Int): Page<Asset> =
            this.assetKindDAO.findById(kindId)
                    .map { kind -> this.assetDAO.findByKind(kind, this.pageRequest(page, size)) }
                    .orElseThrow { ResourceNotFoundException("asset kind $kindId does not exist") }

    fun getAssetsByGymAndKind(gymId: Int, kindId: Int): List<Asset> {
        val kind = this.assetKindDAO.findById(kindId).orElseThrow { ResourceNotFoundException("asset kind $kindId does not exist") }
        val gym = this.gymDAO.findById(gymId).orElseThrow { ResourceNotFoundException("gym $gymId does not exist") }
        return this.assetDAO.findByGymAndKind(gym, kind)
    }

    fun createAsset(asset: AssetDTOInput): Asset {
        val kind = this.assetKindDAO.findById(asset.kindId)
                .orElseThrow { ResourceNotFoundException("asset kind ${asset.kindId} does not exist") }

        val gym = this.gymDAO.findById(asset.gymId)
                .orElseThrow { ResourceNotFoundException("gym ${asset.gymId} does not exist") }

        this.checkName(asset.name, gym)
        return this.assetDAO.save(Asset(asset.name, kind, gym))
    }

    fun updateAsset(asset: AssetDTOInput, id: Int): Asset {
        if (this.assetDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException("asset $id does not exist")
        }
        val kind = this.assetKindDAO.findById(asset.kindId)
                .orElseThrow { ResourceNotFoundException("asset kind ${asset.kindId} does not exist") }

        val gym = this.gymDAO.findById(asset.gymId).orElseThrow { ResourceNotFoundException("gym ${asset.gymId} does not exist") }
        this.checkName(asset.name, gym)
        return this.assetDAO.save(Asset(id, asset.name, kind, gym))
    }

    fun deleteAsset(id: Int) {
        val asset = this.assetDAO.findById(id).orElseThrow { ResourceNotFoundException("asset $id does not exist") }
        this.assetDAO.delete(asset)
    }

    /**
     * Check if the name is unique inside the gym
     */
    private fun checkName(name: String, gym: Gym) {
        if (this.assetDAO.findByGymAndName(gym, name).isPresent) {
            throw ResourceAlreadyExistsException("asset with name $name already exists")
        }
    }

    private fun pageRequest(page: Int, size: Int, sort: Sort = Sort.by("name")) = PageRequest.of(page, size, sort)

}