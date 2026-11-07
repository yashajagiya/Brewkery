package com.example.brewkery

import com.example.brewkery.data.model.CoffeeItemDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeFilterTest {

    private lateinit var sampleItems: List<CoffeeItemDto>

    @Before
    fun setup() {
        sampleItems = listOf(
            CoffeeItemDto(
                id = 1,
                categoryId = "cat_hot_coffee",
                name = "Toasted Caramel Macchiato",
                tagline = "Double shot espresso with velvety oat milk & salted caramel",
                description = "Rich espresso roast layered with velvety steamed milk",
                basePrice = 4.85
            ),
            CoffeeItemDto(
                id = 2,
                categoryId = "cat_cold_brews",
                name = "Velvet Vanilla Cold Foam",
                tagline = "18-hour cold steeped coffee topped with sweet cream foam",
                description = "Slow-steeped Colombian single-origin beans",
                basePrice = 5.40
            ),
            CoffeeItemDto(
                id = 3,
                categoryId = "cat_bakery",
                name = "Golden Normandy Croissant",
                tagline = "36-layer flakey pastry with European churned butter",
                description = "Baked fresh at sunrise with Normandy butter",
                basePrice = 3.90
            ),
            CoffeeItemDto(
                id = 4,
                categoryId = "cat_bakery",
                name = "Crushed Pistachio Danish",
                tagline = "Flaky puff pastry with rich Sicilian pistachio custard",
                description = "Laminated pastry parcel filled with pistachio paste",
                basePrice = 4.60
            )
        )
    }

    private fun applyFilters(
        items: List<CoffeeItemDto>,
        categoryId: String,
        query: String
    ): List<CoffeeItemDto> {
        return items.filter { item ->
            val matchesCategory = (categoryId == "ALL") || (item.categoryId == categoryId)
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.tagline.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    @Test
    fun testCategoryFilterAll() {
        val result = applyFilters(sampleItems, "ALL", "")
        assertEquals(4, result.size)
    }

    @Test
    fun testCategoryFilterSpecificCategory() {
        val hotCoffeeResult = applyFilters(sampleItems, "cat_hot_coffee", "")
        assertEquals(1, hotCoffeeResult.size)
        assertEquals("Toasted Caramel Macchiato", hotCoffeeResult.first().name)

        val bakeryResult = applyFilters(sampleItems, "cat_bakery", "")
        assertEquals(2, bakeryResult.size)
    }

    @Test
    fun testSearchQueryMatchingName() {
        val result = applyFilters(sampleItems, "ALL", "Vanilla")
        assertEquals(1, result.size)
        assertEquals("Velvet Vanilla Cold Foam", result.first().name)
    }

    @Test
    fun testSearchQueryCaseInsensitive() {
        val result = applyFilters(sampleItems, "ALL", "pistachio")
        assertEquals(1, result.size)
        assertEquals("Crushed Pistachio Danish", result.first().name)
    }

    @Test
    fun testSearchQueryMatchingTaglineOrDescription() {
        val result = applyFilters(sampleItems, "ALL", "espresso")
        assertEquals(1, result.size)
        assertEquals(1, result.first().id)
    }

    @Test
    fun testCombinedCategoryAndSearchQuery() {
        val result = applyFilters(sampleItems, "cat_bakery", "Croissant")
        assertEquals(1, result.size)
        assertEquals("Golden Normandy Croissant", result.first().name)

        val noMatch = applyFilters(sampleItems, "cat_hot_coffee", "Croissant")
        assertTrue(noMatch.isEmpty())
    }
}
