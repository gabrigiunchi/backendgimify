package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.model.Asset
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AssetControllerTest : AbstractControllerTest() {

    @Autowired
    private lateinit var assetDAO: AssetDAO

    @Autowired
    private lateinit var gymDAO: GymDAO

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Test
    fun `Should get all assets`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.greaterThanOrEqualTo(4)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an asset by its id`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = this.assetDAO.save(Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first()))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/${asset.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(asset.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an asset if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create an asset`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first())
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(asset.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update an asset`() {
        this.assetDAO.deleteAll()
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first())
        val savedAsset = this.assetDAO.save(asset)
        asset.name = "newName"
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSETS}/${savedAsset.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(asset.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update an asset if it does not exist`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first())
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSETS}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset if its id already exist`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = this.assetDAO.save(Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first()))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset if its name already exists inside the gym`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = this.assetDAO.save(Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first()))
        this.assetDAO.save(asset)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }



    @Test
    fun `Should delete an asset`() {
        val kind = this.assetKindDAO.findByName(AssetKindEnum.TAPIS_ROULANT.name).get()
        val asset = Asset(-1, "ciclette2", kind, this.gymDAO.findAll().first())
        val savedId = this.assetDAO.save(asset).id
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.ASSETS}/$savedId")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete an asset if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.ASSETS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}
