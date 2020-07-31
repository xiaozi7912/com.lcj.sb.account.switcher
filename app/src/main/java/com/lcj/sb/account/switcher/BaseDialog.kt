package com.lcj.sb.account.switcher

import android.app.Activity
import android.app.AlertDialog
import android.view.View

abstract class BaseDialog(val activity: Activity) {
    protected val LOG_TAG = javaClass.simpleName
    protected var mDialog: AlertDialog
    protected lateinit var mRootView: View

    init {
        mDialog = AlertDialog.Builder(activity, R.style.CustomDialog)
                .setOnDismissListener { dismiss() }
                .create()
    }

    open fun show() {
        mDialog.show()
        mDialog.setContentView(mRootView)
    }

    abstract fun initView()

    open fun dismiss() {
        mDialog.dismiss()
    }
}