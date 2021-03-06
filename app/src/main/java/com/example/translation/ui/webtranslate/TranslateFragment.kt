package com.example.translation.ui.webtranslate

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.preference.PreferenceManager
import com.example.translation.MainActivity
import com.example.translation.R
import com.example.translation.databinding.FragmentTranslateBinding
import com.example.translation.ui.home.DocsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_translate.*
import org.json.JSONObject
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

                    if(favorList.containsKey(view!!.url)){ // ?????????????????? ?????? url??? ???????????? ????????? ???
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                        //web_bookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                    }
                    else{
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_border_black_24dp) // ??????
                    }
                    if ((activity as MainActivity).checkTranslate()){(activity as MainActivity).translateToggle()}
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.urlEdit.setText(view!!.url)
                    binding.progressHorizontal.visibility = View.INVISIBLE

                    if(favorList.containsKey(view!!.url)){ // ?????????????????? ?????? url??? ???????????? ????????? ???
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                        //web_bookmark.setImageResource(R.drawable.baseline_star_black_24dp)
                    }
                    else{
                        binding.webBookmark.setImageResource(R.drawable.baseline_star_border_black_24dp) // ??????
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

                            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val file_dir = file.path + "/pdf_temp/$file_name"

                            val intent = Intent(requireContext(),DocsActivity::class.java)
                            intent.putExtra("pdf_dir",file_dir)
                            intent.putExtra("pdf_name",file_name)
                            intent.putExtra("pdf_names",file_names)
                            startActivity(intent)
                        }
                    }
                    return super.shouldOverrideUrlLoading(view, url)
                }
            }

            binding.webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                try{
                    val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                    val _contentDisposition = URLDecoder.decode(contentDisposition,"UTF-8")

                    var _mimetype = mimetype

                    val url_arr = url.split("/")
                    Log.d("Down-Sample", url_arr[url_arr.size-1])

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

                    val request = DownloadManager.Request(Uri.parse(url)).apply {
                        setMimeType(_mimetype)
                        addRequestHeader("User-Agent",userAgent)
                        setDescription("Downloading File")
                        setAllowedOverMetered(true)
                        setAllowedOverRoaming(true)
                        setTitle(url_arr[url_arr.size-1])
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            setRequiresCharging(false)
                        }

                        allowScanningByMediaScanner()
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/pdf_temp/",url_arr[url_arr.size-1]
                        )
                    }

                    downloadManager.enqueue(request)
                    Toast.makeText(requireContext(),"???????????? ?????????...",Toast.LENGTH_SHORT).show()
                    //MOVE_FILE(requireContext(),Environment.DIRECTORY_DOWNLOADS+"/pdf_temp/",url_arr[url_arr.size-1],)
                    Log.d("Down-Sample",url)
                }catch (e : Exception){
                    if(ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(requireContext(), "???????????? ??????????????? ??????\n????????? ???????????????.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(requireActivity(),
                                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                110);
                        } else {
                            Toast.makeText(requireContext(), "???????????? ??????????????? ??????\n????????? ???????????????.", Toast.LENGTH_LONG).show();
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

        setFragmentResultListener("requestKey") { requestKey, bundle ->
            result = bundle.getString("bundleKey").toString()
            Log.d("URL-Sample",result)
            binding.webView.loadUrl(result)
        }

        materialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        binding.webBookmark.setOnClickListener {

            if(favorList.containsKey(binding.urlEdit.text.toString())){ // ?????????????????? ?????? url??? ???????????? ???????????????
                binding.webBookmark.setImageResource(R.drawable.baseline_star_border_black_24dp) // ??????
                favorList.remove(binding.urlEdit.text.toString())
                saveBookmark(favorList)

            }
            else{ // ?????? Url ?????? ????????? ?????? ??? ??? ??????
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
                        Toast.makeText(requireContext(),"??????3",Toast.LENGTH_SHORT).show()
                    }
                    R.id.mS -> {
                        Toast.makeText(requireContext(),"??????4",Toast.LENGTH_SHORT).show()
                    }
                    R.id.mT -> {
                        val img_url = binding.webView.url
                        Log.d("Img-Sample",img_url.toString())
                        if (img_url != null) {
                            if(img_url.lowercase(Locale.getDefault()).endsWith(".jpg") || img_url.lowercase(Locale.getDefault())
                                    .endsWith(".png")){

                                val request = DownloadManager.Request(Uri.parse(img_url))
                                request.allowScanningByMediaScanner()

                                request.setNotificationVisibility(
                                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                                val filename = img_url.split("/")
                                request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS+"/",
                                    filename[filename.size-1]
                                )
                                val dm = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                dm.enqueue(request)
                            }
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

            // ?????? ???????????? ??????
            File(inputPath + inputFile).delete()

            // ?????? ????????? ????????? , ?????? ?????? ????????? ?????? ????????? ?????????
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
            "?????????" -> {
                originalLanguage = "ko"
            }
            "??????" -> {
                originalLanguage = "en"
            }
            "?????????" -> {
                originalLanguage = "ja"
            }
            "?????????" -> {
                originalLanguage = "zh"
            }
        }

        val tempLan2 = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString("tr_lan2","").toString()

        when (tempLan2) {
            "?????????" -> {
                targetLanguage = "ko"
            }
            "??????" -> {
                targetLanguage = "en"
            }
            "?????????" -> {
                targetLanguage = "ja"
            }
            "?????????" -> {
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
            .setMessage("?????? URL : ${urlEdit.text}")
            .setTitle("??????????????? ??????")
            .setPositiveButton("??????") { dialog, _ ->
                val name = nameTextField.editText?.text.toString()
                /**
                 * Do as you wish with the data here --
                 * Download/Clone the repo from my Github to see the entire implementation
                 * using the link provided at the end of the article.
                 */
                favorList[urlEdit.text.toString()] = name
                binding.webBookmark.setImageResource(R.drawable.baseline_star_black_24dp) // ?????????
                saveBookmark(favorList)
                dialog.dismiss()
            }
            .setNegativeButton("??????") { dialog, _ ->
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
