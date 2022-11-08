package com.example.translation.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.translation.MainActivity
import com.example.translation.R
import com.example.translation.pref

// 앱 설정 클래스
class SettingFragment : PreferenceFragmentCompat() {
    lateinit var mainActivity: MainActivity
    lateinit var prefs : SharedPreferences
    var trlanPreference : Preference? = null
    var trlan2Preference : Preference? = null
    var borderthickPreference : Preference? = null
    var borderstylePreference : Preference? = null
    var imageTLanguage : Preference? = null
    var imageExPreference : Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_preference,rootKey)

        mainActivity = context as MainActivity
        trlanPreference = findPreference("tr_lan1") // 대상 언어
        trlan2Preference = findPreference("tr_lan2") // 번역 언어
        borderthickPreference = findPreference("border_thick") // 테두리 굵기
        borderstylePreference = findPreference("border_style") // 테두리 스타일
        imageTLanguage = findPreference("image_targetLanguage") // 이미지 번역 언어

        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        
        // 각 설정 값 초기 설정
        
        if(prefs.getString("tr_lan1","") != ""){
            trlanPreference?.summary = prefs.getString("tr_lan1","영어")
        }

        if(prefs.getString("tr_lan2","") != ""){
            trlan2Preference?.summary = prefs.getString("tr_lan2","한국어")
        }

        if(prefs.getString("border_thick", "") != ""){
            borderthickPreference?.summary = prefs.getString("border_thick","1px")
        }

        if(prefs.getString("border_style","") != ""){
            borderstylePreference?.summary = prefs.getString("border_style","solid")
        }

        if(prefs.getString("image_targetLanguage","") != ""){
            imageTLanguage?.summary = prefs.getString("image_targetLanguage","en")
        }

    }

    // 클릭한 값에 따라 설정 값 반영
    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s ->
        when(s){
            "tr_lan1" -> {
                val summary = prefs.getString("tr_lan1","영어")
                trlanPreference?.summary = summary
            }
            "tr_lan2" -> {
                val summary = prefs.getString("tr_lan2","한국어")
                trlan2Preference?.summary = summary
            }
            "border_thick" -> {
                val summary = prefs.getString("border_thick","1px")
                borderthickPreference?.summary = summary
            }
            "border_style" -> {
                val summary = prefs.getString("border_style","solid")
                borderstylePreference?.summary = summary
            }
            "image_targetLanguage" -> {
                val summary = prefs.getString("image_targetLanguage","en")
                imageTLanguage?.summary = summary
            }
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}