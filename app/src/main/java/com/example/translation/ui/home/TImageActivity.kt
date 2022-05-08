package com.example.translation.ui.home

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.translation.R
import kotlinx.android.synthetic.main.activity_timage.*
import java.io.*
import java.util.*


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
        trImageView.setImageBitmap(bm)
    }
}