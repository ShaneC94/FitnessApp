package com.fitnessapp.ui.chat

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// ------------ DATA MODELS ------------
data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    @SerializedName("choices") val choices: List<Choice>
)

data class Choice(
    @SerializedName("message") val message: Message
)

// ------------ RETROFIT SERVICE ------------
interface OpenAIService {
    @Headers(
        "Content-Type: application/json"
    )
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

// ------------ API OBJECT ------------
object OpenAIApi {
    private const val BASE_URL = "https://api.openai.com/v1/"

    fun create(apiKey: String): OpenAIService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClientHelper.getClient(apiKey))
            .build()
            .create(OpenAIService::class.java)
    }
}
