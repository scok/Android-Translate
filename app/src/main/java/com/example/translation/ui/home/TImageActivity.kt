package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.translation.R
import kotlinx.android.synthetic.main.activity_timage.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.*
import java.net.CookieManager
import java.net.URLDecoder
import java.net.URLEncoder

var image_dir2 : String = ""

class TImageActivity : AppCompatActivity() {

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timage)

        val progressDialog : ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("이미지 구성중...")
        progressDialog.setCancelable(true)
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
        progressDialog.show()

        val handler : Handler = Handler()
        handler.postDelayed(Runnable {
            val intent = intent

            val image_dir = intent.getStringExtra("file_dir")
            val image_dirs = intent.getStringExtra("file_dirs")
            val file_name = intent.getStringExtra("file_name")
            val file_names = intent.getStringExtra("file_names")

            if (image_dir != null) {
                image_dir2 = image_dir
            }

            val target_Language = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("image_targetLanguage", "").toString()

            val clientBuilder : OkHttpClient.Builder = OkHttpClient.Builder()
            val loggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            clientBuilder.addInterceptor(loggingInterceptor)

            val client : Retrofit = Retrofit.Builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build()

            val file = File(image_dir)
            val service = client.create(PapagoService::class.java)

            val partMap = HashMap<String, RequestBody>()
            val trsource = RequestBody.create(MediaType.parse("text/plain"),"en")
            val trtarget = RequestBody.create(MediaType.parse("text/plain"),"ko")
            partMap.put("source",trsource)
            partMap.put("target",trtarget)

            val requestFile = RequestBody.create(MediaType.parse("image/jpeg"),file)
            val body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestFile)

            val call : Call<PapagoEntity> = service.transferPapago("w5lgfrssck","tct9yx0oteeuixAnAdIOETTtKiZFhixSLzNw3vvM",body,partMap)
            call.enqueue(object: Callback<PapagoEntity>{
                override fun onResponse(
                    call: Call<PapagoEntity>,
                    response: Response<PapagoEntity>
                ) {
                    if(response.isSuccessful){
                        Log.d("Test","번역 완료")
                        val imageStr = response.body()?.data?.renderedImage.toString()
                        val bytePlainOrg = Base64.decode(imageStr,0)
                        val inStream : ByteArrayInputStream = ByteArrayInputStream(bytePlainOrg)
                        val bm : Bitmap = BitmapFactory.decodeStream(inStream)
                        val imageView : SubsamplingScaleImageView = findViewById(R.id.trImageView)
                        imageView.setImage(ImageSource.bitmap(bm))
                    }else{
                        Log.e("Test","번역 에러")
                    }
                }

                override fun onFailure(call: Call<PapagoEntity>, t: Throwable) {
                    Log.e("Test","통신 에러")
                }
            })

            progressDialog.dismiss()
        },1000)
    }

    private fun clearCache(){
        val cacheDirFile : File = this.cacheDir
        if(cacheDirFile.isDirectory){
            clearSubCacheFiles(cacheDirFile)
        }
    }

    private fun clearSubCacheFiles(cacheDirFile : File){
        if(cacheDirFile.isFile){
            return
        }
        for (cacheFile in cacheDirFile.listFiles()!!) {
            if (cacheFile.isFile) {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
            } else {
                clearSubCacheFiles(cacheFile)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearCache()
    }
}