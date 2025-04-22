package com.training.graduation.chat_token

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://web-production-5c347.up.railway.app/"

    val instance: ChatTokenApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ChatTokenApi::class.java)
    }
}
