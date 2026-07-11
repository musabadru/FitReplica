package com.fitreplica.core.domain.fake

import com.fitreplica.core.domain.repository.ClosetFilter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeClothingRepositoryTest {
    @Test
    fun `search query matches by token prefix, not substring`() =
        runTest {
            val repository = FakeClothingRepository()
            val item = sampleClothingItem(name = "Blue Nike Jacket")
            repository.addItem(item)

            // Prefix of an indexed token: matches, same as a real FTS4 `nik*` search.
            assertEquals(listOf(item), repository.observeItems(ClosetFilter(searchQuery = "nik")).first())

            // Substring that isn't a prefix of any token: a real FTS4 prefix search for
            // "lu*" would not match "blue" either, so the fake must not match it.
            assertEquals(emptyList<Any>(), repository.observeItems(ClosetFilter(searchQuery = "lu")).first())
        }

    @Test
    fun `hyphenated search terms stay separate tokens instead of merging`() =
        runTest {
            val repository = FakeClothingRepository()
            val item = sampleClothingItem(name = "Blue Nike Jacket")
            repository.addItem(item)

            val results = repository.observeItems(ClosetFilter(searchQuery = "nike-jacket")).first()

            assertEquals(listOf(item), results)
        }
}
