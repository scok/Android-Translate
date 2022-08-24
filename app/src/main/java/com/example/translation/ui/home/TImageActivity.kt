package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
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
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

var image_dir2 : String = ""

class TImageActivity : AppCompatActivity() {

    private var mDownloadManager: DownloadManager? = null
    var mDownloadQueueId : Long? = null

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timage)

        val target_Language = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("image_targetLanguage", "").toString()

        val intent = intent

        val image_url = intent.getStringExtra("image_url")
        val image_name = intent.getStringExtra("image_name")

        val outputFilepath : String = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+"/img_temp")}/$image_name"
        val outputFilepath2 : String = "${externalCacheDir}/$image_name"
        val uri = Uri.parse(image_url)
        URLDownloading(uri,outputFilepath2)

        val progressDialog : ProgressDialog = ProgressDialog(this)
        progressDialog.setMessage("이미지 구성중...")
        progressDialog.setCancelable(true)
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal)
        progressDialog.show()

        val handler : Handler = Handler()
        handler.postDelayed(Runnable {
            val clientBuilder : OkHttpClient.Builder = OkHttpClient.Builder()
            val loggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            clientBuilder.addInterceptor(loggingInterceptor)

            val client : Retrofit = Retrofit.Builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build()

            val file = File(outputFilepath2)
            Log.d("Test-Sample",file.toString())
            val service = client.create(PapagoService::class.java)

            val partMap = HashMap<String, RequestBody>()
            val trsource = RequestBody.create(MediaType.parse("text/plain"),"en")
            val trtarget = RequestBody.create(MediaType.parse("text/plain"),"ko")
            partMap.put("source",trsource)
            partMap.put("target",trtarget)

            val requestFile = RequestBody.create(MediaType.parse("image/jpeg"),file)
            val body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestFile)

            val call : Call<PapagoEntity> = service.transferPapago("w5lgfrssck","tct9yx0oteeuixAnAdIOETTtKiZFhixSLzNw3vvM",body,partMap)
            call.enqueue(object: Callback<PapagoEntity> {
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
                        val imageView = findViewById<ImageView>(R.id.trImageView)
                        imageView.setImageBitmap(bm)
                    }else{
                        Log.e("Test","번역 에러")
                    }
                }

                override fun onFailure(call: Call<PapagoEntity>, t: Throwable) {
                    Log.e("Test","통신 에러")
                }
            })

            progressDialog.dismiss()
        },2000)
    }

    private fun URLDownloading(url: Uri, outputFilepath: String){
        if(mDownloadManager == null){
            mDownloadManager = (applicationContext.getSystemService(Context.DOWNLOAD_SERVICE)) as DownloadManager
        }
        val outputFile : File = File(outputFilepath)
        if(!outputFile.parentFile.exists()){
            outputFile.parentFile.mkdirs()
        }

        val downloadUri : Uri = url
        val request : DownloadManager.Request = DownloadManager.Request(downloadUri)
        var pathSegmentList : List<String> = downloadUri.pathSegments
        request.setTitle("이미지 다운로드")
        request.setDestinationUri(Uri.fromFile(outputFile))
        request.setAllowedOverMetered(true)

        mDownloadQueueId = mDownloadManager!!.enqueue(request)
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