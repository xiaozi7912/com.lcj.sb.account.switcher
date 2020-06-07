package com.lcj.sb.account.switcher.view

import android.app.Activity
import android.view.LayoutInflater
import com.lcj.sb.account.switcher.BaseDialog
import com.lcj.sb.account.switcher.databinding.DialogMonsterFilterBinding

class MonsterFilterDialog(activity: Activity) : BaseDialog(activity) {
    init {
        mBinding = DialogMonsterFilterBinding.inflate(LayoutInflater.from(activity))
    }

    companion object {
        private var instance: MonsterFilterDialog? = null

        fun getInstance(activity: Activity): MonsterFilterDialog {
            if (instance == null) instance = MonsterFilterDialog(activity)
            return instance!!
        }
    }

    override fun dismiss() {
        super.dismiss()
        instance = null
    }
}