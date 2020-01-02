package com.lcj.sb.account.switcher

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics

class BaseApplication : Application() {
    companion object {
        lateinit var analytics: FirebaseAnalytics
    }

    override fun onCreate() {
        super.onCreate()
        analytics = FirebaseAnalytics.getInstance(this)
    }
}