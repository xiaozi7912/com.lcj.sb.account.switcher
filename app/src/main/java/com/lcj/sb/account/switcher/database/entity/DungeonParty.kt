package com.lcj.sb.account.switcher.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = "dungeon_party",
        foreignKeys = [ForeignKey(entity = Account::class, parentColumns = ["id"], childColumns = ["account_id"], onDelete = CASCADE)])
data class DungeonParty(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        @ColumnInfo(name = "account_id") var accountId: Int,
        @ColumnInfo(name = "dungeon_type") var dungeonType: Int,
        @ColumnInfo(name = "element_type") var elementType: Int,
        var title: String,
        @ColumnInfo(name = "image_path") var imagePath: String
)