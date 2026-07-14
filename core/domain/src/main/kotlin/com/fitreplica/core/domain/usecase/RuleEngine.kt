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
            val weather = context.weather ?: weatherProvider.currentWeather()
            val tags = context.tags + weather.orEmptyTags()
            return closet
                .asSequence()
                .filter { it.status == Status.CLEAN }
                .filterNot { it.condition == Condition.RETIRED }
                .filter { context.preferredTypes.isEmpty() || it.type in context.preferredTypes }
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

private fun com.fitreplica.core.model.WeatherSnapshot?.orEmptyTags(): Set<String> = this?.conditionTags.orEmpty()
