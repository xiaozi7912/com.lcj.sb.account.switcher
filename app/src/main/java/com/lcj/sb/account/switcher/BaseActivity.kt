package com.lcj.sb.account.switcher

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.utils.SharedPrefs

/**
 * Created by Larry on 2018-06-18.
 */
open class BaseActivity : AppCompatActivity() {
    val LOG_TAG: String = javaClass.simpleName
    val mActivity: Activity = this
    var mHandler: Handler = Handler()

    protected lateinit var mAuth: FirebaseAuth
    protected lateinit var mCurrentLang: Account.Language

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mCurrentLang = SharedPrefs.getInstance(mActivity).getCurrentLang()
    }

    override fun onStart() {
        super.onStart()
        initView()
    }

    protected open fun initView() {
        Log.i(LOG_TAG, "initView")
    }
}