package com.gabrigiunchi.backendtesi.model.dto.output

import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.AssetKind

data class AssetDTOOutput(
        val id: Int,
        val name: String,
        val gymId: Int,
        val gymName: String,
        val kind: AssetKind) {

    constructor(asset: Asset) : this(asset.id, asset.name, asset.gym.id, asset.gym.name, asset.kind)
}