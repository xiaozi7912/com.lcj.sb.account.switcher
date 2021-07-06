package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.view.LayoutInflater
import com.lcj.sb.account.switcher.BaseDialog
import com.lcj.sb.account.switcher.databinding.DialogProgressBinding

class ProgressDialog(activity: Activity) : BaseDialog(activity) {
    private var mBinding = DialogProgressBinding.inflate(LayoutInflater.from(activity))

    init {
        mRootView = mBinding.root
    }

    companion object {
        private var instance: ProgressDialog? = null

        fun getInstance(activity: Activity): ProgressDialog {
            if (instance == null) instance = ProgressDialog(activity)
            return instance!!
        }
    }

    override fun initView() {
        TODO("Not yet implemented")
    }

    override fun show() {
        super.show()
        mDialog.setCancelable(false)
    }

    override fun dismiss() {
        super.dismiss()
        instance = null
    }
}