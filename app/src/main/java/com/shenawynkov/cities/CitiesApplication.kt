package com.shenawynkov.cities

import android.app.Application
import com.shenawynkov.cities.domain.repository.CityRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CitiesApplication : Application() {

    @Inject
    lateinit var cityRepository: CityRepository

    // Application-level coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        // Launch a coroutine to populate the database if needed
        // This should not block the main thread.
        applicationScope.launch {
            cityRepository.populateDatabaseFromSourceRequestIfNeeded()
        }
    }
} 