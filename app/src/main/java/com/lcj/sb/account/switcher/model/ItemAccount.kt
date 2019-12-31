package com.lcj.sb.account.switcher.model

import androidx.databinding.BaseObservable

data class ItemAccount(
        val alias: String,
        val path: String,
        val updateTime: String,
        val selected: Boolean = false
) : BaseObservable() {
}