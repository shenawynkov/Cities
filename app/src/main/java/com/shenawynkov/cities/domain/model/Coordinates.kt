package com.shenawynkov.cities.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val lon: Double,
    val lat: Double
) 