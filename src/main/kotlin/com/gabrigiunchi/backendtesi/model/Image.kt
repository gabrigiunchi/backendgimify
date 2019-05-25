package com.gabrigiunchi.backendtesi.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
open class Image(
        @Id
        val id: String,
        val lastModified: Long)