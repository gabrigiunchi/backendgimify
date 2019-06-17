package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Drawable
import org.springframework.data.repository.PagingAndSortingRepository

interface DrawableDAO : PagingAndSortingRepository<Drawable, Int>