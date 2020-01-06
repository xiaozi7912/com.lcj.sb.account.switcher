package com.lcj.sb.account.switcher.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "accounts")
data class Account(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var alias: String,
        var folder: String,
        var lang: Int,
        var selected: Boolean = false,
        @ColumnInfo(name = "create_time") var createTime: Long,
        @ColumnInfo(name = "update_time") var updateTime: Long
) : Serializable {
    enum class Language {
        JP, TW
    }
}