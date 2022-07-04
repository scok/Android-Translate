package com.example.translation.ui.home

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.File

interface PapagoService {
    @FormUrlEncoded
    @POST("translate")
    fun transferPapago(
        @Header("X-NCP-APIGW-API-KEY-ID") apiKeyID: String = "w5lgfrssck",
        @Header("X-NCP-APIGW-API-KEY") apiKey:String = "tct9yx0oteeuixAnAdIOETTtKiZFhixSLzNw3vvM",
        @Field("source") source: String = "en",
        @Field("target") target: String = "ko",
        @Field("image") image: String = image_dir2
    ): Call<PapagoEntity>
}