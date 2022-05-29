package com.example.translation.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.translation.MainActivity
import com.example.translation.R
import com.example.translation.pref

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
        trlanPreference = findPreference("tr_lan1")
        trlan2Preference = findPreference("tr_lan2")
        borderthickPreference = findPreference("border_thick")
        borderstylePreference = findPreference("border_style")
        imageTLanguage = findPreference("image_targetLanguage")
        imageExPreference = findPreference("image_extension")

        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity)

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

        if(prefs.getString("image_extension","") != ""){
            imageExPreference?.summary = prefs.getString("image_extension","png")
        }
    }

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
            "image_extension" -> {
                val summary = prefs.getString("image_extension","png")
                imageExPreference?.summary = summary
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

    /*
    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if(preference == listPreference){
            listPreference.summary = newValue.toString()
        }
        else if(preference == listPreference2){
            listPreference2.summary = newValue.toString()
        }
        return true
    }
     */

    override fun onDestroy() {
        super.onDestroy()
    }
}