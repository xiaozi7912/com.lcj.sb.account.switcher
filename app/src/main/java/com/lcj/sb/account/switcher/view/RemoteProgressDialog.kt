package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import com.lcj.sb.account.switcher.R
import com.lcj.sb.account.switcher.databinding.DialogAccountUploadBinding

class RemoteProgressDialog(val activity: Activity) {
    private val mDialog = AlertDialog.Builder(activity, R.style.CustomDialog).create()
    private val mBinding = DialogAccountUploadBinding.inflate(LayoutInflater.from(activity))

    init {
        mBinding.dialogCloseBtn.isVisible = false
        mBinding.dialogTitle.isVisible = false
        mBinding.dialogMessage.isVisible = false
        mBinding.accountUploadFileCountTv.isVisible = false
    }

    companion object {
        private var instance: RemoteProgressDialog? = null

        fun getInstance(activity: Activity): RemoteProgressDialog {
            if (instance == null) instance = RemoteProgressDialog(activity)
            return instance!!
        }
    }

    var title: String
        get() = mBinding.dialogTitle.text.toString()
        set(value) {
            mBinding.dialogTitle.text = value
        }

    var message: String
        get() = mBinding.dialogMessage.text.toString()
        set(value) {
            mBinding.dialogMessage.text = value
        }

    fun setVisible(title: Boolean = true, message: Boolean = false, fileCount: Boolean = false) {
        titleIsVisible = title
        messageIsVisible = message
        fileCountIsVisible = fileCount
    }

    fun setFileCount(current: Int, total: Int) {
        mBinding.accountUploadFileCountTv.text = String.format("%s / %s", current, total)
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

    private var titleIsVisible: Boolean
        get() = mBinding.dialogTitle.isVisible
        set(value) {
            mBinding.dialogTitle.isVisible = value
        }

    private var messageIsVisible: Boolean
        get() = mBinding.dialogMessage.isVisible
        set(value) {
            mBinding.dialogMessage.isVisible = value
        }

    private var fileCountIsVisible: Boolean
        get() = mBinding.accountUploadFileCountTv.isVisible
        set(value) {
            mBinding.accountUploadFileCountTv.isVisible = value
        }
}