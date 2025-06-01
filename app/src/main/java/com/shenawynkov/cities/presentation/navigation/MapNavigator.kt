package com.shenawynkov.cities.presentation.navigation

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.shenawynkov.cities.domain.model.City
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.net.toUri

@Singleton
class MapNavigator @Inject constructor(private val application: Application) {

    fun navigateToMap(city: City) {
        val encodedCityName = Uri.encode(city.name)
        val gmmIntentUriString = "geo:${city.coord.lat},${city.coord.lon}?q=$encodedCityName"
        val gmmIntentUri = gmmIntentUriString.toUri()
        
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            if (mapIntent.resolveActivity(application.packageManager) != null) {
                application.startActivity(mapIntent)
                return // Navigation successful
            } 
        } catch (e: ActivityNotFoundException) {
            // Log or handle if needed, but don't print to console in production
        } catch (e: Exception) {
            // Log or handle if needed
            e.printStackTrace() // Consider removing for production or using a proper logger
        }

        // Fallback to generic geo intent
        val genericGeoUri = "geo:${city.coord.lat},${city.coord.lon}".toUri()
        val genericMapIntent = Intent(Intent.ACTION_VIEW, genericGeoUri)
        genericMapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            if (genericMapIntent.resolveActivity(application.packageManager) != null) {
                application.startActivity(genericMapIntent)
            } else {
                // Optionally handle the case where no map app is found at all
            }
        } catch (e: ActivityNotFoundException) {
            // Log or handle if needed
        } catch (e: Exception) {
            // Log or handle if needed
            e.printStackTrace() // Consider removing for production or using a proper logger
        }
    }
} 