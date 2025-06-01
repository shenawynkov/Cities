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
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    private val trie: Trie,
    private val ioDispatcher: CoroutineDispatcher // Already provided by AppModule
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
    override suspend fun populateDatabaseFromSourceRequestIfNeeded() { // Renaming to loadAndProcessCitiesIfNeeded internally
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
                withContext(Dispatchers.Default) { // Use Default dispatcher for Trie building
                    trie.build(sortedCities) // Pass the sorted list, Trie will use city.name
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
        // This Flow will emit the full sorted list once loaded.
        // Call ensureLoaded to trigger loading if it hasn't happened.
        // For simplicity here, assuming loadAndProcessCitiesIfNeeded is called from Application/ViewModel init.
        return _allCitiesSorted.asStateFlow()
    }

    override fun searchCities(query: String): Flow<List<City>> {
        // Ensure data is loaded before searching. For simplicity, assume it is.
        // The search itself is not a Flow generating operation with the Trie, it's direct.
        // We wrap the result in a flow to match the repository interface, but it emits only one list.
        // ViewModel will need to handle this appropriately if it expects continuous updates from search Flow.
        // Given the constraints, direct list return from a suspend fun might be better for the use case.
        // However, to keep changes minimal for now, wrap in a flow.

        val searchResults = if (query.isBlank()) {
            _allCitiesSorted.value // Return all sorted cities if query is blank
        } else {
            trie.search(query).sortedWith( // Trie results also need to be sorted for consistent display
                compareBy({ it.name.lowercase(Locale.getDefault()) }, { it.country.lowercase(Locale.getDefault()) })
            )
        }
        return MutableStateFlow(searchResults).asStateFlow() // Emit as a simple state flow
    }
} 