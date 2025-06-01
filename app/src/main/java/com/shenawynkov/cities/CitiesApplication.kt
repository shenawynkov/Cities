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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            cityRepository.populateTrieFromSourceRequestIfNeeded()
        }
    }
} 