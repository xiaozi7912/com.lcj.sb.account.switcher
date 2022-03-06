package com.lcj.sb.account.switcher.database.entity

import androidx.paging.Config
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lcj.sb.account.switcher.utils.Configs
import java.io.Serializable

@Entity(tableName = "accounts", indices = [Index(value = ["folder"], unique = true)])
data class Account(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var alias: String,
    var folder: String,
    var lang: Int,
    var selected: Boolean = false,
    var hidden: Boolean = false,
    @ColumnInfo(name = "create_time") var createTime: Long,
    @ColumnInfo(name = "update_time") var updateTime: Long
) : Serializable {
    enum class Language(val packageName: String, val screenName: String) {
        JP(Configs.PREFIX_NAME_SB_JP, Configs.SCREEN_SB_JP),
        TW(Configs.PREFIX_NAME_SB_TW, Configs.SCREEN_SB_TW)
    }
}