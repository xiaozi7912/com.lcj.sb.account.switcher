package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.DialogAccountUploadBinding

class AccountUploadDialog(val activity: Activity) {
    private val mDialog = AlertDialog.Builder(activity, R.style.CustomDialog).create()
    private val mBinding = DialogAccountUploadBinding.inflate(LayoutInflater.from(activity))

    companion object {
        private var instance: AccountUploadDialog? = null

        fun getInstance(activity: Activity): AccountUploadDialog {
            if (instance == null) instance = AccountUploadDialog(activity)
            return instance!!
        }
    }

    fun setFileName(fileName: String) {
        mBinding.accountUploadTv.text = "上傳中：$fileName"
    }

    fun setFileCount(current: Int, total: Int) {
        mBinding.accountUploadFileCountTv.text = "$current / $total"
    }

    fun setProgress(progress: Int) {
        mBinding.accountUploadProgressBar.progress = progress
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