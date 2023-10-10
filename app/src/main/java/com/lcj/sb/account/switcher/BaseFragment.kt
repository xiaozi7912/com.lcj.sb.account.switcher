package com.lcj.sb.account.switcher

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.lcj.sb.account.switcher.utils.Configs

/**
 * Created by Larry on 2018-07-01.
 */
open class BaseFragment : Fragment() {
    protected val LOG_TAG: String = javaClass.simpleName
    protected lateinit var mActivity: Activity

    protected lateinit var mContentView: View

    companion object {
        const val REQUEST_CODE_GOOGLE_SIGN_IN = 1001
        const val REQUEST_CODE_FOLDER_PERMISSION = 1002
        const val REQUEST_CODE_FOLDER_PERMISSION_FOR_LOAD_GAME = 1003
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

    protected fun hasFolderPermission(packageName: String): Boolean {
        var result = false
        for (permission in mActivity.contentResolver.persistedUriPermissions) {
            if (permission.uri.toString().contains(packageName)) {
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