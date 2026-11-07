package com.example.brewkery.data.model

import com.google.gson.annotations.SerializedName

data class CatalogResponse(
    @SerializedName("meta") val meta: MetaDto? = null,
    @SerializedName("categories") val categories: List<CategoryDto> = emptyList(),
    @SerializedName("items") val items: List<CoffeeItemDto> = emptyList()
)

data class MetaDto(
    @SerializedName("app") val app: String? = null,
    @SerializedName("version") val version: String? = null,
    @SerializedName("tagline") val tagline: String? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("currency_symbol") val currencySymbol: String? = "$",
    @SerializedName("delivery_fee") val deliveryFee: Double? = 2.50,
    @SerializedName("tax_rate_percent") val taxRatePercent: Double? = 8.0,
    @SerializedName("estimated_delivery_time") val estimatedDeliveryTime: String? = "20 - 30 mins"
)

data class CategoryDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("icon") val icon: String = "",
    @SerializedName("item_count") val itemCount: Int = 0
)

data class CoffeeItemDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("base_price") val basePrice: Double = 0.0,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("review_count") val reviewCount: Int = 0,
    @SerializedName("prep_time") val prepTime: String = "",
    @SerializedName("calories") val calories: Int = 0,
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("badge") val badge: String? = null,
    @SerializedName("ingredients") val ingredients: List<String> = emptyList(),
    @SerializedName("customizations") val customizations: CustomizationsDto? = null
)

data class CustomizationsDto(
    @SerializedName("sizes") val sizes: List<SizeOptionDto> = emptyList(),
    @SerializedName("sugar_levels") val sugarLevels: List<String> = emptyList(),
    @SerializedName("milk_options") val milkOptions: List<MilkOptionDto> = emptyList()
)

data class SizeOptionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("extra_price") val extraPrice: Double = 0.0
)

data class MilkOptionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("extra_price") val extraPrice: Double = 0.0
)
