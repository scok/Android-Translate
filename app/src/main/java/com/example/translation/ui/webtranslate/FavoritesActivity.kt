package com.example.translation.ui.webtranslate

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.translation.R
import kotlinx.android.synthetic.main.activity_favorites.*

class FavoritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val intent = intent
        val faver : HashMap<String, String>  = intent.getSerializableExtra("favor_list") as HashMap<String, String>
        val items : ArrayList<String> = ArrayList(faver.values)
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,items)
        favorites_list.adapter = adapter
        adapter.notifyDataSetChanged()

        favorites_end.setOnClickListener {
            finish()
        }
    }
}