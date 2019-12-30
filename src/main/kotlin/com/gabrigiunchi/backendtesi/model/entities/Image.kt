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
        var drawable: Drawable?,

        val lastModified: Long,
        val bucket: String) {

    companion object {
        fun copy(image: Image, entity: Drawable, type: ImageType = image.type): Image =
                Image(image.id, type, entity, image.lastModified, image.bucket)

        fun create(id: String, type: ImageType, bucket: String, entity: Drawable? = null) =
                Image(id, type, entity, Date().time, bucket)
    }

    constructor(id: String, type: ImageType, drawable: Drawable, bucket: String) :
            this(id, type, drawable, Date().time, bucket)

    constructor(id: String, type: ImageType, bucket: String, lastModified: Long) :
            this(id, type, null, lastModified, bucket)
}