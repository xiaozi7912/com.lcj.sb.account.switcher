package com.lcj.sb.account.switcher.utils

import android.os.Environment

/**
 * Created by Larry on 2018-06-19.
 */
class Configs {
    companion object {
        val PATH_EXTERNAL_STORAGE: String = Environment.getExternalStorageDirectory().absolutePath
        val PATH_APP_DATA: String = String.format("%s/%s", PATH_EXTERNAL_STORAGE, "Android/data")
        val PREFIX_NAME_SB: String = "com.ghg.sb"
        val NAME_ACCOUNT_INFO_FILE: String = "account.json"
    }
}