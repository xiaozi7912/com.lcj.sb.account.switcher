package com.lcj.sb.account.switcher.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lcj.sb.account.switcher.database.entity.Account

@Dao
interface AccountDAO {
    @Insert
    fun insert(account: Account)

    @Update
    fun update(account: Account)

    @Query("UPDATE accounts SET selected = 0 WHERE lang = :lang AND selected = 1")
    fun deselectAll(lang: Int)

    @Query("SELECT * FROM accounts WHERE lang = :lang AND hidden = 0 ORDER BY id ASC LIMIT 20")
    fun accounts(lang: Int): LiveData<List<Account>>

    @Query("SELECT * FROM accounts WHERE lang = :lang AND hidden = :hidden ORDER BY id ASC LIMIT 20")
    fun accounts(lang: Int, hidden: Boolean): List<Account>

    @Query("SELECT * FROM accounts WHERE folder = :folder")
    fun account(folder: String): Account?
}