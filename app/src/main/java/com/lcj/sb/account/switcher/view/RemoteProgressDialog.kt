package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.DialogAccountUploadBinding

class RemoteProgressDialog(val activity: Activity) {
    private val mDialog = AlertDialog.Builder(activity, R.style.CustomDialog).create()
    private val mBinding = DialogAccountUploadBinding.inflate(LayoutInflater.from(activity))

    init {
        mBinding.dialogCloseBtn.visibility = View.GONE
        mBinding.accountUploadFileCountTv.visibility = View.GONE
    }

    companion object {
        private var instance: RemoteProgressDialog? = null

        fun getInstance(activity: Activity): RemoteProgressDialog {
            if (instance == null) instance = RemoteProgressDialog(activity)
            return instance!!
        }
    }

    fun setTitle(title: String) {
        mBinding.dialogTitle.text = title
    }

    fun setFileCount(current: Int, total: Int) {
        mBinding.accountUploadFileCountTv.text = "$current / $total"
    }

    fun setProgress(progress: Int) {
        mBinding.accountUploadProgressBar.progress = progress
    }

    fun setCloseClickListener(listener: View.OnClickListener) {
        mBinding.dialogCloseBtn.setOnClickListener(listener)
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