package com.fitreplica.core.domain.repository

import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.OutfitSuggestion
import com.fitreplica.core.model.SuggestionContext

interface SuggestionEngine {
    suspend fun suggest(
        closet: List<ClothingItem>,
        context: SuggestionContext,
    ): List<OutfitSuggestion>
}
