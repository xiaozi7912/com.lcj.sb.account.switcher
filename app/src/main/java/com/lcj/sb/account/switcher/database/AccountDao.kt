package com.lcj.sb.account.switcher.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lcj.sb.account.switcher.database.entity.Account

@Dao
interface AccountDao {
    @Insert
    fun insertAccount(account: Account)

    @Update
    fun updateAccount(account: Account)

    @Query("UPDATE accounts SET selected = 0")
    fun deselectAllAccount()

    @Query("SELECT * FROM accounts WHERE lang = :lang ORDER BY id")
    fun loadAccounts(lang: Int): LiveData<List<Account>>
}