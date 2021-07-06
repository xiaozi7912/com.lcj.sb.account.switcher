package com.lcj.sb.account.switcher.database

import androidx.paging.DataSource
import androidx.room.*
import com.lcj.sb.account.switcher.database.entity.Account

@Dao
interface AccountDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account)

    @Delete
    fun delete(account: Account)

    @Update
    fun update(account: Account)

    @Query("UPDATE accounts SET selected = 0 WHERE lang = :lang AND selected = 1")
    fun deselectAll(lang: Int)

    @Query("SELECT * FROM accounts ORDER BY folder ASC")
    fun liveAccounts(): DataSource.Factory<Int, Account>

    @Query("SELECT * FROM accounts WHERE lang = :lang ORDER BY folder ASC")
    fun liveAccounts(lang: Int): DataSource.Factory<Int, Account>

    @Query("SELECT * FROM accounts WHERE lang = :lang AND hidden = :hidden ORDER BY folder ASC")
    fun liveAccounts(lang: Int, hidden: Boolean): DataSource.Factory<Int, Account>

    @Query("SELECT * FROM accounts WHERE lang = :lang AND hidden = :hidden ORDER BY folder ASC")
    fun accounts(lang: Int, hidden: Boolean): List<Account>

    @Query("SELECT * FROM accounts WHERE folder = :folder")
    fun account(folder: String): Account?

    @Query("SELECT COUNT(id) FROM accounts WHERE folder = :folder")
    fun getTotalCount(folder: String): Int = 0
}