package com.shenawynkov.cities.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class City(
    val country: String,
    val name: String,
    @SerialName("_id") val id: Int,
    val coord: Coordinates
) 