package com.lcj.sb.account.switcher.utils

import android.util.Log
import org.json.JSONObject
import java.io.*

/**
 * Created by Larry on 2018-06-19.
 */
class AccountInfoManager() {
    val LOG_TAG: String = javaClass.simpleName

    val KEY_TAB_TYPE: String = "TAB_TYPE"
    val KEY_ACCOUNT_TYPE_JP: String = "ACCOUNT_JP"
    val KEY_ACCOUNT_TYPE_TW: String = "ACCOUNT_TW"
    val VALUE_ACCOUNT_TYPE_DEFAULT = "No Account"

    var currentTab: Int = TAB_TYPE_JP
        get() {
            Log.i(LOG_TAG, "currentTab get")
            return field
        }
        set(value) {
            Log.i(LOG_TAG, "currentTab set")
            Log.v(LOG_TAG, "currentTab set value : " + value)
            field = value
        }
    var currentJPAccount: String? = null
        get() {
            Log.i(LOG_TAG, "currentJPAccount get")
            return field
        }
        set(value) {
            Log.i(LOG_TAG, "currentJPAccount set")
            Log.v(LOG_TAG, "currentJPAccount set value : " + value)
            field = value
        }
    var currentTWAccount: String? = null
        get() {
            Log.i(LOG_TAG, "currentTWAccount get")
            return field
        }
        set(value) {
            Log.i(LOG_TAG, "currentTWAccount set")
            Log.v(LOG_TAG, "currentTWAccount set value : " + value)
            field = value
        }

    companion object {
        private var mInstance: AccountInfoManager? = null

        val TAB_TYPE_JP: Int = 1
        val TAB_TYPE_TW: Int = 2

        fun getInstance(): AccountInfoManager {
            if (mInstance == null) mInstance = AccountInfoManager()
            return mInstance!!
        }
    }

    fun readAccountInfoFile() {
        Log.i(LOG_TAG, "readAccountInfoFile")
        var accountFile = File(String.format("%s/%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW, Configs.NAME_ACCOUNT_INFO_FILE))
        Log.v(LOG_TAG, "readAccountInfoFile accountFile.absolutePath : " + accountFile.absolutePath)
        Log.v(LOG_TAG, "readAccountInfoFile accountFile.exists : " + accountFile.exists())

        if (accountFile.exists()) {
            var reader = BufferedReader(FileReader(accountFile))
            var strBuilder = StringBuilder()
            var readLine: String? = null

            do {
                readLine = reader.readLine()
                Log.v(LOG_TAG, "readAccountInfoFile readLine : " + readLine)
                if (readLine != null) strBuilder.append(readLine)
            } while (readLine != null)

            var jsonObject = JSONObject(strBuilder.toString())

            currentTab = jsonObject.optInt(KEY_TAB_TYPE, TAB_TYPE_JP)
            currentJPAccount = jsonObject.optString(KEY_ACCOUNT_TYPE_JP, VALUE_ACCOUNT_TYPE_DEFAULT)
            currentTWAccount = jsonObject.optString(KEY_ACCOUNT_TYPE_TW, VALUE_ACCOUNT_TYPE_DEFAULT)
            reader.close()
        } else {
            currentTWAccount = VALUE_ACCOUNT_TYPE_DEFAULT
            currentJPAccount = VALUE_ACCOUNT_TYPE_DEFAULT
        }
    }

    fun writeAccountInfoFile() {
        Log.i(LOG_TAG, "writeAccountInfoFile")
        var accountFile = File(String.format("%s/%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB_TW, Configs.NAME_ACCOUNT_INFO_FILE))
        Log.v(LOG_TAG, "writeAccountInfoFile accountFile.absolutePath : " + accountFile.absolutePath)
        Log.v(LOG_TAG, "writeAccountInfoFile accountFile.exists : " + accountFile.exists())
        var jsonObject = JSONObject()

        jsonObject.put(KEY_TAB_TYPE, currentTab)
        jsonObject.put(KEY_ACCOUNT_TYPE_JP, currentJPAccount)
        jsonObject.put(KEY_ACCOUNT_TYPE_TW, currentTWAccount)

        var writer = BufferedWriter(FileWriter(accountFile))
        writer.write(jsonObject.toString())
        writer.flush()
        writer.close()
    }

}