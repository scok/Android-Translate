package com.example.translation.ui.webtranslate

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.translation.*
import com.example.translation.databinding.FragmentTranslateBinding
import kotlinx.android.synthetic.main.fragment_translate.*


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

            binding.webView.webViewClient = object : WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.urlEdit.setText(view!!.url)
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

        binding.webMenu.setOnClickListener { v ->
            val popup: PopupMenu = PopupMenu(requireContext(), v)
            (activity as MainActivity).menuInflater.inflate(R.menu.web_option, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.m1 -> {
                        Toast.makeText(requireContext(),"메뉴1",Toast.LENGTH_SHORT).show()
                    }
                    R.id.m2 -> {
                        Toast.makeText(requireContext(),"메뉴2",Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(),"에러",Toast.LENGTH_SHORT).show()
                    }
                }
                false
            }

            popup.show()

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

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val items = (activity as MainActivity).resources.getStringArray(R.array.my_array)
        val myAdapter = ArrayAdapter(
            context as MainActivity,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )

        orginalSpinner.adapter = myAdapter
        orginalSpinner.setSelection(0)
        orginalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0 -> {
                        originalLanguage = "en".trim()
                    }
                    1 -> {
                        originalLanguage = "ko".trim()
                    }
                    2 -> {
                        originalLanguage = "ja".trim()
                    }
                    3 -> {
                        originalLanguage = "zh".trim()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        targetSpinner.adapter = myAdapter
        targetSpinner.setSelection(1)
        targetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                when (p2) {
                    0 -> {
                        targetLanguage = "en".trim()
                    }
                    1 -> {
                        targetLanguage = "ko".trim()
                    }
                    2 -> {
                        targetLanguage = "ja".trim()
                    }
                    3 -> {
                        targetLanguage = "zh-CN".trim()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).supportActionBar?.show()
        _binding = null
    }
}