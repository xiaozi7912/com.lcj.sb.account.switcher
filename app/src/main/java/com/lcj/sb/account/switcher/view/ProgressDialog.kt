package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.lcj.sb.account.switcher.R

class ProgressDialog(val activity: Activity) {
    private val mDialog = AlertDialog.Builder(activity, R.style.CustomDialog).create()
    private val mInflater = LayoutInflater.from(activity)

    companion object {
        private var instance: ProgressDialog? = null

        fun newInstance(activity: Activity): ProgressDialog {
            if (instance == null) instance = ProgressDialog(activity)
            return instance!!
        }
    }

    fun show() {
        mDialog.show()
        mDialog.setCancelable(false)
        mDialog.setContentView(mInflater.inflate(R.layout.dialog_progress, null, false))
    }

    fun dismiss() {
        mDialog.dismiss()
        instance = null
    }
}