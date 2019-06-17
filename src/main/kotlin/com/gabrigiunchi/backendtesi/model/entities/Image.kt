package com.gabrigiunchi.backendtesi.model.entities

import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
open class Image(
        @Id
        val id: String,
        val type: ImageType,

        @ManyToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        val drawable: Drawable,

        val lastModified: Long,
        val bucket: String) {

    constructor(id: String, type: ImageType, drawable: Drawable, bucket: String) :
            this(id, type, drawable, Date().time, bucket)
}