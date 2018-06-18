package com.lcj.sb.account.switcher

import android.app.Activity
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * Created by Larry on 2018-06-18.
 */
open class BaseActivity : AppCompatActivity() {
    val LOG_TAG: String = javaClass.simpleName
    val mActivity: Activity = this

    companion object {
        val PATH_EXTERNAL_STORAGE: String = Environment.getExternalStorageDirectory().absolutePath
        val PATH_APP_DATA: String = String.format("%s/%s", PATH_EXTERNAL_STORAGE, "Android/data")
        val PREFIX_NAME_SB: String = "com.ghg.sb"
    }

    protected open fun initView() {
        Log.i(LOG_TAG, "initView")
    }
}