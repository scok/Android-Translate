package com.example.translation.ui.webtranslate

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.translation.MainActivity
import com.example.translation.R
import kotlinx.android.synthetic.main.activity_favorites.*

class FavoritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        var doc = Jsoup.connect(url).get()
        var elements = doc.select("link apple-touch-icon").select("head[sizes]")
        
        val intent = intent
        //val favorList : LinkedHashMap<String, String>  = intent.getSerializableExtra("favor_list") as LinkedHashMap<String, String>
        //val items : ArrayList<String> = ArrayList(favorList.values)
        val items : ArrayList<String>  = intent.getSerializableExtra("favor_list") as ArrayList<String>
        val adapter = ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,items)
        favorites_list.adapter = adapter
        adapter.notifyDataSetChanged()
        favorites_list.setOnItemClickListener { adapterView, view, i, l ->
            intent.putExtra("favor_index",i )
            setResult(RESULT_OK, intent)
            for(e in elements){
                var url = e.absUrl("href")
            }
            finish()
        }
        favorites_end.setOnClickListener {
            finish()
        }
    }
}
