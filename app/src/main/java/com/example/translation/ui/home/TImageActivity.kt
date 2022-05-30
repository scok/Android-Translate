package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Base64
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
import java.io.*


class TImageActivity : AppCompatActivity() {

    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timage)

        val intent = intent

        val image_dir = intent.getStringExtra("file_dirs")
        val file_name = intent.getStringExtra("file_name")
        val file_names = intent.getStringExtra("file_names")

        if(!Python.isStarted()){
            Python.start(AndroidPlatform(this))
        }

        val target_Language = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("image_targetLanguage", "").toString()

        val py : Python = Python.getInstance()
        val pyo : PyObject = py.getModule("test")
        val imageStr = pyo.callAttr("translate",target_Language, image_dir,file_name,file_names).toString()

        val bytePlainOrg = Base64.decode(imageStr,0)
        val inStream : ByteArrayInputStream = ByteArrayInputStream(bytePlainOrg)
        val bm : Bitmap = BitmapFactory.decodeStream(inStream)
        //trImageView.setImageBitmap(bm)

        val imageView : SubsamplingScaleImageView = findViewById(R.id.trImageView)
        imageView.setImage(ImageSource.bitmap(bm))
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