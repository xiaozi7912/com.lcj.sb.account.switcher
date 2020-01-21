package com.lcj.sb.account.switcher.model

import android.content.Context
import android.util.Log
import androidx.databinding.BaseObservable
import com.lcj.sb.account.switcher.database.BaseDatabase
import com.lcj.sb.account.switcher.database.entity.Account

data class AccountEditModel(val alias: String) : BaseObservable() {
    private val LOG_TAG = javaClass.simpleName

    var editAlias: String = ""

    fun onCancelClick() {
        Log.i(LOG_TAG, "onCancelClick")
    }

    fun onEditClick(context: Context, account: Account) {
        Log.i(LOG_TAG, "onEditClick")
        if (editAlias.isNotEmpty()) {
            account.alias = editAlias
            account.updateTime = System.currentTimeMillis()

            Thread {
                BaseDatabase.getInstance(context).accountDAO()
                        .update(account)
            }.start()
        }
    }
}