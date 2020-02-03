package com.lcj.sb.account.switcher

import android.app.Activity
import android.app.Application
import android.os.Handler
import com.google.firebase.analytics.FirebaseAnalytics

class BaseApplication : Application() {

    companion object {
        private val mHandler = Handler()
        private lateinit var analytics: FirebaseAnalytics

        fun setCurrentScreen(activity: Activity, screenName: String, screenClassOverride: String) {
            Thread {
                Thread.sleep(1000)
                mHandler.post { analytics.setCurrentScreen(activity, screenName, screenClassOverride) }
            }.start()
        }
    }

    override fun onCreate() {
        super.onCreate()
        analytics = FirebaseAnalytics.getInstance(this)
    }
}