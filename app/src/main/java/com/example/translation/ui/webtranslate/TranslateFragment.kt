package com.example.translation.ui.webtranslate

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.translation.*
import com.example.translation.databinding.FragmentTranslateBinding


var originalLanguage: String = ""
var targetLanguage: String = ""

class TranslateFragment : Fragment() {

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

        binding.webView.apply {
            webSettings = binding.webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true

            binding.webView.webChromeClient = object : WebChromeClient(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressHorizontal.progress = newProgress
                }
            }

            binding.webView.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressHorizontal.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.urlEdit.setText(view!!.url)
                    binding.progressHorizontal.visibility = View.INVISIBLE
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

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
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

        binding.webBookmark.setOnClickListener {

        }

        binding.urlEdit.setOnEditorActionListener { textView, i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_SEARCH){
                val loadingUrl = textView.text.toString()

                if(URLUtil.isNetworkUrl(loadingUrl)){
                    binding.webView.loadUrl(loadingUrl)
                    binding.urlEdit.setText(loadingUrl)
                }else{
                    binding.webView.loadUrl("http://$loadingUrl")
                    binding.urlEdit.setText("http://$loadingUrl")
                }
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

        }

        binding.webTranslate.setOnClickListener {

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
}