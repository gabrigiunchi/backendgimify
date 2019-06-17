package com.gabrigiunchi.backendtesi.model.entities

import com.gabrigiunchi.backendtesi.model.type.ImageType
import javax.persistence.Entity
import javax.persistence.Id

@Entity
open class Image(
        @Id
        val id: String,
        val type: ImageType,
        val lastModified: Long)