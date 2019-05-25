package com.gabrigiunchi.backendtesi.service

import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.LatLng
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class GeocodingService {

    @Value("\${google.apikey}")
    private var apiKey: String = ""

    fun geocode(address: String): LatLng {
        val context = GeoApiContext.Builder()
                .apiKey(this.apiKey)
                .build()

        val results = GeocodingApi.geocode(context, address).await()
        return results[0].geometry.location
    }
}