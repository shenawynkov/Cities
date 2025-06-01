package com.shenawynkov.cities.di

import com.shenawynkov.cities.data.repository.CityRepositoryImpl
import com.shenawynkov.cities.domain.repository.CityRepository
import com.shenawynkov.cities.domain.usecase.GetInitialCitiesUseCase
import com.shenawynkov.cities.domain.usecase.SearchCitiesUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json { ignoreUnknownKeys = true; prettyPrint = false; isLenient = true }
    }

    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideTrie(): com.shenawynkov.cities.data.trie.Trie {
        return com.shenawynkov.cities.data.trie.Trie()
    }

    @Provides
    fun provideGetInitialCitiesUseCase(cityRepository: CityRepository): GetInitialCitiesUseCase {
        return GetInitialCitiesUseCase(cityRepository)
    }

    @Provides
    fun provideSearchCitiesUseCase(cityRepository: CityRepository): SearchCitiesUseCase {
        return SearchCitiesUseCase(cityRepository)
    }
} 