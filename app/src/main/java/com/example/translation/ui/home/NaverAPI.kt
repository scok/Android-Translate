package com.example.translation.ui.home

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NaverAPI{
    val BASE_URL = "https://naveropenapi.apigw.ntruss.com/image-to-image/v1/"

    fun create() : PapagoService {
        val clientBuilder : OkHttpClient.Builder = OkHttpClient.Builder()
        val loggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        clientBuilder.addInterceptor(loggingInterceptor)

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .build()
            .create(PapagoService::class.java)
    }
}