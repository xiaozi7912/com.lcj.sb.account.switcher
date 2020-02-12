package com.lcj.sb.account.switcher.repository

import com.lcj.sb.account.switcher.database.entity.Account

interface IAccountListListener {
    fun onItemClick(account: Account)
    fun onDeleteClick(account: Account)
    fun onEditAliasClick(account: Account)
    fun onSaveClick(account: Account)
    fun onLoadGameClick(account: Account)
    fun onMoreClick(account: Account)
}