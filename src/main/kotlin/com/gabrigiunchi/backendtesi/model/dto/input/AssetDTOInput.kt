package com.gabrigiunchi.backendtesi.model.dto.input

import com.gabrigiunchi.backendtesi.model.entities.Asset


class AssetDTOInput(
        val name: String,
        val kindId: Int,
        val gymId: Int) {

    constructor(asset: Asset) : this(asset.name, asset.kind.id, asset.gym.id)
}