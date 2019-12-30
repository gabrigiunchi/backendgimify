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
        val bucket: String,
        val lastModified: Long,
        @ManyToOne
        @OnDelete(action = OnDeleteAction.CASCADE)
        var drawable: Drawable?) {

    companion object {
        fun copy(image: Image, entity: Drawable, type: ImageType = image.type): Image =
                Image(image.id, type, image.bucket, image.lastModified, entity)

        fun create(id: String, type: ImageType, bucket: String, entity: Drawable? = null) =
                Image(id, type, bucket, Date().time, entity)
    }

    constructor(id: String, type: ImageType, drawable: Drawable, bucket: String) :
            this(id, type, bucket, Date().time, drawable)

    constructor(id: String, type: ImageType, bucket: String, lastModified: Long) :
            this(id, type, bucket, lastModified, null)
}