package com.shenawynkov.cities.domain.usecase

import com.shenawynkov.cities.domain.model.City
import com.shenawynkov.cities.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    operator fun invoke(query: String): Flow<List<City>> {
        // The use case can also decide if a blank prefix should return all cities
        // or let the repository handle that. For now, assume repository handles it or ViewModels manage this logic.
        return cityRepository.searchCities(query)
    }
} 