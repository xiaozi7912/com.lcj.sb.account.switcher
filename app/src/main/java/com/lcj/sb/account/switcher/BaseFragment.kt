package com.lcj.sb.account.switcher

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.lcj.sb.account.switcher.utils.Configs

/**
 * Created by Larry on 2018-07-01.
 */
open class BaseFragment : Fragment() {
    val LOG_TAG: String = javaClass.simpleName
    var mHandler: Handler = Handler()
    lateinit var mActivity: Activity

    lateinit var mContentView: View

    companion object {
        const val REQUEST_CODE_GOOGLE_SIGN_IN = 1001
        const val REQUEST_CODE_FOLDER_PERMISSION = 1002
    }

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

    protected fun hasFolderPermission(): Boolean {
        var result = false
        for (permission in mActivity.contentResolver.persistedUriPermissions) {
            if (permission.uri == Uri.parse(Configs.URI_ANDROID_DATA)) {
                result = true
                break
            }
        }
        return result
    }

    fun startApplication(appId: String) {
        mActivity.packageManager.getLaunchIntentForPackage(appId).let {
            startActivity(it)
        }
    }
}