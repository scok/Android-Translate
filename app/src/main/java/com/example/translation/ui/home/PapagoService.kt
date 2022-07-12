package com.example.translation.ui.home

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface PapagoService {
    @Multipart
    @POST("/image-to-image/v1/translate")
    fun transferPapago(
        @Header("X-NCP-APIGW-API-KEY-ID") apiKeyID: String,
        @Header("X-NCP-APIGW-API-KEY") apiKey:String,
        @Part image : MultipartBody.Part,
        @PartMap data : HashMap<String, RequestBody>
    ): Call<PapagoEntity>
}