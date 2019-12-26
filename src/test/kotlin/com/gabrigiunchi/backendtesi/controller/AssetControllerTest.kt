package com.gabrigiunchi.backendtesi.controller

import com.gabrigiunchi.backendtesi.BaseTest
import com.gabrigiunchi.backendtesi.MockEntities
import com.gabrigiunchi.backendtesi.constants.ApiUrls
import com.gabrigiunchi.backendtesi.dao.AssetDAO
import com.gabrigiunchi.backendtesi.dao.AssetKindDAO
import com.gabrigiunchi.backendtesi.dao.CityDAO
import com.gabrigiunchi.backendtesi.dao.GymDAO
import com.gabrigiunchi.backendtesi.model.dto.input.AssetDTOInput
import com.gabrigiunchi.backendtesi.model.entities.Asset
import com.gabrigiunchi.backendtesi.model.entities.AssetKind
import com.gabrigiunchi.backendtesi.model.entities.Gym
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class AssetControllerTest : BaseTest()
{

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
        this.assetDAO.deleteAll()
        this.cityDAO.deleteAll()
        this.assetKindDAO.deleteAll()
        this.gymDAO.deleteAll()
    }

    @Test
    fun `Should get all assets`() {
        val gym = this.createGym()
        val kind = this.assetKindDAO.save(MockEntities.assetKinds[0])
        this.assetDAO.saveAll(listOf(
                Asset("a2", kind, gym),
                Asset("a1", kind, gym),
                Asset("a3", kind, gym),
                Asset("a4", kind, gym)
        ))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[0].name", Matchers.`is`("a1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[0].kind.id", Matchers.`is`(kind.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[0].gymId", Matchers.`is`(gym.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[0].gymName", Matchers.`is`(gym.name)))

                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[1].name", Matchers.`is`("a2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[2].name", Matchers.`is`("a3")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[3].name", Matchers.`is`("a4")))

                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an asset by its id`() {
        val kind = this.assetKindDAO.save(MockEntities.assetKinds[0])
        val asset = this.assetDAO.save(Asset("ciclette2", kind, this.createGym()))
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/${asset.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(asset.name)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.`is`(asset.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kind.id", Matchers.`is`(asset.kind.id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.gym.id", Matchers.`is`(asset.gym.id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an asset by gym`() {
        val kind = this.assetKindDAO.save(MockEntities.assetKinds[0])
        val gym1 = this.createGym()
        val gym2 = this.gymDAO.save(Gym("jdnsadas", "jnjsajkd", gym1.city))
        val assets = this.assetDAO.saveAll(listOf(
                Asset("a1", kind, gym1),
                Asset("a2", kind, gym2),
                Asset("a3", kind, gym1),
                Asset("a4", kind, gym2),
                Asset("a5", kind, gym1)
        )).toList()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/gym/${gym1.id}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[0].id", Matchers.`is`(assets[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[1].id", Matchers.`is`(assets[2].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[2].id", Matchers.`is`(assets[4].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an asset by kind`() {
        val kinds = this.assetKindDAO.saveAll(MockEntities.assetKinds).toList()
        val gym = this.createGym()
        val assets = this.assetDAO.saveAll(listOf(
                Asset("a1", kinds[0], gym),
                Asset("a2", kinds[0], gym),
                Asset("a3", kinds[1], gym),
                Asset("a4", kinds[2], gym),
                Asset("a5", kinds[0], gym)
        )).toList()

        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/kind/${assets[0].kind.id}/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.`is`(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[0].id", Matchers.`is`(assets[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[1].id", Matchers.`is`(assets[1].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.[2].id", Matchers.`is`(assets[4].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should get an asset by gym and kind`() {
        val kinds = this.assetKindDAO.saveAll(MockEntities.assetKinds).toList()
        val gym1 = this.createGym()
        val gym2 = this.gymDAO.save(Gym("gym2", "address", gym1.city))
        val assets = this.assetDAO.saveAll(listOf(
                Asset("a1", kinds[0], gym1),
                Asset("a2", kinds[0], gym1),
                Asset("a3", kinds[1], gym2),
                Asset("a4", kinds[2], gym1),
                Asset("a5", kinds[0], gym2)
        )).toList()

        val url = "${ApiUrls.ASSETS}/gym/${gym1.id}/kind/${assets[0].kind.id}"
        this.mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.`is`(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].id", Matchers.`is`(assets[0].id)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].id", Matchers.`is`(assets[1].id)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an asset if it does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an asset gym if the gym does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/gym/-1/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an asset by kind if the kind does not exist`() {
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/kind/-1/page/0/size/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an asset by gym and kind if the kind does not exist`() {
        val gym = this.createGym()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/gym/${gym.id}/kind/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset kind -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not get an asset by gym and kind if the gym does not exist`() {
        val kind = this.createAssetKind()
        this.mockMvc.perform(MockMvcRequestBuilders.get("${ApiUrls.ASSETS}/gym/-1/kind/${kind.id}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("gym -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should create an asset`() {
        val asset = AssetDTOInput("ciclette2", this.createAssetKind().id, this.createGym().id)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`(asset.name)))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should update an asset`() {
        val kind = this.createAssetKind()
        val gym = this.createGym()
        val savedAsset = this.assetDAO.save(Asset("ciclette2", kind, gym))
        val assetDTOInput = AssetDTOInput("newName", kind.id, gym.id)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSETS}/${savedAsset.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(assetDTOInput)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.`is`("newName")))
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update an asset if it does not exist`() {
        val asset = AssetDTOInput("aaa", this.createAssetKind().id, this.createGym().id)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSETS}/-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update an asset if the gym does not exist`() {
        val asset = this.assetDAO.save(Asset("dsa", this.createAssetKind(), this.createGym()))
        val assetDTO = AssetDTOInput("aaa", this.createAssetKind().id, -1)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSETS}/${asset.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(assetDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not update an asset if the asset kind does not exist`() {
        val asset = this.assetDAO.save(Asset("dsa", this.createAssetKind(), this.createGym()))
        val assetDTO = AssetDTOInput("aaa", -1, this.createGym().id)
        mockMvc.perform(MockMvcRequestBuilders.put("${ApiUrls.ASSETS}/${asset.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(assetDTO)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset if its name already exists inside the gym`() {
        val asset = this.assetDAO.save(Asset("ciclette2", this.createAssetKind(), this.createGym()))
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(AssetDTOInput(asset))))
                .andExpect(MockMvcResultMatchers.status().isConflict)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset if the gym does not exist`() {
        val asset = AssetDTOInput("aaa", this.createAssetKind().id, -1)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should not create an asset if the asset kind does not exist`() {
        val asset = AssetDTOInput("aaa", -1, this.createGym().id)
        mockMvc.perform(MockMvcRequestBuilders.post(ApiUrls.ASSETS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(asset)))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `Should delete an asset`() {
        val kind = this.createAssetKind()
        val asset = Asset("ciclette2", kind, this.createGym())
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
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].message", Matchers.`is`("asset -1 does not exist")))
                .andDo(MockMvcResultHandlers.print())
    }

    private fun createGym(): Gym {
        val city = this.cityDAO.save(MockEntities.mockCities[0])
        return this.gymDAO.save(Gym("gym1", "address1", city))
    }

    private fun createAssetKind(): AssetKind {
        return this.assetKindDAO.save(MockEntities.assetKinds[0])
    }
}
