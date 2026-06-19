package com.example.data.api

import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface CineApiService {
    @GET
    suspend fun getRemoteCatalog(@Url url: String): List<MediaItem>

    companion object {
        // Default URL for live database on GitHub or another dynamic endpoint
        // Users can easily host this JSON anywhere (e.g. GitHub raw, Firebase hosting, Gist)
        const val DEFAULT_DATABASE_URL = "https://raw.githubusercontent.com/aviaosebola/free-films-database/main/catalog.json"

        fun create(): CineApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            // We use a dummy base URL since we pass full custom URLs to the parameter
            return Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CineApiService::class.java)
        }
    }
}
