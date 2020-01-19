package com.lcj.sb.account.switcher.utils

import android.os.Environment

/**
 * Created by Larry on 2018-06-19.
 */
class Configs {
    companion object {
        val PATH_EXTERNAL_STORAGE: String = Environment.getExternalStorageDirectory().absolutePath
        val PATH_APP_DATA: String = String.format("%s/%s", PATH_EXTERNAL_STORAGE, "Android/data")

        const val PREFIX_NAME_SB_JP: String = "jp.gungho.bm"
        const val PREFIX_NAME_SB_TW: String = "com.ghg.sb"
        const val NAME_ACCOUNT_INFO_FILE: String = "account.json"

        const val PREF_KEY_FIRST_RUN = "first_run"
        const val PREF_KEY_LANGUAGE = "lang"

        const val URL_WEB_SITE_JP = "https://sb.gungho.jp/member/"
        const val URL_WEB_SITE_TW = "http://www.gungho-gamania.com/SB/Bulletins/Bulletin.aspx"
        const val URL_APK_JP = "https://lcj.page.link/7Yoh"

        const val SCREEN_NAME_SB_JP = "SCREEN_SB_JP"
        const val SCREEN_NAME_SB_TW = "SCREEN_SB_TW"

        const val INTENT_KEY_ACCOUNT = "ACCOUNT"
    }
}