package com.example.translation.ui.webtranslate

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.translation.R
import kotlinx.android.synthetic.main.activity_image_viewer_t.*


class ImageViewerT : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer_t)
        try {
            val uri =
                Uri.parse("${cacheDir}-translate.jpg")
            trimageView2.setImageURI(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}