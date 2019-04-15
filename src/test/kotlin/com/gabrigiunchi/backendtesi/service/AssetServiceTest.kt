package com.gabrigiunchi.backendtesi.service

import com.gabrigiunchi.backendtesi.BackendtesiApplication
import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.exceptions.ResourceAlreadyExistsException
import com.gabrigiunchi.backendtesi.exceptions.ResourceNotFoundException
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import org.assertj.core.api.Assertions
import org.junit.Assert
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

    @Before
    fun clearDB() {
        this.assetDAO.deleteAll()
    }

    @Test
    fun `Should create an asset`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = Asset(-1, "ciclette1", kind, this.gymDAO.findAll().first())
        val savedAsset = this.assetService.createAsset(asset)
        Assertions.assertThat(this.assetDAO.findById(savedAsset.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(savedAsset.id).get().name).isEqualTo(asset.name)
    }

    @Test
    fun `Should not create an asset if its id already exists`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = this.assetDAO.save(Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first()))
        this.assetDAO.save(asset)
        try {
            this.assetService.createAsset(asset)
            Assert.fail()
        } catch (e: ResourceAlreadyExistsException) {
            // OK
        } catch (e: Exception) {
            Assert.fail()
        }
    }

    @Test
    fun `Should not create an asset if its name already exists inside the gym`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val gyms = this.gymDAO.findAll().toList()

        // Two assets in different gyms can have the same name
        val asset1 = this.assetService.createAsset(Asset("ciclette2", kind, gyms[0]))
        val asset2 = this.assetService.createAsset(Asset("ciclette2", kind, gyms[1]))

        Assertions.assertThat(this.assetDAO.findById(asset1.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(asset2.id)).isPresent

        try {
            this.assetService.createAsset(Asset("ciclette2", kind, gyms[0]))
            Assert.fail()
        } catch (e: ResourceAlreadyExistsException) {
            // OK
        } catch (e: Exception) {
            Assert.fail()
        }
    }

    /************************************ UPDATE **********************************************************/

    @Test
    fun `Should update an asset`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = this.assetService.createAsset(Asset(-1, "ciclette1", kind, this.gymDAO.findAll().first()))
        val newAsset = Asset(asset.id, "new name for this asset", asset.kind, asset.gym)

        this.assetService.updateAsset(newAsset, asset.id)
        Assertions.assertThat(this.assetDAO.findById(asset.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(asset.id).get().name).isEqualTo("new name for this asset")
    }

    @Test
    fun `Should not update an asset if its id does not exist`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = this.assetDAO.save(Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first()))
        this.assetDAO.save(asset)
        try {
            this.assetService.updateAsset(asset, -1)
            Assert.fail()
        } catch (e: ResourceNotFoundException) {
            // OK
        } catch (e: Exception) {
            Assert.fail()
        }
    }

    @Test
    fun `Should not update an asset if its name already exists inside the gym`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val gyms = this.gymDAO.findAll().toList()

        // Two assets in different gyms can have the same name
        val asset1 = this.assetService.createAsset(Asset("ciclette2", kind, gyms[0]))
        val asset2 = this.assetService.createAsset(Asset("ciclette3", kind, gyms[0]))

        Assertions.assertThat(this.assetDAO.findById(asset1.id)).isPresent
        Assertions.assertThat(this.assetDAO.findById(asset2.id)).isPresent

        try {
            this.assetService.updateAsset(Asset("ciclette3", kind, gyms[0]), asset1.id)
            Assert.fail()
        } catch (e: ResourceAlreadyExistsException) {
            // OK
        } catch (e: Exception) {
            Assert.fail()
        }
    }


}