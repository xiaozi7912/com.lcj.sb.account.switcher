package com.lcj.sb.account.switcher.database.entity

data class GoogleDriveItem(
        val id: String,
        val name: String,
        val modifiedTime: Long,
        val lang: Account.Language)