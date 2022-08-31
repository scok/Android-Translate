package com.example.translation.ui.webtranslate

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebSettings
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.example.translation.MainActivity
import com.example.translation.R
import com.example.translation.databinding.FragmentSearchBinding
import com.example.translation.databinding.FragmentTranslateBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    private val binding get() = _binding!!

    @SuppressLint("SetJavaScriptEnabled", "DiscouragedPrivateApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.webAddress.setOnEditorActionListener { textView, i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_SEARCH){
                val loadingUrl = textView.text.toString()
                var result = "https://google.com"
                if (loadingUrl.startsWith("http://") || loadingUrl.startsWith("https://")) {
                    result = loadingUrl
                } else if (loadingUrl.contains("www"))  {
                    result = "http://$loadingUrl"

                } else if (loadingUrl.contains(".com") )  {
                    result = "http://www.$loadingUrl"
                } else if (loadingUrl.isEmpty() )  {
                    result = "https://www.google.com"
                } else {
                    val textComponents = loadingUrl.split(" ")
                    val searchText = textComponents.joinToString("+")
                    result = "https://www.google.com/search?q=$searchText"
                }
                setFragmentResult("requestKey", bundleOf("bundleKey" to result))
                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, TranslateFragment())
                    .commit()
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

        return binding.root
    }
}