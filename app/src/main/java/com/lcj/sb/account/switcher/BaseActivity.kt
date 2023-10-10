package com.lcj.sb.account.switcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
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
    protected val LOG_TAG: String = javaClass.simpleName

    protected lateinit var mContentView: View

    protected lateinit var mAuth: FirebaseAuth
    protected lateinit var mRemoteConfig: FirebaseRemoteConfig
    protected lateinit var mCurrentLang: Account.Language

    protected lateinit var mDisplayMetrics: DisplayMetrics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mRemoteConfig = FirebaseRemoteConfig.getInstance()
        mDisplayMetrics = resources.displayMetrics

        MobileAds.initialize(this)
        Handler(mainLooper).postDelayed({ initAdMob() }, 1000)

        mContentView = findViewById(android.R.id.content)

        getPreferences(Context.MODE_PRIVATE).apply {
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

    protected fun startWebSite(url: String) {
        Intent(Intent.ACTION_VIEW).let {
            it.data = Uri.parse(url)
            startActivity(it)
        }
    }

    protected abstract fun initView()
    protected abstract fun initAdMob()
}