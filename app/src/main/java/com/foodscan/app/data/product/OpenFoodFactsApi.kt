package com.foodscan.app.data.product

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface OpenFoodFactsApi {
    @GET("api/v3/product/{barcode}")
    suspend fun product(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = FIELDS,
        @Query("lc") language: String = "ru",
        @Query("product_type") type: String = "food",
    ): Response<OffResponseDto>

    companion object {
        const val FIELDS = "code,product_name,product_name_ru,brands,categories,categories_tags," +
            "nutriments,product_quantity_unit,quantity,ingredients_text,ingredients_text_ru," +
            "allergens_tags,traces_tags,additives_tags,image_front_url,nutriscore_grade,nova_group"
        const val USER_AGENT = "FoodScan/1.0 (Android; com.foodscan.app; development)"

        fun create(baseUrl: String = "https://world.openfoodfacts.org/"): OpenFoodFactsApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .callTimeout(25, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .addInterceptor { chain ->
                    chain.proceed(chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT).header("Accept", "application/json").build())
                }.build()
            return Retrofit.Builder().baseUrl(baseUrl).client(client)
                .addConverterFactory(GsonConverterFactory.create()).build().create(OpenFoodFactsApi::class.java)
        }
    }
}

data class OffResponseDto(val status: String?, val product: OffProductDto?)

data class OffProductDto(
    val code: String?,
    @SerializedName("product_name") val name: String?,
    @SerializedName("product_name_ru") val russianName: String?,
    val brands: String?,
    val categories: String?,
    @SerializedName("categories_tags") val categoryTags: List<String>?,
    val quantity: String?,
    val nutriments: Map<String, JsonElement>?,
    @SerializedName("product_quantity_unit") val quantityUnit: String?,
    @SerializedName("ingredients_text") val ingredients: String?,
    @SerializedName("ingredients_text_ru") val russianIngredients: String?,
    @SerializedName("allergens_tags") val allergens: List<String>?,
    @SerializedName("traces_tags") val traces: List<String>?,
    @SerializedName("additives_tags") val additives: List<String>?,
    @SerializedName("image_front_url") val imageUrl: String?,
    @SerializedName("nutriscore_grade") val nutriScore: String?,
    @SerializedName("nova_group") val novaGroup: JsonElement?,
)
