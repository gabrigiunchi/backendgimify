package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AssetService {

    @Autowired
    private lateinit var assetDAO: AssetDAO

    fun createAsset(asset: Asset): Asset {
        if (this.assetDAO.findById(asset.id).isPresent) {
            throw ResourceAlreadyExistsException(asset.id)
        }

        // The name inside the gym should be unique
        val assetsInGym = this.assetDAO.findByGym(asset.gym)
        if (assetsInGym.any { a -> a.name == asset.name }) {
            throw ResourceAlreadyExistsException(asset.name)
        }

        return this.assetDAO.save(asset)
    }

    fun updateAsset(asset: Asset, id: Int): Asset {
        if (this.assetDAO.findById(id).isEmpty) {
            throw ResourceNotFoundException(asset.id)
        }

        // The name inside the gym should be unique
        val assetsInGym = this.assetDAO.findByGym(asset.gym)
        if (assetsInGym.any { a -> a.name == asset.name }) {
            throw ResourceAlreadyExistsException(asset.name)
        }

        return this.assetDAO.save(asset)
    }
}