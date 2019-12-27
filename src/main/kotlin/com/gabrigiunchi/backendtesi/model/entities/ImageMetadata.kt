package com.gabrigiunchi.backendtesi.model.entities

import com.gabrigiunchi.backendtesi.model.type.ImageType


class ImageMetadata(val id: String, val type: ImageType, val bucketName: String, val lastModified: Long) {
    constructor(image: Image) : this(image.id, image.type, image.bucket, image.lastModified)
}