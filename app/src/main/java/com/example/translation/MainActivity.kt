package com.example.translation

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.view.*
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.example.translation.databinding.ActivityMainBinding
import com.example.translation.ui.webtranslate.ApiTranslateNmt
import com.google.android.material.navigation.NavigationView
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.googlecode.tesseract.android.TessBaseAPI
import org.json.JSONObject
import java.io.*


var pref: Int = 0
var pref2: Int = 0
var pref3: Int = 0
var pref4: String = ""
var pref5: String = ""
var hexColor: String = ""
var hexColor2: String = ""
var hexColor3: String = ""

//---------------스크롤모드 변수 -------------//
//var tagP = null
var fullTranslateMode = false   //전체번역 on off
var translateOn = false // true 시 번역
var innerWindowHeight = 0   // 스크린 사이즈
var originScrollY = 0
//var scrollYCheck = 0
//var pTagTextArray = arrayOf<String>()
//p , a , strong ,li
var tagPIndex = 0
var maxTagPIndex = 0
var tagAIndex = 0
var maxTagAIndex = 0
var tagStrongIndex = 0
var maxTagStrongIndex = 0
var tagLiIndex = 0
var maxTagLiIndex = 0


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var translate: Translate? = null
    lateinit var image : Bitmap
    private lateinit var mTess : TessBaseAPI
    var datapath : String = ""
    lateinit var OCRTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_manage
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        handler.post(handlerTask) // tick timer 실행 [번역기 on]
    }

    private fun copyFiles(){
        try{
            val filepath : String = "$datapath/tessdata.eng.traineddata"
            val assetManager : AssetManager = assets
            val instream : InputStream = assetManager.open("tessdata/eng.traineddata")

            val outstream : OutputStream = FileOutputStream(filepath)
            val buffer = ByteArray(1024)
            var read : Int
            while ((instream.read(buffer).also { read = it })!=-1){
                outstream.write(buffer,0,read)
            }
            outstream.flush()
            outstream.close()
            instream.close()
        }catch (e: FileNotFoundException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun checkFile(dir : File){
        if(!dir.exists() && dir.mkdirs()){
            copyFiles()
        }

        if(dir.exists()){
            val datafilepath : String = "$datapath/tessdata.eng.traineddata"
            val datafile : File = File(datafilepath)
            if(!datafile.exists()){
                copyFiles()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun getTranslateService() {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            resources.openRawResource(R.raw.credential2).use { `is` ->
                val myCredentials = GoogleCredentials.fromStream(`is`)
                val translateOptions =
                    TranslateOptions.newBuilder().setCredentials(myCredentials).build()
                translate = translateOptions.service
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

    @SuppressLint("ResourceType")
    override fun onActionModeStarted(mode: ActionMode) {
        super.onActionModeStarted(mode)

        pref = PreferenceManager.getDefaultSharedPreferences(this)
            .getInt("background_color", ContextCompat.getColor(baseContext, R.color.background))
        pref2 = PreferenceManager.getDefaultSharedPreferences(this)
            .getInt(
                "word_color",
                ContextCompat.getColor(baseContext, R.color.background)
            )
        pref3 = PreferenceManager.getDefaultSharedPreferences(this)
            .getInt(
                "border_color",
                ContextCompat.getColor(baseContext, R.color.background)
            )
        pref4 = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("border_thick", "").toString()
        pref5 = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("border_style", "").toString()
        hexColor = String.format("#%06X", 0xFFFFFF and pref)
        hexColor2 = String.format("#%06X", 0xFFFFFF and pref2)
        hexColor3 = String.format("#%06X", 0xFFFFFF and pref3)

        var cusWebView2 = findViewById<WebView>(R.id.webView)

        //-----------------
        cusWebView2.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (originScrollY < scrollY){
                originScrollY = scrollY
                translateOn = true
            }
        }
        //------------------

        mode.menu.clear()
        mode.menuInflater.inflate(R.menu.drawerlayout,mode.menu)
        mode.menu.getItem(0).setOnMenuItemClickListener {
            cusWebView2.evaluateJavascript("javascript:(function getSelectedText(){return window.getSelection().toString();})()",
                ValueCallback<String>(){ value ->
                    val str = value.toString().substring(1, value.toString().length-1)
                    val translateTask = ApiTranslateNmt(str).execute().get()
                    cusWebView2.evaluateJavascript(
                        "javascript:(function getSelectedText(){\n" +
                                "    var sel, range, newTranslateText, range2, container;\n" +
                                "    if (window.getSelection) {\n" +
                                "        sel = window.getSelection();\n" +
                                "        if (sel.rangeCount) {\n" +
                                "            range = sel.getRangeAt(0);\n" +
                                "            range2 = range.cloneRange();\n" +
                                "            range2.collapse(false);\n" +
                                //"            range = selection.focusOffset;\n" +
                                //"            range.deleteContents();\n" +
                                "            container = document.createElement(\"span\");\n" +
                                "            container.style.backgroundColor = \"$hexColor\";\n" +
                                "            container.style.color = \"$hexColor2\";\n" +
                                "            container.style.border = \"$pref4 $pref5 $hexColor3\";\n" +
                                "            container.style.borderRadius = \"25px\";\n" +
                                "            newTranslateText = document.createTextNode('${translateTask}');\n" +
                                "            container.appendChild(newTranslateText);\n" +
                                //"            range2.appendChild(newTranslateText);\n" +
                                "            range2.insertNode(container);\n" +
                                //"            newTranslateText.appendChild(container);\n" +
                                "        }\n" +
                                "    } else if (document.selection && document.selection.createRange) {\n" +
                                "        range = document.selection.createRange();\n" +
                                "        range.text = '${translateTask}';\n" +
                                "    }\n" +
                                "})()", null
                    )
                }
            )
            return@setOnMenuItemClickListener true
        }
        mode.menu.getItem(2).setOnMenuItemClickListener {
            cusWebView2.evaluateJavascript(
                //     "javascript:(function getPTagText2(){\n" +
                //             "   var tagP = document.getElementsByTagName('p');\n" +
                //             "   var textString = tagP[$tagPIndex].textContent\n" +
                //             "   return textString;\n" +
                //             "})()"
                "javascript:(function getPTagText99(){\n" +
                        "   var tagP = document.getElementsByTagName('p');\n" +
                        "   var textString = tagP[0].innerText; \n" +
                      //  "   var cord = tagP[$tagLiIndex].getBoundingClientRect(); \n" +
                       // "   return cord.y;\n" +
                        "   return textString;\n" +
                        "})()"
            ){value->
                Toast.makeText(applicationContext , value.toString() , Toast.LENGTH_SHORT).show()
                //tagLiIndex += 1
            }
            return@setOnMenuItemClickListener true
        }
        mode.menu.getItem(1).setOnMenuItemClickListener {
            translateToggle()
            return@setOnMenuItemClickListener true
        }
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
    }

    fun scrollTranslate(){
        Toast.makeText(applicationContext , ".." , Toast.LENGTH_SHORT).show()
    }

    val handler = Handler()
    val millisTime = 200
    val handlerTask = object : Runnable {
        override fun run() {
            // do task
            if (translateOn && fullTranslateMode ) {
                val cusWebView2 = findViewById<WebView>(R.id.webView)

                //-- PTag -- //
                if( maxTagPIndex != 0) {
                        // scrollYCheck = scrollY+50
                        cusWebView2.evaluateJavascript(
                            "javascript:(function getPTagText2(){\n" +
                                    // "   var wrapper = document.querySelector('#wrap');\n" +
                                    // "   var lists = wrapper.querySelector('#p');\n" +
                                    "   var tagP = document.getElementsByTagName('p');\n" +
                                    // "   var tagP = ${values.htmlEncode()};\n" +
                                    //"   console.log(tag_p[0]);\n"+
                                    //"   return tagP[1].innerHTML;\n" +
                                    "   var cord = tagP[$tagPIndex].getBoundingClientRect(); \n" +
                                    "   return cord.y;\n" +
                                    "})()"
                        ) { value ->
                            if ( tagPIndex < maxTagPIndex &&  value.toFloat() < innerWindowHeight) {//innerWindowHeight){
                                cusWebView2.evaluateJavascript(
                                    "javascript:(function getPTagText3(){\n" +
                                            "   var tagP = document.getElementsByTagName('p');\n" +
                                            "   var textString = tagP[$tagPIndex].innerText; \n" +
                                            "   return textString;\n" +
                                            "})()"
                                ) { value ->
                                    val str = value.substring(1 , value.toString().length - 1)
                                    val translateTask = ApiTranslateNmt(str).execute().get()
                                    //val translateTask = str
                                    //Toast.makeText(applicationContext , "$value" , Toast.LENGTH_SHORT).show()
                                    // val inputText = "<div>$translateTask</div>"
                                    //Toast.makeText(applicationContext , tagPIndex.toString() , Toast.LENGTH_SHORT).show()
                                    //Toast.makeText(applicationContext, translateTask+tagPIndex.toString(), Toast.LENGTH_SHORT).show()
                                    //Toast.makeText(applicationContext , str , Toast.LENGTH_SHORT).show()
                                    cusWebView2.evaluateJavascript(
                                        "javascript:(function translateText(){\n" +
                                                "   var tagP = document.getElementsByTagName('p');\n" +
                                                "   const newDiv = document.createElement('div');\n" +
                                                // "   newDiv.style.color = 'blue' ;\n"+
                                                "   newDiv.style.backgroundColor = \"$hexColor\";\n" +
                                                "   newDiv.style.color = \"$hexColor2\";\n" +
                                                "   newDiv.style.border = \"$pref4 $pref5 $hexColor3\";\n" +
                                                "   newDiv.style.borderRadius = \"25px\";\n" +
                                                "   const newText = document.createTextNode(\"${translateTask}\");\n" +
                                                "   newDiv.appendChild(newText);\n" +
                                                "   tagP[$tagPIndex].appendChild(newDiv);\n" +
                                                // "   tagP[$tagPIndex].innerHTML += '${inputText}'; \n"+
                                                // "   tagP[$tagPIndex].append($translateTask)\n" + //"${translateTask}"
                                                "})()" , null
                                    )
                                    tagPIndex += 1
                                }
                                /*
                                cusWebView2.evaluateJavascript(
                                    "javascript:(function getPTagText(){\n" +
                                            // "   var wrapper = document.querySelector('#wrap');\n" +
                                            // "   var lists = wrapper.querySelector('#p');\n" +
                                            "   var tagP = document.getElementsByTagName('p');\n" +
                                            //"   console.log(tag_p[0]);\n"+
                                            //"   return tagP[1].innerHTML;\n" +
                                            "   tagP[1].append(\"Hello\");\n" +
                                            //"   var cord = tagP[1].getBoundingClientRect()\n" +
                                            //"   return cord.y;\n" +
                                            "})()",null

                                )*/

                                //oldTagPIndex += 1

                            }
                            // Toast.makeText(applicationContext , "$value&$innerWindowHeight" , Toast.LENGTH_SHORT).show()
                        }
                }

                //----a---!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
                /*
                    cusWebView2.evaluateJavascript(
                        "javascript:(function getPTagText2(){\n" +
                                "   var tagP = document.getElementsByTagName('a');\n" +
                                "   var cord = tagP[$tagAIndex].getBoundingClientRect(); \n" +
                                "   return cord.y;\n" +
                                "})()"
                    ) { value ->
                        if (tagAIndex < maxTagAIndex && value.toFloat() < 200) {//innerWindowHeight){
                            tagAIndex += 1
                            cusWebView2.evaluateJavascript(
                                "javascript:(function getPTagText3(){\n" +
                                        "   var tagP = document.getElementsByTagName('a');\n" +
                                        "   var textString = tagP[$tagAIndex].innerText; \n" +
                                        "   return textString;\n" +
                                        "})()"
                            ) { value ->
                                Toast.makeText(applicationContext , "$value" , Toast.LENGTH_SHORT).show()
                                val str = value.substring(1 , value.toString().length - 1)
                                val translateTask = ApiTranslateNmt(str).execute().get()
                                //val translateTask = str
                                cusWebView2.evaluateJavascript(
                                    "javascript:(function translateText(){\n" +
                                            "   var tagP = document.getElementsByTagName('a');\n" +
                                            "   const newDiv = document.createElement('div');\n" +
                                            "   newDiv.style.backgroundColor = \"$hexColor\";\n" +
                                            "   newDiv.style.color = \"$hexColor2\";\n" +
                                            "   newDiv.style.border = \"$pref4 $pref5 $hexColor3\";\n" +
                                            "   newDiv.style.borderRadius = \"25px\";\n" +
                                            "   const newText = document.createTextNode(\"${translateTask}\");\n" +
                                            "   newDiv.appendChild(newText);\n" +
                                            "   tagP[$tagAIndex].appendChild(newDiv);\n" +
                                            "})()" , null
                                )
                            }
                        }
                    } */
                //----Strong---!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
                if(maxTagStrongIndex != 0) {
                        cusWebView2.evaluateJavascript(
                            "javascript:(function getStrongTagText2(){\n" +
                                    "   var tagP = document.getElementsByTagName('strong');\n" +
                                    "   var cord = tagP[$tagStrongIndex].getBoundingClientRect(); \n" +
                                    "   return cord.y;\n" +
                                    "})()"
                        ) { value ->
                            if(tagStrongIndex < maxTagStrongIndex && value.toFloat() < innerWindowHeight){//innerWindowHeight){
                                cusWebView2.evaluateJavascript(
                                    "javascript:(function getStrongTagText3(){\n" +
                                            "   var tagP = document.getElementsByTagName('strong');\n" +
                                            "   var textString = tagP[$tagStrongIndex].innerText; \n" +
                                            "   return textString;\n" +
                                            "})()"
                                ){ value ->
                                    val str = value.substring(1, value.toString().length-1)
                                    val translateTask = ApiTranslateNmt(str).execute().get()
                                    //val translateTask = str
                                    cusWebView2.evaluateJavascript(
                                        "javascript:(function translateText2(){\n" +
                                                "   var tagP = document.getElementsByTagName('strong');\n" +
                                                "   const newDiv = document.createElement('div');\n"+
                                                "   newDiv.style.backgroundColor = \"$hexColor\";\n" +
                                                "   newDiv.style.color = \"$hexColor2\";\n" +
                                                "   newDiv.style.border = \"$pref4 $pref5 $hexColor3\";\n" +
                                                "   newDiv.style.borderRadius = \"25px\";\n" +
                                                "   const newText = document.createTextNode(\"${translateTask}\");\n"+
                                                "   newDiv.appendChild(newText);\n"+
                                                "   tagP[$tagStrongIndex].appendChild(newDiv);\n"+
                                                "})()", null
                                    )
                                    tagStrongIndex += 1
                                }
                            }
                        }
                }
                //----Li li--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!//
                if(maxTagLiIndex != 0) {
                        cusWebView2.evaluateJavascript(
                            "javascript:(function getPTagText2(){\n" +
                                    "   var tagP = document.getElementsByTagName('li');\n" +
                                    "   var cord = tagP[$tagLiIndex].getBoundingClientRect(); \n" +
                                    "   return cord.y;\n" +
                                    "})()"
                        ) { value ->
                            if(tagLiIndex < maxTagLiIndex && value.toFloat() < innerWindowHeight){//innerWindowHeight){
                                cusWebView2.evaluateJavascript(
                                    "javascript:(function getPTagText3(){\n" +
                                            "   var tagP = document.getElementsByTagName('li');\n" +
                                            "   var textString = tagP[$tagLiIndex].innerText; \n" +
                                            "   return textString;\n" +
                                            "})()"
                                ){ value ->
                                    //Toast.makeText(applicationContext , "$value" , Toast.LENGTH_SHORT).show()
                                    val str = value.substring(1, value.toString().length-1)
                                    val translateTask = ApiTranslateNmt(str).execute().get()
                                    //val translateTask = str
                                    cusWebView2.evaluateJavascript(
                                        "javascript:(function translateText(){\n" +
                                                "   var tagP = document.getElementsByTagName('li');\n" +
                                                "   const newDiv = document.createElement('div');\n"+
                                                "   newDiv.style.backgroundColor = \"$hexColor\";\n" +
                                                "   newDiv.style.color = \"$hexColor2\";\n" +
                                                "   newDiv.style.border = \"$pref4 $pref5 $hexColor3\";\n" +
                                                "   newDiv.style.borderRadius = \"25px\";\n" +
                                                "   const newText = document.createTextNode(\"${translateTask}\");\n"+
                                                "   newDiv.appendChild(newText);\n"+
                                                "   tagP[$tagLiIndex].appendChild(newDiv);\n"+
                                                "})()", null
                                    )
                                    tagLiIndex += 1
                                }
                            }
                        }
                }
                //----------------------------------------//
            }
            translateOn = false
            handler.postDelayed(this, millisTime.toLong()) // millisTiem 이후 다시
        }
    }

    public fun hideNavigationBar() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    fun translateToggle() {
        var cusWebView2 = findViewById<WebView>(R.id.webView)
        if (fullTranslateMode){
            fullTranslateMode = false
            translateOn = false

            Toast.makeText(applicationContext , "번역 OFF" , Toast.LENGTH_SHORT).show()
        }
        else {
            fullTranslateMode = true
            translateOn = true

            cusWebView2.evaluateJavascript(
                "javascript:(function getPTagText(){\n" +
                        "   return window.innerHeight;\n" +
                        "})()"
            ){value ->  innerWindowHeight = value.toInt()
                //Toast.makeText(applicationContext , innerWindowHeight.toString() , Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(applicationContext , "번역 ON" , Toast.LENGTH_SHORT).show()
            cusWebView2.evaluateJavascript(
                "javascript:(function getPTagText55(){\n" +
                        "   var tagP = document.getElementsByTagName('p');\n" +
                        "   return tagP.length;\n" +
                        "})()"
            ){ value ->
                maxTagPIndex = value.toInt()
                tagPIndex = 0
                //oldTagPIndex = 0
                originScrollY = 0
            }
            cusWebView2.evaluateJavascript(
                "javascript:(function getPTagText55(){\n" +
                        "   var tagP = document.getElementsByTagName('Strong');\n" +
                        "   return tagP.length;\n" +
                        "})()"
            ){ value ->
                maxTagStrongIndex = value.toInt()
                tagStrongIndex = 0

            }
            cusWebView2.evaluateJavascript(
                "javascript:(function getPTagText55(){\n" +
                        "   var tagP = document.getElementsByTagName('a');\n" +
                        "   return tagP.length;\n" +
                        "})()"
            ){ value ->
                maxTagAIndex = value.toInt()
                tagAIndex = 0

            }
            cusWebView2.evaluateJavascript(
                "javascript:(function getPTagText55(){\n" +
                        "   var tagP = document.getElementsByTagName('li');\n" +
                        "   return tagP.length;\n" +
                        "})()"
            ){ value ->
                maxTagLiIndex = value.toInt()
                tagLiIndex = 0
            }
        }

        /*
    cusWebView2.evaluateJavascript(
        "javascript:(function getPTagText(){\n" +
                "   var tagP = document.getElementsByTagName('p');\n" +
                "   var cord = tagP[1].textContent\n" +
                "   return cord;\n" +
                "})()"
    ){value ->
        Toast.makeText(applicationContext , "$value" , Toast.LENGTH_SHORT).show()
    } */
        /*
        cusWebView2.evaluateJavascript(
            "javascript:(function getPTagText(){\n" +
                    "   var tagP = document.getElementsByTagName('p');\n" +
                    "   return tagP;\n" +
                    "})()",
        ){value ->
            tagP = value
        }*/
    }
    fun checkTranslate(): Boolean {
        return fullTranslateMode
    }

}