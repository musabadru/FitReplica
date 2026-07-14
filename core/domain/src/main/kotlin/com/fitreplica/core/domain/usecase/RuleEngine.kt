package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.SuggestionEngine
import com.fitreplica.core.domain.repository.WeatherProvider
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.OutfitSuggestion
import com.fitreplica.core.model.Status
import com.fitreplica.core.model.SuggestionContext
import javax.inject.Inject

private const val MAX_SUGGESTIONS = 3

class RuleEngine
    @Inject
    constructor(
        private val weatherProvider: WeatherProvider,
    ) : SuggestionEngine {
        override suspend fun suggest(
            closet: List<ClothingItem>,
            context: SuggestionContext,
        ): List<OutfitSuggestion> {
            weatherProvider.currentWeatherUnlessProvided(context)
            val tags = context.tags.normalizedTags()
            return closet
                .asSequence()
                .filter { it.status == Status.CLEAN }
                .filterNot { it.condition == Condition.RETIRED }
                .filter { context.preferredTypes.isEmpty() || it.type in context.preferredTypes }
                .filter { item -> tags.isEmpty() || tags.all { tag -> item.matchesTag(tag) } }
                .sortedWith(compareBy<ClothingItem> { it.timesWorn }.thenByDescending { it.addedAt })
                .take(MAX_SUGGESTIONS)
                .mapIndexed { index, item ->
                    val reason =
                        if (tags.isEmpty()) {
                            "Underused clean item"
                        } else {
                            "Underused clean item matching ${tags.sorted().joinToString()}"
                        }
                    OutfitSuggestion(
                        id = "rule-${item.id.value}-$index",
                        title = item.name,
                        itemIds = listOf(item.id),
                        reason = reason,
                    )
                }.toList()
        }
    }

private suspend fun WeatherProvider.currentWeatherUnlessProvided(context: SuggestionContext) {
    if (context.weather == null) currentWeather()
}

private fun ClothingItem.matchesTag(tag: String): Boolean {
    val normalized = tag.trim().lowercase()
    return listOfNotNull(name, brand, colorPrimary, colorSecondary, type.name, notes)
        .any { value -> value.lowercase().contains(normalized) }
}

private fun Set<String>.normalizedTags(): Set<String> =
    mapNotNullTo(mutableSetOf()) { tag ->
        tag.trim().lowercase().takeIf(String::isNotBlank)
    }
