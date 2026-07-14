package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.NoOpWeatherProvider
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.ClothingItem
import com.fitreplica.core.model.ClothingType
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.Status
import com.fitreplica.core.model.SuggestionContext
import com.fitreplica.core.model.WeatherSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleEngineTest {
    @Test
    fun `prefers underused clean non-retired items`() =
        runTest {
            val engine = RuleEngine(NoOpWeatherProvider())
            val cleanUnderused = item("clean-underused", Status.CLEAN, Condition.GOOD, timesWorn = 1)
            val cleanOverused = item("clean-overused", Status.CLEAN, Condition.GOOD, timesWorn = 10)
            val dirty = item("dirty", Status.DIRTY, Condition.GOOD, timesWorn = 0)
            val retired = item("retired", Status.CLEAN, Condition.RETIRED, timesWorn = 0)

            val suggestions = engine.suggest(listOf(cleanOverused, dirty, retired, cleanUnderused), SuggestionContext())

            assertEquals(ClothingId("clean-underused"), suggestions.first().itemIds.single())
        }

    @Test
    fun `ignores blank tags and trims padded tags`() =
        runTest {
            val engine = RuleEngine(NoOpWeatherProvider())
            val blueItem = item("blue-shirt", Status.CLEAN, Condition.GOOD, timesWorn = 0)

            val suggestions =
                engine.suggest(
                    listOf(blueItem),
                    SuggestionContext(tags = setOf(" ", " blue ")),
                )

            assertEquals(ClothingId("blue-shirt"), suggestions.single().itemIds.single())
        }

    @Test
    fun `weather tags do not act as required text tags`() =
        runTest {
            val engine = RuleEngine(NoOpWeatherProvider())
            val item = item("plain-shirt", Status.CLEAN, Condition.GOOD, timesWorn = 0)

            val suggestions =
                engine.suggest(
                    listOf(item),
                    SuggestionContext(weather = WeatherSnapshot(conditionTags = setOf("rainy"))),
                )

            assertEquals(ClothingId("plain-shirt"), suggestions.single().itemIds.single())
        }

    private fun item(
        id: String,
        status: Status,
        condition: Condition,
        timesWorn: Int,
    ): ClothingItem =
        ClothingItem(
            id = ClothingId(id),
            name = id,
            type = ClothingType.TOP,
            brand = null,
            colorPrimary = "blue",
            colorSecondary = null,
            condition = condition,
            status = status,
            size = null,
            timesWorn = timesWorn,
            lastWornAt = null,
            addedAt = 0L,
        )
}
