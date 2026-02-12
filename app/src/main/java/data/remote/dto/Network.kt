package com.example.goalhabitapp.data.remote
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.goalhabitapp.data.local.TokenStore
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Network {

    fun api(baseUrl: String, tokenStore: TokenStore): ApiService {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val token = runBlocking { tokenStore.tokenFlow.first() }
            val req = chain.request().newBuilder().apply {
                if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token")
            }.build()
            chain.proceed(req)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl) // Эмулятор: http://10.0.2.2:8000/
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
