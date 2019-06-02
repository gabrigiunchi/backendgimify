package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.AssetDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.springframework.stereotype.Service

@Service
class AssetService(
        private val assetDAO: AssetDAO,
        private val gymDAO: GymDAO,
        private val assetKindDAO: AssetKindDAO
) {

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

    /**
     * Check if the name is unique inside the gym
     */
    private fun checkName(name: String, gym: Gym) {
        if (this.assetDAO.findByGymAndName(gym, name).isPresent) {
            throw ResourceAlreadyExistsException("asset with name $name already exists")
        }
    }
}