package com.lcj.sb.account.switcher.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.lcj.sb.account.switcher.database.entity.Account

class SharedPrefs(context: Context) {
    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private var LOG_TAG = javaClass.simpleName
        private var instance: SharedPrefs? = null

        fun getInstance(context: Context): SharedPrefs {
            if (instance == null) {
                instance = SharedPrefs(context)
            }
            return instance!!
        }
    }

    fun getCurrentLang(): Account.Language {
        return Account.Language.valueOf(mPrefs.getString("lang", "JP")!!)
    }

    fun setCurrentLang(lang: Account.Language) {
        mPrefs.edit().putString("lang", lang.name).apply()
    }
}