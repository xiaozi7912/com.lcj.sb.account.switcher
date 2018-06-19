package com.lcj.sb.account.switcher.utils

import android.util.Log
import org.json.JSONObject
import java.io.*

/**
 * Created by Larry on 2018-06-19.
 */
class AccountInfoManager() {
    val LOG_TAG: String = javaClass.simpleName

    var mCurrentAccount: String? = null
        get() {
            Log.i(LOG_TAG, "mCurrentAccount get")
            return field
        }
        set(value) {
            Log.i(LOG_TAG, "mCurrentAccount set")
            Log.v(LOG_TAG, "mCurrentAccount set value : " + value)
            field = value
        }

    companion object {
        var mInstance: AccountInfoManager? = null

        fun getInstance(): AccountInfoManager {
            if (mInstance == null) mInstance = AccountInfoManager()
            return mInstance!!
        }
    }

    fun readAccountInfoFile() {
        Log.i(LOG_TAG, "readAccountInfoFile")
        var accountFile = File(String.format("%s/%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB, Configs.NAME_ACCOUNT_INFO_FILE))
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
            mCurrentAccount = jsonObject.optString("account")

            reader.close()
        } else {
            mCurrentAccount = "No Account"
        }
    }

    fun writeAccountInfoFile() {
        Log.i(LOG_TAG, "writeAccountInfoFile")
        var accountFile = File(String.format("%s/%s/%s", Configs.PATH_APP_DATA, Configs.PREFIX_NAME_SB, Configs.NAME_ACCOUNT_INFO_FILE))
        Log.v(LOG_TAG, "writeAccountInfoFile accountFile.absolutePath : " + accountFile.absolutePath)
        Log.v(LOG_TAG, "writeAccountInfoFile accountFile.exists : " + accountFile.exists())
        var jsonObject = JSONObject()

        jsonObject.put("account", mCurrentAccount)

        var writer = BufferedWriter(FileWriter(accountFile))
        writer.write(jsonObject.toString())
        writer.flush()
        writer.close()
    }

}