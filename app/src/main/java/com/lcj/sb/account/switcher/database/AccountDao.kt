package com.lcj.sb.account.switcher.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lcj.sb.account.switcher.database.entity.Account

@Dao
interface AccountDao {
    @Insert
    fun insertAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE lang = :lang ORDER BY id")
    fun loadAccounts(lang: Int): Array<Account>
}