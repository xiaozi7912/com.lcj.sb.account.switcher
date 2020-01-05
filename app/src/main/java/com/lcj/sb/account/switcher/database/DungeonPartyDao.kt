package com.lcj.sb.account.switcher.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lcj.sb.account.switcher.database.entity.DungeonParty

@Dao
interface DungeonPartyDao {
    @Insert
    fun insert(dungeonParty: DungeonParty)

    @Query("DELETE FROM dungeon_party")
    fun deleteAll()

    @Query("SELECT * FROM dungeon_party WHERE account_id = :accountId")
    fun partys(accountId: Int): LiveData<List<DungeonParty>>
}