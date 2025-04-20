package com.training.graduation.token
import TokenApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuild {

    private var retrofit: Retrofit? = null

    private fun builtRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(Constent.jassTokenApi)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getApi(): TokenApi {
        return builtRetrofit().create(TokenApi::class.java)
    }
}
