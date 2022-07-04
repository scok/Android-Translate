package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
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
import okhttp3.OkHttpClient
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

            if(!Python.isStarted()){
                Python.start(AndroidPlatform(this))
            }

            if (image_dir != null) {
                Log.d("Test",image_dir)
                image_dir2 = URLEncoder.encode(image_dir,"EUC-KR")
            }

            val target_Language = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("image_targetLanguage", "").toString()

            val papagoService = NaverAPI().create()
            val call = papagoService.transferPapago()
            call.enqueue(object : retrofit2.Callback<PapagoEntity>{
                override fun onResponse(
                    call: Call<PapagoEntity>,
                    response: Response<PapagoEntity>
                ) {
                    if(response.isSuccessful){
                        Log.d("Test","Successful!")

                        val result = response.body()
                        val imageString = result?.data?.renderedImage
                        if (imageString != null) {
                            Log.d("Test",imageString)
                        }
                        Log.e("Test",response.raw().toString())
                    }
                    else{
                        Log.e("Test","fail!")
                        Log.e("Test","error code : "+response.code())
                        Log.e("Test","error message : "+response.message())
                    }
                }

                override fun onFailure(call: Call<PapagoEntity>, t: Throwable) {
                    Log.e("Test","onFailure!")
                    t.printStackTrace()
                }
            })


            /*
            val py : Python = Python.getInstance()
            val pyo : PyObject = py.getModule("test")
            val imageStr = pyo.callAttr("translate",target_Language, image_dirs,file_name,file_names).toString()

            val bytePlainOrg = Base64.decode(imageStr,0)
            val inStream : ByteArrayInputStream = ByteArrayInputStream(bytePlainOrg)
            val bm : Bitmap = BitmapFactory.decodeStream(inStream)
            //trImageView.setImageBitmap(bm)

            val imageView : SubsamplingScaleImageView = findViewById(R.id.trImageView)
            imageView.setImage(ImageSource.bitmap(bm))

             */
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