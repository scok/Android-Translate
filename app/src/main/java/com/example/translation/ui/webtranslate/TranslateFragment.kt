package com.example.translation.ui.webtranslate

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.preference.PreferenceManager
import com.example.translation.MainActivity
import com.example.translation.R
import com.example.translation.databinding.FragmentTranslateBinding
import com.example.translation.ui.home.DocsActivity
import com.example.translation.ui.home.PapagoEntity
import com.example.translation.ui.home.PapagoService
import com.example.translation.ui.home.TImageActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_image_viewer_t.*
import kotlinx.android.synthetic.main.fragment_translate.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.net.URLDecoder
import java.util.*


var originalLanguage: String = ""
var targetLanguage: String = ""

class TranslateFragment : Fragment() {

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var customAlertDialogView : View
    private lateinit var nameTextField : TextInputLayout
    private lateinit var favorList : LinkedHashMap<String, String>
    private var result : String = ""
    private var donwloadid : Long? = null

    private var _binding: FragmentTranslateBinding? = null
    private lateinit var webSettings: WebSettings

    private val binding get() = _binding!!

    @SuppressLint("SetJavaScriptEnabled", "DiscouragedPrivateApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslateBinding.inflate(inflater, container, false)

        (activity as MainActivity).supportActionBar?.hide()

        setHasOptionsMenu(true)

        favorList = LinkedHashMap<String, String>()

        binding.webView.apply {
            webSettings = binding.webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true

            binding.webView.webChromeClient = object : WebChromeClient(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressHorizontal.progress = newProgress
                }
            }
            favorList= loadBookmark() as LinkedHashMap<String, String>
            binding.webView.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressHorizontal.visibility = View.VISIBLE

                    if(favorList.containsKey(view!!.url)){ // 북마크에이미 해당 url이 존재하면 칠해진 별
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                        //web_bookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                    }
                    else{
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_border_black_24dp) // 빈별
                    }
                    if ((activity as MainActivity).checkTranslate()){(activity as MainActivity).translateToggle()}
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.urlEdit.setText(view!!.url)
                    binding.progressHorizontal.visibility = View.INVISIBLE

                    if(favorList.containsKey(view!!.url)){ // 북마크에이미 해당 url이 존재하면 칠해진 별
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                        //web_bookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                    }
                    else{
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_border_black_24dp) // 빈별
                    }
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                }

                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    return super.shouldInterceptRequest(view, request)
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null) {
                        if(url.substring(url.length-4) == ".pdf"){

                            val url_arr = url.split("/")
                            val file_name = url_arr[url_arr.size-1]
                            val file_names = file_name.substring(0,file_name.length-4)

                            val file_dir = "${(activity as MainActivity).externalCacheDir}/$file_name"

                            val intent = Intent(requireContext(),DocsActivity::class.java)
                            intent.putExtra("pdf_url",url)
                            intent.putExtra("pdf_dir",file_dir)
                            intent.putExtra("pdf_name",file_name)
                            intent.putExtra("pdf_names",file_names)
                            startActivity(intent)
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }

            binding.webView.setOnLongClickListener { v ->
                val hr : WebView.HitTestResult = (v as WebView).hitTestResult
                Log.d("Test-Webview","getExtra="+hr.extra+"\t\tType="+hr.type)
                if(hr.type == WebView.HitTestResult.IMAGE_TYPE){
                    val image_url = hr.extra
                    if(image_url!!.lastIndexOf("png") != -1){
                        val url_arr = image_url.split("/")
                        val file_name = url_arr[url_arr.size-1]

                        val intent = Intent(requireContext(),TImageActivity::class.java)
                        intent.putExtra("image_url",image_url)
                        intent.putExtra("image_name",file_name)
                        startActivity(intent)
                    }
                }
                return@setOnLongClickListener false
            }


            if(Build.VERSION.SDK_INT >= 19){
                binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE,null)
            }else{
                binding.webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE,null)
            }
            (activity as MainActivity).window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )

            webSettings.setSupportMultipleWindows(true)
            webSettings.javaScriptCanOpenWindowsAutomatically = true
            webSettings.loadWithOverviewMode = true
            webSettings.useWideViewPort = true
            webSettings.setSupportZoom(false)
            webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            webSettings.domStorageEnabled = true
            webSettings.safeBrowsingEnabled = true
            webSettings.mediaPlaybackRequiresUserGesture = false
            webSettings.allowContentAccess = true
            webSettings.setGeolocationEnabled(true)
            webSettings.allowUniversalAccessFromFileURLs = true
            webSettings.allowFileAccess = true
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            webSettings.setEnableSmoothTransition(true)

            fitsSystemWindows = true

        }

        binding.webView.clearCache(true)
        binding.webView.clearHistory()

        setFragmentResultListener("requestKey") { requestKey, bundle ->
            result = bundle.getString("bundleKey").toString()
            Log.d("URL-Sample",result)
            binding.webView.loadUrl(result)
        }

        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        binding.webBookmark.setOnClickListener {

            if(favorList.containsKey(binding.urlEdit.text.toString())){ // 북마크에이미 해당 url이 존재하면 북마크삭제
                binding.webBookmark.setImageResource(R.drawable.baseline_star_border_black_24dp) // 빈별
                favorList.remove(binding.urlEdit.text.toString())
                saveBookmark(favorList)

            }
            else{ // 신규 Url 이면 북마크 추가 후 별 색칠
                customAlertDialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.fragment_blank,null,false)
                launchCustomAlertDialog()
            }

        }



        binding.urlEdit.setOnEditorActionListener { textView, i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_SEARCH){
                val loadingUrl = textView.text.toString()
                val domains = arrayOf(".org",".net",".edu",".gov",".mil",
                    ".kr",".jp",".us",".cn")
                if (loadingUrl.startsWith("http://") || loadingUrl.startsWith("https://")) {
                    binding.webView.loadUrl(loadingUrl)
                    binding.urlEdit.setText(loadingUrl)
                } else if (loadingUrl.startsWith("www."))  {
                    binding.webView.loadUrl("http://$loadingUrl")
                    binding.urlEdit.setText("http://$loadingUrl")
                } else if (loadingUrl.endsWith(".com") )  {
                    binding.webView.loadUrl("http://www.$loadingUrl")
                    binding.urlEdit.setText("http://www.$loadingUrl")
                } else if (loadingUrl.contains("."))   {
                    domains.forEach {
                        if (loadingUrl.endsWith(it)){
                            binding.webView.loadUrl("http://www.$loadingUrl")
                            binding.urlEdit.setText("http://www.$loadingUrl")
                        }
                    }
                } else {
                    val textComponents = loadingUrl.split(" ")
                    val searchText = textComponents.joinToString("+")
                    binding.webView.loadUrl("https://www.google.com/search?q=$searchText")
                    binding.urlEdit.setText("https://www.google.com/search?q=$searchText")
                }

                /*
                val loadingUrl = textView.text.toString()

                if(URLUtil.isNetworkUrl(loadingUrl)){
                    binding.webView.loadUrl(loadingUrl)
                    binding.urlEdit.setText(loadingUrl)
                }else{
                    binding.webView.loadUrl("http://$loadingUrl")
                    binding.urlEdit.setText("http://$loadingUrl")
                }*/
            }

            return@setOnEditorActionListener false
        }

        binding.webMenu.setOnClickListener { v ->
            val context = ContextThemeWrapper(requireContext(),R.style.popup)
            val popup: PopupMenu = PopupMenu(context, v)
            (activity as MainActivity).menuInflater.inflate(R.menu.web_option, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.mP -> {
                        //Toast.makeText(requireContext(),"메뉴3",Toast.LENGTH_SHORT).show()
                       // (activity as MainActivity)
                        val trasnlateScreenShotCachePath = (activity as MainActivity).translateScreenshot().toString()
                        //Toast.makeText(requireContext(),trasnlateScreenShotCachePath,Toast.LENGTH_SHORT).show()

                        val clientBuilder : OkHttpClient.Builder = OkHttpClient.Builder()
                        val loggingInterceptor : HttpLoggingInterceptor = HttpLoggingInterceptor()
                        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                        clientBuilder.addInterceptor(loggingInterceptor)

                        val client : Retrofit = Retrofit.Builder()
                            .baseUrl("https://naveropenapi.apigw.ntruss.com")
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(clientBuilder.build())
                            .build()

                        val file = File(trasnlateScreenShotCachePath)
                        val service = client.create(PapagoService::class.java)

                        val partMap = HashMap<String, RequestBody>()
                        val trsource = RequestBody.create("text/plain".toMediaTypeOrNull() , "en")
                        val trtarget = RequestBody.create("text/plain".toMediaTypeOrNull() , "ko")
                        partMap["source"] = trsource
                        partMap["target"] = trtarget

                        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull() , file)
                        val body : MultipartBody.Part = MultipartBody.Part.createFormData("image",file.name,requestFile)

                      //  Log.d("Test-Sample","forei : $i")
                       // Log.d("Test-Sample","forej : $j")

                        service.transferPapago("w5lgfrssck","tct9yx0oteeuixAnAdIOETTtKiZFhixSLzNw3vvM",body,partMap)
                            .enqueue(object: Callback<PapagoEntity> {
                                override fun onResponse(
                                    call: Call<PapagoEntity> ,
                                    response: Response<PapagoEntity>
                                ) {
                                    if(response.isSuccessful){
                                        Log.d("Test","번역 완료")
                                        val imageStr = response.body()?.data?.renderedImage.toString()
                                        val bytePlainOrg = Base64.decode(imageStr,0)
                                        val inStream : ByteArrayInputStream = ByteArrayInputStream(bytePlainOrg)
                                        val bm : Bitmap = BitmapFactory.decodeStream(inStream)
                                        Log.d("Test-Sample","bm : $bm\n")

                                        // 파일 저장
                                        val savePath = "${context.cacheDir}-translate.jpg"
                                        val filePath = File(savePath)

                                        try {
                                            val fos = FileOutputStream(filePath)
                                            bm?.compress(Bitmap.CompressFormat.JPEG, 90, fos)

                                            fos.close()
                                            Log.d("Test","savePath: ${savePath}")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                       // trimageView2.setImageBitmap(bm)

                                        val intent = Intent((activity as MainActivity),ImageViewerT::class.java)
                                        //intent.putExtra("favor_list", favorList) as LinkedHashMap<String, String>
                                        //intent.putExtra("favor_list", favorList)
                                        // startActivity(intent)
                                        startActivity(intent)

                                    }else{
                                        Log.e("Test","번역 에러")
                                    }
                                }

                                override fun onFailure(call: Call<PapagoEntity> , t: Throwable) {
                                    Log.e("Test","통신 에러")
                                }
                            })


                    }
                    R.id.mS -> {
                        Toast.makeText(requireContext(),"메뉴4",Toast.LENGTH_SHORT).show()
                        (activity as MainActivity).openSettingFragment()
                            //미완성
                    }
                    R.id.mT -> {
                        val imgUrl = binding.webView.url
                        Log.d("Img-Sample",imgUrl.toString())
                        if (imgUrl != null && imgUrl.lastIndexOf("imgrc") != -1 ) {
                            val intent = Intent(requireContext(),TImageActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    else -> {
                    }
                }
                false
            }

            popup.show()

        }

        binding.webBack.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                (activity as MainActivity).onBackPressed()
            }
        }

        binding.webForward.setOnClickListener {
            if (binding.webView.canGoForward()) {
                binding.webView.goForward()
            }
        }

        binding.webRefresh.setOnClickListener {
            binding.webView.reload()
        }

        binding.webFavorites.setOnClickListener {
            val intent = Intent(requireContext(),FavoritesActivity::class.java)
            val items : ArrayList<String> = ArrayList(favorList.values)
            intent.putExtra("favor_list", items)
            //intent.putExtra("favor_list", favorList) as LinkedHashMap<String, String>
            //intent.putExtra("favor_list", favorList)
           // startActivity(intent)
            startActivityForResult(intent,REQUEST_RESULT)
        }
        binding.webTranslate.setOnClickListener {
            (activity as MainActivity).translateToggle()
        }

        binding.webExit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, SearchFragment())
                .commit()
        }

        return binding.root
    }

    fun MOVE_FILE(context: Context, inputPath: String, inputFile: String, outputPath: String) {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            val dir = File(outputPath)
            Log.e("dir", dir.path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            Log.e("MOVE_FILE", outputPath + "/" + inputFile + "______" + dir.path)
            `in` = FileInputStream(inputPath + inputFile)
            out = FileOutputStream("$outputPath/$inputFile")
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null
            out.flush()
            out.close()
            out = null

            // 기존 원본파일 삭제
            File(inputPath + inputFile).delete()

            // 파일 미디어 동기화 , 사진 혹은 동영상 파일 갤러리 동기화
            val tmp_file = File("$outputPath/$inputFile")
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(tmp_file)
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val tempLan = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString("tr_lan1","").toString()

        when (tempLan) {
            "한국어" -> {
                originalLanguage = "ko"
            }
            "영어" -> {
                originalLanguage = "en"
            }
            "일본어" -> {
                originalLanguage = "ja"
            }
            "중국어" -> {
                originalLanguage = "zh"
            }
        }

        val tempLan2 = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString("tr_lan2","").toString()

        when (tempLan2) {
            "한국어" -> {
                targetLanguage = "ko"
            }
            "영어" -> {
                targetLanguage = "en"
            }
            "일본어" -> {
                targetLanguage = "ja"
            }
            "중국어" -> {
                targetLanguage = "zh"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).supportActionBar?.show()
        _binding = null
    }

    private fun launchCustomAlertDialog() {
        nameTextField = customAlertDialogView.findViewById(R.id.name_text_field)
        binding.webView.evaluateJavascript(
            "javascript:(function getTitle(){\n" +
                    "   var tagTitle = document.getElementsByTagName('title');\n" +
                    "   var textString = tagTitle[0].textContent; \n" +
                    "   return textString;\n" +
                    "})()"
        ){ value ->
            nameTextField.editText?.setText(value?.substring(1, value.toString().length-1) ?: " ")
        }
        // Building the Alert dialog using materialAlertDialogBuilder instance
        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setMessage("저장 URL : ${urlEdit.text}")
            .setTitle("즐겨찾기에 추가")
            .setPositiveButton("추가") { dialog, _ ->
                val name = nameTextField.editText?.text.toString()
                /**
                 * Do as you wish with the data here --
                 * Download/Clone the repo from my Github to see the entire implementation
                 * using the link provided at the end of the article.
                 */
                favorList[urlEdit.text.toString()] = name
                binding.webBookmark.setImageResource(R.drawable.baseline_star_black_24dp) // 별추가
                saveBookmark(favorList)
                dialog.dismiss()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun displayMessage(message : String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun saveBookmark(inputMap: LinkedHashMap<String, String>){
        val sharedPreferences = (activity as MainActivity).applicationContext.getSharedPreferences(
            "BookMark_sharedPreferences", Context.MODE_PRIVATE
        )
        val jsonObject = JSONObject(inputMap as LinkedHashMap<*,*>)
        val jsonString = jsonObject.toString()
        val editor = sharedPreferences.edit()
        editor.remove("map").commit()
        editor.putString("map", jsonString)
        editor.commit()
    }


    private fun loadBookmark():LinkedHashMap<String,String>{
        val outputMap = LinkedHashMap<String,String>()
        val pSharedPref = (activity as MainActivity).applicationContext.getSharedPreferences(
            "BookMark_sharedPreferences", Context.MODE_PRIVATE)

        try {
            if (pSharedPref != null) {
                val jsonString = pSharedPref.getString("map", JSONObject().toString())
                val jsonObject = JSONObject(jsonString)
                val keysItr = jsonObject.keys()
                while (keysItr.hasNext()) {
                    val key = keysItr.next()
                    val value = jsonObject[key] as String
                    outputMap[key] = value
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return outputMap as LinkedHashMap<String, String>
    }
    companion object {
        private const val REQUEST_RESULT = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // If you have multiple activities returning results then you should include unique request codes for each
        if (requestCode == REQUEST_RESULT) {
            // The result code from the activity started using startActivityForResults
            if (resultCode == Activity.RESULT_OK && data != null) {
                val favorIndex = data.getIntExtra("favor_index",9999)
                val favorURL = ArrayList(favorList.keys)[favorIndex]
                binding.webView.loadUrl(favorURL)
                binding.urlEdit.setText(favorURL)
                //val value: String = ArrayList<String>(favorList.values)[favorIndex]
                //Toast.makeText(activity as MainActivity,favorURL.toString(),Toast.LENGTH_LONG).show()
            }
            else{
                //fail
            }
        }
    }
}
