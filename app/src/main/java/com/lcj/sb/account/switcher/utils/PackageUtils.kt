package com.lcj.sb.account.switcher.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

class PackageUtils(val context: Context) {
    private var LOG_TAG = javaClass.simpleName
    private var mManager = context.packageManager

    companion object {
        private var instance: PackageUtils? = null

        fun getInstance(context: Context): PackageUtils {
            if (instance == null) instance = PackageUtils(context)
            return instance!!
        }
    }

    fun getVersionCode(packageName: String): Long {
        Log.i(LOG_TAG, "getVersionCode")
        try {
            val packageInfo = mManager.getPackageInfo(packageName, 0)
            return packageInfo.versionCode.toLong()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getVersionName(packageName: String): String? {
        Log.i(LOG_TAG, "getVersionName")
        try {
            val packageInfo = mManager.getPackageInfo(packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }
}