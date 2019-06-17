package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Drawable
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.springframework.data.repository.PagingAndSortingRepository

interface ImageDAO : PagingAndSortingRepository<Image, String> {
    fun findByDrawableAndBucket(drawable: Drawable, bucket: String): List<Image>
    fun findByDrawableAndTypeAndBucket(drawable: Drawable, type: ImageType, bucket: String): List<Image>
}