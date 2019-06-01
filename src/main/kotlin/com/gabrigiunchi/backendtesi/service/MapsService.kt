package com.gabrigiunchi.backendtesi.service

import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.TimeZoneApi
import com.google.maps.model.LatLng
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZoneId


@Service
class MapsService {

    @Value("\${google.apikey}")
    private var apiKey: String = ""

    fun geocode(address: String): LatLng? {
        val results = GeocodingApi.geocode(context, address).await()
        return if (results.isEmpty()) null else results[0].geometry.location
    }

    fun getTimezone(latLng: LatLng): ZoneId = TimeZoneApi.getTimeZone(context, latLng).await().toZoneId()

    private val context
        get() = GeoApiContext.Builder().apiKey(this.apiKey).build()
}