package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.model.entities.AssetKind
import com.gabrigiunchi.backendtesi.model.type.AssetKindEnum
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AssetKindControllerTest : BaseTest()
{

    @Autowired
    private lateinit var assetKindDAO: AssetKindDAO

    @Before
    fun clearDB()
    {
        this.assetKindDAO.deleteAll()
    }

    @Test
    fun `Should get all asset kinds`() {
        val enums = AssetKindEnum.values()
        this.assetKindDAO.saveAll(enums.map { AssetKind(it, 20) })
        this.mockMvc.perform(MockMvcRequestBuilders.get(ApiUrls.ASSET_KIND)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(enums.size)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an asset kind by its id`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create an asset kind`() {
        val kind = AssetKind("nuovo assetkind", 20)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSET_KIND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(kind)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(kind.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update an asset kind`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val updatedKind = AssetKind(kind.id, AssetKindEnum.CICLE, 30)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSET_KIND}/${kind.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updatedKind)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(updatedKind.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maxReservationTime", Matchers.`is`(updatedKind.maxReservationTime)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update an asset kind if it does not exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        val updatedKind = AssetKind(kind.id, AssetKindEnum.TREADMILLS, 20)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSET_KIND}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updatedKind)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset kind if its id already exist`() {
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSET_KIND)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(kind)))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset kind if its name already exist`() {
        val kind = AssetKind("nuovo kind", 20)
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
        val kind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.ASSET_KIND}/${kind.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not delete an asset kind if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.delete("${ApiUrls.ASSET_KIND}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update max time reservation for an asset kind`() {
        val assetKind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CRUNCH_BENCH, 20))
        mockMvc.perform(MockMvcRequestBuilders.patch("${ApiUrls.ASSET_KIND}/${assetKind.id}/maxTime/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(assetKind.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`("Crunch bench")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maxReservationTime", Matchers.`is`(10)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update max time reservation for an asset kind if it does not exist`() {
        mockMvc.perform(MockMvcRequestBuilders.patch("${ApiUrls.ASSET_KIND}/-1/maxTime/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update max time reservation for an asset kind if the time is negative`() {
        val assetKind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        mockMvc.perform(MockMvcRequestBuilders.patch("${ApiUrls.ASSET_KIND}/${assetKind.id}/maxTime/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update max time reservation for an asset kind if the time is 0`() {
        val assetKind = this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        this.assetKindDAO.save(AssetKind(AssetKindEnum.CICLE, 20))
        mockMvc.perform(MockMvcRequestBuilders.patch("${ApiUrls.ASSET_KIND}/${assetKind.id}/maxTime/0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
    }
}