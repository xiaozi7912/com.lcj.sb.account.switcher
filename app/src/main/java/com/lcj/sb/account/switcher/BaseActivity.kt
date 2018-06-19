package com.lcj.sb.account.switcher

import android.app.Activity
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * Created by Larry on 2018-06-18.
 */
open class BaseActivity : AppCompatActivity() {
    val LOG_TAG: String = javaClass.simpleName
    val mActivity: Activity = this
    var mHandler: Handler = Handler()

    protected open fun initView() {
        Log.i(LOG_TAG, "initView")
    }
}