package com.example.brewkery

import com.example.brewkery.data.model.CatalogResponse
import com.example.brewkery.data.model.CoffeeItemDto
import com.example.brewkery.ui.components.CartItemSummary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CoffeeModelsTest {

    private val gson = Gson()

    @Test
    fun testCatalogResponseJsonParsing() {
        val sampleJson = """
            {
              "meta": {
                "app": "Brewkery",
                "version": "1.0.0",
                "currency_symbol": "$",
                "delivery_fee": 2.50,
                "tax_rate_percent": 8.0,
                "estimated_delivery_time": "20 - 30 mins"
              },
              "categories": [
                {
                  "id": "cat_hot_coffee",
                  "name": "Hot Coffee",
                  "icon": "☕",
                  "item_count": 2
                }
              ],
              "items": [
                {
                  "id": 1,
                  "category_id": "cat_hot_coffee",
                  "name": "Toasted Caramel Macchiato",
                  "tagline": "Double shot espresso",
                  "base_price": 4.85,
                  "rating": 4.9,
                  "review_count": 312
                }
              ]
            }
        """.trimIndent()

        val catalog = gson.fromJson(sampleJson, CatalogResponse::class.java)

        assertNotNull(catalog)
        assertEquals("Brewkery", catalog.meta?.app)
        assertEquals(2.50, catalog.meta?.deliveryFee ?: 0.0, 0.001)
        assertEquals(1, catalog.categories.size)
        assertEquals("cat_hot_coffee", catalog.categories.first().id)
        assertEquals(1, catalog.items.size)
        assertEquals("Toasted Caramel Macchiato", catalog.items.first().name)
        assertEquals(4.85, catalog.items.first().basePrice, 0.001)
    }

    @Test
    fun testCoffeeItemDtoCustomizationsParsing() {
        val itemJson = """
            {
              "id": 1,
              "category_id": "cat_hot_coffee",
              "name": "Toasted Caramel Macchiato",
              "base_price": 4.85,
              "customizations": {
                "sizes": [
                  { "id": "sz_small", "label": "Tall (8 oz)", "extra_price": 0.0 },
                  { "id": "sz_medium", "label": "Grande (12 oz)", "extra_price": 0.65 }
                ],
                "sugar_levels": [ "0%", "50%", "100%" ],
                "milk_options": [
                  { "id": "m_oat", "name": "Oat Milk", "extra_price": 0.0 },
                  { "id": "m_almond", "name": "Almond Milk", "extra_price": 0.50 }
                ]
              }
            }
        """.trimIndent()

        val dto = gson.fromJson(itemJson, CoffeeItemDto::class.java)

        assertNotNull(dto)
        assertNotNull(dto.customizations)
        assertEquals(2, dto.customizations?.sizes?.size)
        assertEquals("Grande (12 oz)", dto.customizations?.sizes?.get(1)?.label)
        assertEquals(0.65, dto.customizations?.sizes?.get(1)?.extraPrice ?: 0.0, 0.001)
        assertEquals(3, dto.customizations?.sugarLevels?.size)
        assertEquals(2, dto.customizations?.milkOptions?.size)
        assertEquals("Almond Milk", dto.customizations?.milkOptions?.get(1)?.name)
    }

    @Test
    fun testCartItemSummaryJsonSerialization() {
        val summaries = listOf(
            CartItemSummary(name = "Latte", size = "Grande", milk = "Oat Milk", sugar = "50%", quantity = 2, price = 11.20),
            CartItemSummary(name = "Croissant", size = "Single", milk = "None", sugar = "Warm", quantity = 1, price = 3.90)
        )

        val jsonString = gson.toJson(summaries)
        assertNotNull(jsonString)

        val type = object : TypeToken<List<CartItemSummary>>() {}.type
        val parsedList: List<CartItemSummary> = gson.fromJson(jsonString, type)

        assertEquals(2, parsedList.size)
        assertEquals("Latte", parsedList[0].name)
        assertEquals("Grande", parsedList[0].size)
        assertEquals(2, parsedList[0].quantity)
        assertEquals(11.20, parsedList[0].price, 0.001)
        assertEquals("Croissant", parsedList[1].name)
    }
}
