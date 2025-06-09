package com.training.graduation.screens.startmeeting

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("cheating")
    suspend fun detectCheating(
        @Part image: MultipartBody.Part
    ): Response<CheatingResult>

    @Multipart
    @POST("attention")
    suspend fun detectFocus(
        @Part image: MultipartBody.Part
    ): Response<AttentionResult>
}




