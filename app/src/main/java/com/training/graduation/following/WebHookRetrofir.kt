//package com.training.graduation.following
//
//import com.training.graduation.token.Constent
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class RetrofitBuild {
//
//    companion object {
//
//        private var retrofit: Retrofit? = null
//
//        private fun built_retrofit(): Retrofit {
//
//            if (retrofit == null) {
//                retrofit = Retrofit.Builder().baseUrl(Constent.webhook)
//                    .addConverterFactory(GsonConverterFactory.create()).build()
//            }
//
//            return retrofit!!
//
//        }
//        fun get_webhook_api(): WebhookApi {
//            return built_retrofit().create(WebhookApi::class.java)
//
//        }
//    }
//}
