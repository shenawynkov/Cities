package com.shenawynkov.cities.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shenawynkov.cities.domain.model.City
import com.shenawynkov.cities.domain.usecase.GetInitialCitiesUseCase
import com.shenawynkov.cities.domain.usecase.SearchCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CityViewModel @Inject constructor(
    private val getInitialCitiesUseCase: GetInitialCitiesUseCase,
    private val searchCitiesUseCase: SearchCitiesUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // This will hold the list of cities to be displayed, whether it's the full list or search results.
    private val _displayCities = MutableStateFlow<List<City>>(emptyList())
    // val displayCities: StateFlow<List<City>> = _displayCities.asStateFlow() // If direct list needed

    val cityCount: StateFlow<Int> = _displayCities
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Grouped cities for the UI, derived from _displayCities
    val groupedCities: StateFlow<Map<Char, List<City>>> = _displayCities.map { cities ->
        cities.groupBy { city ->
            val name = city.name.trim()
            if (name.isEmpty()) '#' else name.first().uppercaseChar()
        }.toSortedMap() // Ensures groups are sorted A-Z
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )
    
    // Removed: private var fullSortedCityList: List<City> = emptyList()

    init {
        loadInitialCities()
        observeSearchText()
    }

    private fun loadInitialCities() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                getInitialCitiesUseCase()
                    .catch { e ->
                        e.printStackTrace()
                        _errorMessage.value = "Failed to load cities: ${e.message ?: "Unknown error"}"
                        _displayCities.value = emptyList()
                        _isLoading.value = false
                    }
                    .collect { cities ->
                        _displayCities.value = cities
                        _isLoading.value = false
                        _errorMessage.value = null
                    }
            } catch (e: Exception) { // Catch exceptions from the use case invocation itself
                 e.printStackTrace()
                _errorMessage.value = "Failed to load cities: ${e.message ?: "Unknown error"}"
                _displayCities.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSearchText() {
        viewModelScope.launch {
            searchText
                .debounce(300) // Add debounce to avoid searching on every key press
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    _isLoading.value = true
                    _errorMessage.value = null
                    if (query.isBlank()) {
                        getInitialCitiesUseCase() // Reload initial list if query is blank
                    } else {
                        searchCitiesUseCase(query)
                    }
                }
                .catch { e ->
                    e.printStackTrace()
                    _errorMessage.value = "Search failed: ${e.message ?: "Unknown error"}"
                    _displayCities.value = emptyList()
                    // _isLoading.value should be handled by finally or a separate state in flow
                }
                .onEach { cities ->
                     _displayCities.value = cities
                     _isLoading.value = false // Set loading to false after collecting results
                     if (cities.isEmpty() && searchText.value.isNotBlank()){
                        // If search is successful but returns no results for a non-blank query.
                        // UI will show "No cities found for..."
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = null // Clear error on success or if query is blank showing full list
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        // The actual search logic is now handled by observing _searchText changes in observeSearchText()
    }
} 