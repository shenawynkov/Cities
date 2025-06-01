package com.shenawynkov.cities.data.repository

import android.content.Context
import com.shenawynkov.cities.data.trie.Trie
import com.shenawynkov.cities.domain.model.City
import com.shenawynkov.cities.domain.repository.CityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for managing city data.
 * Given the assignment constraint that "Database implementations are forbidden",
 * this repository loads all city data from a JSON asset into memory upon first request.
 * It uses a Trie data structure for efficient, case-insensitive prefix searching of city names,
 * aiming for O(L) search complexity (L = prefix length) to meet performance requirements.
 *
 * The entire dataset (~200,000 cities) is held in a sorted list in memory (`_allCitiesSorted`)
 * for display, and the Trie also resides in memory. This approach carries a significant
 * risk of OutOfMemoryError on devices with limited RAM, especially considering Android 5.0+
 * compatibility. This is a direct consequence of the "no database" constraint when handling
 * such a large dataset.
 *
 * The initial loading and processing (sorting, Trie building) time is not a primary concern
 * as per assignment requirements ("Initial loading time of the app does not matter").
 */
@Singleton
class CityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    private val trie: Trie,
    private val ioDispatcher: CoroutineDispatcher
) : CityRepository {

    private val _allCitiesSorted = MutableStateFlow<List<City>>(emptyList())
    private var areCitiesLoadedAndProcessed = false
    private val loadMutex = Mutex() // To prevent concurrent loading and Trie building

    companion object {
        private const val CITIES_JSON_FILE_NAME = "cities.json"
    }

    /**
     * This method replaces populateDatabaseFromSourceRequestIfNeeded.
     * It loads cities from the JSON asset, sorts them for display, and builds the Trie for searching.
     * This is an in-memory preprocessing step as databases are forbidden.
     * The efficiency of storing all cities in memory and in a Trie depends on available device RAM.
     * For ~200k entries, this is memory-intensive and risks OOM on lower-end devices,
     * but is chosen to meet the "no database" and "better than linear search" requirements.
     */
    override suspend fun populateTrieFromSourceRequestIfNeeded() {
        loadAndProcessCitiesIfNeeded()
    }

    private suspend fun loadAndProcessCitiesIfNeeded() {
        if (areCitiesLoadedAndProcessed) return

        loadMutex.withLock {
            // Double-check after acquiring the lock
            if (areCitiesLoadedAndProcessed) return@withLock

                try {
                val jsonString = withContext(ioDispatcher) {
                    context.assets.open(CITIES_JSON_FILE_NAME).bufferedReader().use { it.readText() }
                }
                val citiesDomainList: List<City> = json.decodeFromString(jsonString)

                // Sort for display: alphabetically (city first, country after)
                val sortedCities = withContext(Dispatchers.Default) { // Use Default dispatcher for sorting
                    citiesDomainList.sortedWith(
                        compareBy({ it.name.lowercase(Locale.getDefault()) }, { it.country.lowercase(Locale.getDefault()) })
                    )
                    }
                    _allCitiesSorted.value = sortedCities
                    
                // Build the Trie for searching (uses city.name internally)
                    withContext(Dispatchers.Default) {
                        trie.build(sortedCities)
                    }
                areCitiesLoadedAndProcessed = true
                } catch (e: IOException) {
                    e.printStackTrace()
                    _allCitiesSorted.value = emptyList() // Emit empty list on error
                // Handle error, e.g., log, notify user. Data won't be available.
            } catch (e: Exception) { // Catch serialization or other exceptions
                e.printStackTrace()
                _allCitiesSorted.value = emptyList()
            }
        }
    }

    override fun getCities(): Flow<List<City>> {
        return _allCitiesSorted.asStateFlow()
    }

    override fun searchCities(query: String): Flow<List<City>> {

        val searchResults = if (query.isBlank()) {
                _allCitiesSorted.value
            } else {
            trie.search(query)
        }
        return MutableStateFlow(searchResults).asStateFlow()
    }
} 