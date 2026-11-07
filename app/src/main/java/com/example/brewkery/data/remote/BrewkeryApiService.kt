package com.example.brewkery.data.remote

import com.example.brewkery.data.model.CatalogResponse
import com.example.brewkery.data.model.CoffeeItemDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BrewkeryApiService {
    @GET("data.json")
    suspend fun getCatalog(): Response<CatalogResponse>

    @GET("api/items/{id}.json")
    suspend fun getItemDetail(@Path("id") id: Int): Response<CoffeeItemDto>

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/VivekShah138/Brewkery/main/"
    }
}
