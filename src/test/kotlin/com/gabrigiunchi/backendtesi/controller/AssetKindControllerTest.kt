package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.AbstractControllerTest
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.model.AssetKind
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import com.gabrigiunchi.backendtesi.util.ApiUrls
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AssetKindControllerTest: AbstractControllerTest() {

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Test
    fun `Should get all asset kinds`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.ASSET_KIND)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.greaterThan(0)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an asset kind by its id`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSET_KIND}/${kind.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(kind.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an asset kind if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSET_KIND}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create an asset kind`() {
        val kind = AssetKind("nuovo assetkind")
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSET_KIND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(kind)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(kind.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update an asset kind`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE))
        val updatedKind = AssetKind(kind.id, AssetKindEnum.TAPIS_ROULANT)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSET_KIND}/${kind.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updatedKind)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(updatedKind.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update an asset kind if it does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE))
        val updatedKind = AssetKind(kind.id, AssetKindEnum.TAPIS_ROULANT)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSET_KIND}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updatedKind)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset kind if its id already exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSET_KIND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(kind)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset kind if its name already exist`() {
        val kind = AssetKind("nuovo kind")
        val savedKind = this.assetKindDAO.save(kind)
        Assertions.assertThat(kind.id).isNotEqualTo(savedKind.id)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSET_KIND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(kind)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete an asset kind`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLETTE))
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.ASSET_KIND}/${kind.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete an asset kind if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.ASSET_KIND}/100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }
}