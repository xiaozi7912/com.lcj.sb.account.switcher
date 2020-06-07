package com.lcj.sb.account.switcher

import android.app.Activity
import android.app.AlertDialog
import androidx.databinding.ViewDataBinding

open class BaseDialog(val activity: Activity) {
    protected val LOG_TAG = javaClass.simpleName
    protected val mDialog = AlertDialog.Builder(activity, R.style.CustomDialog).create()
    protected lateinit var mBinding: ViewDataBinding

    open fun show() {
        mDialog.show()
        mDialog.setContentView(mBinding.root)
    }

    open fun dismiss() {
        mDialog.dismiss()
    }
}