package com.lcj.sb.account.switcher.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lcj.sb.account.switcher.database.entity.DungeonParty

@Dao
interface DungeonPartyDAO {
    @Insert
    fun insert(dungeonParty: DungeonParty)

    @Query("DELETE FROM dungeon_party")
    fun deleteAll()

    @Query("SELECT * FROM dungeon_party WHERE account_id = :accountId")
    fun getPartyList(accountId: Int): LiveData<List<DungeonParty>>

    @Query("SELECT * FROM dungeon_party WHERE account_id = :accountId AND dungeon_type IN (:dungeonTypes) AND element_type IN (:elementTypes) AND (title LIKE :title OR monster_name LIKE :title OR remark LIKE :title)")
    fun getFilterPartyList(accountId: Int, dungeonTypes: List<Int>, elementTypes: List<Int>, title: String): List<DungeonParty>
}