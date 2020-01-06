package com.lcj.sb.account.switcher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.utils.Configs

/**
 * Created by Larry on 2018-06-18.
 */
abstract class BaseActivity : AppCompatActivity() {
    val LOG_TAG: String = javaClass.simpleName
    val mActivity: Activity = this
    var mHandler: Handler = Handler()

    protected lateinit var mAuth: FirebaseAuth
    protected lateinit var mAnalytics: FirebaseAnalytics
    protected lateinit var mCurrentLang: Account.Language
    protected var mFirstRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mAnalytics = FirebaseAnalytics.getInstance(mActivity)
        MobileAds.initialize(mActivity)

        PreferenceManager.getDefaultSharedPreferences(mActivity).apply {
            mFirstRun = getBoolean(Configs.PREF_KEY_FIRST_RUN, true)
            mCurrentLang = Account.Language.valueOf(getString(Configs.PREF_KEY_LANGUAGE, "JP")!!)
        }
        Log.v(LOG_TAG, "onCreate mAnalytics.appInstanceId : ${mAnalytics.appInstanceId}")
        Log.v(LOG_TAG, "onCreate mAnalytics.firebaseInstanceId : ${mAnalytics.firebaseInstanceId}")
    }

    override fun onStart() {
        super.onStart()
        Log.i(LOG_TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume")
    }

    fun startWebSite(url: String) {
        Intent(Intent.ACTION_VIEW).let {
            it.data = Uri.parse(url)
            startActivity(it)
        }
    }

    abstract fun initView()
    abstract fun reloadAd()
}