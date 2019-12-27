package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Drawable
import com.gabrigiunchi.backendtesi.model.entities.Image
import com.gabrigiunchi.backendtesi.model.type.ImageType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface ImageDAO : PagingAndSortingRepository<Image, String> {
    fun findByIdAndBucket(imageId: String, bucket: String): Optional<Image>
    fun findByBucket(bucket: String, pageable: Pageable): Page<Image>
    fun findByDrawableAndBucket(drawable: Drawable, bucket: String): List<Image>
    fun findByDrawableAndTypeAndBucket(drawable: Drawable, type: ImageType, bucket: String): List<Image>
}