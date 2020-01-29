package com.lcj.sb.account.switcher.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lcj.sb.account.switcher.database.entity.FolderSync
import io.reactivex.Single

@Dao
interface FolderSyncDAO {
    @Insert
    fun insert(entity: FolderSync)

    @Update
    fun update(entity: FolderSync)

    @Query("SELECT * FROM tbl_folder_sync WHERE type = :type AND lang = :lang")
    fun folderSync(type: Int, lang: Int): Single<FolderSync?>
}