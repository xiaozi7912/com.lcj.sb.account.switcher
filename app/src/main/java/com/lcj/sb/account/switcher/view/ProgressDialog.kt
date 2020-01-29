package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.DialogProgressBinding

class ProgressDialog(val activity: Activity) {
    private val mDialog = AlertDialog.Builder(activity, R.style.CustomDialog).create()
    private val mBinding = DialogProgressBinding.inflate(LayoutInflater.from(activity))

    companion object {
        private var instance: ProgressDialog? = null

        fun getInstance(activity: Activity): ProgressDialog {
            if (instance == null) instance = ProgressDialog(activity)
            return instance!!
        }
    }

    fun show() {
        mDialog.show()
        mDialog.setCancelable(false)
        mDialog.setContentView(mBinding.root)
    }

    fun dismiss() {
        mDialog.dismiss()
        instance = null
    }
}