package com.example.translation.ui.webtranslate

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.translation.MainActivity
import com.example.translation.R
import com.example.translation.databinding.FragmentTranslateBinding
import com.example.translation.ui.home.DocsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_translate.*
import org.json.JSONObject
import java.net.URLDecoder
import java.util.*


var originalLanguage: String = ""
var targetLanguage: String = ""

class TranslateFragment : Fragment() {

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var customAlertDialogView : View
    private lateinit var nameTextField : TextInputLayout
    private lateinit var favorList : LinkedHashMap<String, String>
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

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null) {
                        if(url.substring(url.length-4) == ".pdf"){
                            //val intent = Intent(requireContext(),DocsActivity::class.java)
                            //startActivity(intent)
                            Log.d("URL-Sample",url.substring(url.length-4))
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }

            binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->

                try{
                    var downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    var _contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8")

                    var _mimetype = mimetype

                    var fileName = _contentDisposition.replace("attachment; filename=","")
                    if(!TextUtils.isEmpty(fileName)){
                        _mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimetype)

                        if(fileName.endsWith(";")){
                            fileName = fileName.substring(0,fileName.length-1)
                        }

                        if(fileName.startsWith("\"") && fileName.endsWith("\"")){
                            fileName = fileName.substring(1,fileName.length-1)
                        }
                    }

                    var request = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(_mimetype)
                        addRequestHeader("User-Agent",userAgent)
                        setDescription("Downloading File")
                        setAllowedOverMetered(true)
                        setAllowedOverRoaming(true)
                        setTitle(fileName)
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            setRequiresCharging(false)
                        }

                        allowScanningByMediaScanner()
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(Environment.getExternalStorageDirectory().path,fileName)
                    }

                    downloadManager.enqueue(request)
                    Toast.makeText(requireContext(),"다운로드 시작중...",Toast.LENGTH_SHORT).show()
                }catch (e : Exception){
                    if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(requireContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(requireActivity(),
                                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                110);
                        } else {
                            Toast.makeText(requireContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                110);
                        }
                    }
                }

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

        binding.webView.loadUrl("https://www.google.com")


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
                if (loadingUrl.startsWith("http://") || loadingUrl.startsWith("https://")) {
                    binding.webView.loadUrl(loadingUrl)
                    binding.urlEdit.setText(loadingUrl)
                } else if (loadingUrl.contains("www"))  {
                    binding.webView.loadUrl("http://$loadingUrl")
                    binding.urlEdit.setText("http://$loadingUrl")
                } else if (loadingUrl.contains(".com") )  {
                    binding.webView.loadUrl("http://www.$loadingUrl")
                    binding.urlEdit.setText("http://www.$loadingUrl")
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
                        Toast.makeText(requireContext(),"메뉴3",Toast.LENGTH_SHORT).show()
                    }
                    R.id.mS -> {
                        Toast.makeText(requireContext(),"메뉴4",Toast.LENGTH_SHORT).show()
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
            (activity as MainActivity).onBackPressed()
        }

        return binding.root
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
