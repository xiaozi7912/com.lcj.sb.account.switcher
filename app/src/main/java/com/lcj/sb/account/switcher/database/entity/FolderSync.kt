package com.lcj.sb.account.switcher.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.io.Serializable

@Entity(tableName = "tbl_folder_sync", primaryKeys = ["type", "lang"])
data class FolderSync(
        var type: Int,
        var lang: Int,
        @ColumnInfo(name = "update_time") var updateTime: Long
) : Serializable {
    enum class Type {
        LOCAL, REMOTE
    }
}