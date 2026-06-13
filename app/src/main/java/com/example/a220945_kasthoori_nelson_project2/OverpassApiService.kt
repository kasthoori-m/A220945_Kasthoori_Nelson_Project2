package com.example.a220945_kasthoori_nelson_project2.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// DATA MODELS
// ---------------------------------------------------------------------------

data class OverpassResponse(
    val elements: List<OverpassElement>
)

data class OverpassElement(
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val tags: Map<String, String>?
) {
    val name: String get() = tags?.get("name") ?: "Unnamed Place"
    val amenity: String get() = tags?.get("amenity") ?: tags?.get("leisure") ?: "place"
    val displayType: String get() = amenity.replace("_", " ").replaceFirstChar { it.uppercase() }
}

// ---------------------------------------------------------------------------
// RETROFIT INTERFACE
// ---------------------------------------------------------------------------

interface OverpassApiService {
    @GET("interpreter")
    suspend fun searchNearby(@Query("data") data: String): OverpassResponse
}

// ---------------------------------------------------------------------------
// RETROFIT SINGLETON — with proper OkHttp client
// Overpass API requires a User-Agent header, otherwise it returns 429/403.
// We also increase timeouts since Overpass can be slow.
// ---------------------------------------------------------------------------

object OverpassRetrofit {
    private const val BASE_URL = "https://overpass-api.de/api/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // Add User-Agent so Overpass API accepts our request
            val request = chain.request().newBuilder()
                .header("User-Agent", "EduQuestApp/1.0 (Android; educational project)")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC // Logs URL + response code for debugging
            }
        )
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)   // Overpass can be slow — give it enough time
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: OverpassApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)            // Use our custom client with User-Agent
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApiService::class.java)
    }
}