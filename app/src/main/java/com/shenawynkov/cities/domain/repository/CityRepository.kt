package com.shenawynkov.cities.domain.repository

import com.shenawynkov.cities.domain.model.City
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    /**
     * Gets the initial list of cities.
     * This will handle loading from source and populating the database on first run if necessary.
     */
    fun getCities(): Flow<List<City>>

    /**
     * Searches cities based on the query.
     */
    fun searchCities(query: String): Flow<List<City>>

    /**
     * Triggers the process to load data from the raw source (e.g., assets JSON)
     * into the database if it hasn't been done already.
     * This should be called on app startup.
     */
    suspend fun populateDatabaseFromSourceRequestIfNeeded()
} 