package com.lcj.sb.account.switcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.utils.Configs

/**
 * Created by Larry on 2018-06-18.
 */
abstract class BaseActivity : AppCompatActivity() {
    val LOG_TAG: String = javaClass.simpleName
    val mActivity: Activity = this
    protected lateinit var mDisplayMetrics: DisplayMetrics
    var mHandler: Handler = Handler()

    protected lateinit var mContentView: View

    protected lateinit var mAuth: FirebaseAuth
    protected lateinit var mRemoteConfig: FirebaseRemoteConfig
    protected lateinit var mCurrentLang: Account.Language
    protected var mFirstRun = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDisplayMetrics = resources.displayMetrics
        mAuth = FirebaseAuth.getInstance()
        mRemoteConfig = FirebaseRemoteConfig.getInstance()
        MobileAds.initialize(mActivity)
        mHandler.postDelayed({ initAdMob() }, 1000)

        mContentView = findViewById(android.R.id.content)

        mActivity.getPreferences(Context.MODE_PRIVATE).apply {
            mFirstRun = getBoolean(Configs.PREF_KEY_FIRST_RUN, true)
            mCurrentLang = Account.Language.valueOf(getString(Configs.PREF_KEY_LANGUAGE, "JP")!!)
        }
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
    abstract fun initAdMob()
}