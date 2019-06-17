package com.gabrigiunchi.backendtesi.dao

import com.gabrigiunchi.backendtesi.model.entities.Drawable
import org.springframework.data.repository.CrudRepository

interface DrawableDAO : CrudRepository<Drawable, Int>