package com.fitreplica.core.model

data class SuggestionContext(
    val tags: Set<String> = emptySet(),
    val preferredTypes: Set<ClothingType> = emptySet(),
    val weather: WeatherSnapshot? = null,
)

data class OutfitSuggestion(
    val id: String,
    val title: String,
    val itemIds: List<ClothingId>,
    val reason: String,
)

data class WeatherSnapshot(
    val temperatureCelsius: Double? = null,
    val conditionTags: Set<String> = emptySet(),
)
