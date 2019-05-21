package com.example.a20gosu_proj

import android.content.Context
import android.content.SharedPreferences

class LangPreference {
    companion object {
        var country: String? =null
        fun setLangText(lang:String){
            LangPreference.country=lang
        }
        fun getLangText(): String? {
            return country
        }
    }
}