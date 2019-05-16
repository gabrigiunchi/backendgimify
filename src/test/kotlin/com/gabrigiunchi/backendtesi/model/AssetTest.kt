package com.gabrigiunchi.backendtesi.model

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.model.type.CityEnum
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class AssetTest : AbstractControllerTest() {

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

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
        val asset = Asset("asset1", this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20)), gym)
        this.assetDAO.save(asset).id
        this.gymDAO.delete(gym)
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.assetKindDAO.count()).isEqualTo(1)
    }

    @Test
    fun `Should delete an asset kind and all of its related assets`() {
        val gym = this.mockGym()
        val assetKind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE, 20))
        val asset = Asset("asset1", assetKind, gym)
        this.assetDAO.save(asset).id
        this.assetKindDAO.delete(assetKind)
        Assertions.assertThat(this.assetDAO.count()).isEqualTo(0)
        Assertions.assertThat(this.gymDAO.count()).isEqualTo(1)
        Assertions.assertThat(this.assetKindDAO.count()).isEqualTo(0)
    }

    private fun mockGym(): Gym {
        return this.gymDAO.save(Gym("Gym1", "Via 2", this.cityDAO.save(City(CityEnum.MIAMI))))
    }

}