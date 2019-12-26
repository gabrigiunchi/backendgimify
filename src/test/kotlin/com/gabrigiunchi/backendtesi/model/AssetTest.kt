package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.entities.AssetKind
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class AssetTest : BaseTest() {

    @Before
    fun clearDB() {
        this.cityDAO.deleteAll()
        this.assetDAO.deleteAll()
        this.assetKindDAO.deleteAll()
        this.gymDAO.deleteAll()
    }

    @Test
    fun `Should delete a gym and all of its related assets`() {
        val gym = this.mockGym()
        val asset = Asset("asset1", this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20)), gym)
        this.assetDAO.save(asset).id
        this.gymDAO.delete(gym)
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.assetKindDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should delete an asset kind and all of its related assets`() {
        val gym = this.mockGym()
        val assetKind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val asset = Asset("asset1", assetKind, gym)
        this.assetDAO.save(asset).id
        this.assetKindDAO.delete(assetKind)
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.assetKindDAO.count()).isEqualTo(0)
    }
}