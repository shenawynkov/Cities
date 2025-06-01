package com.shenawynkov.cities.domain.usecase

import com.shenawynkov.cities.domain.model.City
import com.shenawynkov.cities.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInitialCitiesUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    suspend operator fun invoke(): Flow<List<City>> {
        cityRepository.populateTrieFromSourceRequestIfNeeded()
        return cityRepository.getCities()
    }
} 