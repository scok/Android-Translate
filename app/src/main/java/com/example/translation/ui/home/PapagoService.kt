package com.example.translation.ui.home

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

// 파파고 API 인터페이스
interface PapagoService {
    @Multipart
    @POST("/image-to-image/v1/translate")
    fun transferPapago(
        @Header("X-NCP-APIGW-API-KEY-ID") apiKeyID: String, // 키 아이디
        @Header("X-NCP-APIGW-API-KEY") apiKey:String, // API KEY
        @Part image : MultipartBody.Part, // 이미지 값
        @PartMap data : HashMap<String, RequestBody> // 정보
    ): Call<PapagoEntity>
}