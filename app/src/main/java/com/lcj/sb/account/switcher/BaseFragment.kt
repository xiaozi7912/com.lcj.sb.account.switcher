package com.lcj.sb.account.switcher

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.utils.Configs

/**
 * Created by Larry on 2018-07-01.
 */
open class BaseFragment : Fragment() {
    val LOG_TAG: String = javaClass.simpleName
    var mHandler: Handler = Handler()
    lateinit var mActivity: Activity

    lateinit var mContentView: View

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.i(LOG_TAG, "onActivityCreated")
        mActivity = requireActivity()
        mContentView = mActivity.findViewById(android.R.id.content)
    }

    override fun onStart() {
        super.onStart()
        Log.i(LOG_TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i(LOG_TAG, "onResume")
    }

    fun startApplication(appId: String) {
        mActivity.packageManager
                .getLaunchIntentForPackage(appId).let {
                    startActivity(it)
                }
    }
}