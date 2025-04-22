package com.training.graduation.chat_token


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatTokenApi {
    @Headers("Content-Type: application/json")
    @POST("get-token")
    fun getChatToken(
        @Body request: ChatTokenRequest
    ): Call<ChatTokenResponse>
}