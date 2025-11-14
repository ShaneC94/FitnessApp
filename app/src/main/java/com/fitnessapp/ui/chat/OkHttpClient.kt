package com.fitnessapp.ui.chat

import okhttp3.Interceptor
import okhttp3.OkHttpClient

// This object creates a customized OkHTTPClient that automaticall adds OPENAI API to every HTTP request sent
object OkHttpClientHelper {

    // Creates and returns an OKHTTPClient with an Authorization header interceptor
    fun getClient(apiKey: String): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(req)
        }

        // Build and return the custom OKHTTPClient
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }
}
