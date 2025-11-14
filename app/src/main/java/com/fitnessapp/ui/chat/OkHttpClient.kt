package com.fitnessapp.ui.chat

import okhttp3.Interceptor
import okhttp3.OkHttpClient

object OkHttpClientHelper {
    fun getClient(apiKey: String): OkHttpClient {
        val interceptor = Interceptor { chain ->
            val req = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(req)
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }
}
