package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.BackendtesiApplication
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.dto.input.AssetDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import javax.transaction.Transactional

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(classes = [BackendtesiApplication::class])
@AutoConfigureMockMvc
@Transactional
class AssetServiceTest {

    @Autowired
    private lateinit var assetService: AssetService

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var cityDAO: CityDAO

    @Before
    fun clearDB() {
        this.gymDAO.deleteAll()
        this.cityDAO.deleteAll()
        this.assetKindDAO.deleteAll()
        this.assetDAO.deleteAll()
    }

    @Test
    fun `Should create an asset`() {
        val asset = AssetDTOInput("ciclette1", this.mockAssetKind().id, this.mockGym().id)
        val savedAsset = this.assetService.createAsset(asset)
        Assertions.assertThat(this.assetDAO.findById(savedAsset.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(savedAsset.id).get().name).isEqualTo(asset.name)
    }

    @Test(expected = ResourceAlreadyExistsException::class)
    fun `Should not create an asset if its name already exists inside the gym`() {
        val kind = this.assetKindDAO.save(MockEntities.assetKinds.first())
        val city = this.cityDAO.save(MockEntities.mockCities.first())
        val gyms = this.gymDAO.saveAll(listOf(
                this.gymDAO.save(Gym("gym1", "address1", city)),
                this.gymDAO.save(Gym("gym2", "address1", city)),
                this.gymDAO.save(Gym("gym3", "address1", city))
        )).toList()

        // Two assets in different gyms can have the same name
        val asset1 = this.assetService.createAsset(AssetDTOInput("ciclette2", kind.id, gyms[0].id))
        val asset2 = this.assetService.createAsset(AssetDTOInput("ciclette2", kind.id, gyms[1].id))

        Assertions.assertThat(this.assetDAO.findById(asset1.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(asset2.id)).isPresent
        this.assetService.createAsset(AssetDTOInput("ciclette2", kind.id, gyms[0].id))
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not create an asset if the gym does not exist`() {
        val kind = this.assetKindDAO.save(MockEntities.assetKinds.first())
        val asset = AssetDTOInput("jnjadas", kind.id, -1)
        this.assetService.createAsset(asset)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not create an asset if the asset kind does not exist`() {
        val asset = AssetDTOInput("jnjadas", -1, this.mockGym().id)
        this.assetService.createAsset(asset)
    }

    /************************************ UPDATE **********************************************************/

    @Test
    fun `Should update an asset`() {
        val asset = this.mockAsset(this.mockGym())
        val newAsset = AssetDTOInput("new name for this asset", asset.kind.id, asset.gym.id)
        this.assetService.updateAsset(newAsset, asset.id)
        Assertions.assertThat(this.assetDAO.findById(asset.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(asset.id).get().name).isEqualTo("new name for this asset")
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not update an asset if its id does not exist`() {
        val asset = this.mockAsset(this.mockGym())
        this.assetService.updateAsset(AssetDTOInput(asset), -1)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not update an asset if the gym not exist`() {
        val asset = this.mockAsset(this.mockGym())
        this.assetService.updateAsset(AssetDTOInput("dashbdhas", asset.kind.id, -1), asset.id)
    }

    @Test(expected = ResourceNotFoundException::class)
    fun `Should not update an asset if the asset kind not exist`() {
        val asset = this.mockAsset(this.mockGym())
        this.assetService.updateAsset(AssetDTOInput("dashbdhas", -1, asset.gym.id), asset.id)
    }

    @Test(expected = ResourceAlreadyExistsException::class)
    fun `Should not update an asset if its name already exists inside the gym`() {
        val kind = this.mockAssetKind()
        val city = this.cityDAO.save(MockEntities.mockCities.first())
        val gyms = this.gymDAO.saveAll(listOf(
                this.gymDAO.save(Gym("gym1", "address1", city)),
                this.gymDAO.save(Gym("gym2", "address1", city)),
                this.gymDAO.save(Gym("gym3", "address1", city))
        )).toList()

        // Two assets in different gyms can have the same name
        val asset1 = this.assetService.createAsset(AssetDTOInput("ciclette2", kind.id, gyms[0].id))
        val asset2 = this.assetService.createAsset(AssetDTOInput("ciclette3", kind.id, gyms[0].id))

        Assertions.assertThat(this.assetDAO.findById(asset1.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(asset2.id)).isPresent
        this.assetService.updateAsset(AssetDTOInput("ciclette3", kind.id, gyms[0].id), asset1.id)
    }

    private fun mockAssetKind() = this.assetKindDAO.save(MockEntities.assetKinds.first())

    private fun mockGym(): Gym {
        val city = this.cityDAO.save(MockEntities.mockCities[0])
        return this.gymDAO.save(Gym("gym1", "address1", city))
    }

    private fun mockAsset(gym: Gym) = this.assetDAO.save(Asset("ciclette2", this.mockAssetKind(), gym))
}