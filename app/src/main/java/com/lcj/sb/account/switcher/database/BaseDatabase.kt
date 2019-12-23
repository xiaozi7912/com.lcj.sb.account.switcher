package com.lcj.sb.account.switcher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.database.entity.Account

@Database(entities = [Account::class], version = 1)
abstract class BaseDatabase : RoomDatabase() {
    companion object {
        private const val DB_NAME: String = BuildConfig.APPLICATION_ID + ".db"
        private var instance: BaseDatabase? = null

        fun getInstance(context: Context): BaseDatabase {
            if (instance == null) {
                instance = create(context)
            }
            return instance!!
        }

        private fun create(context: Context): BaseDatabase {
            return Room.databaseBuilder(context, BaseDatabase::class.java, DB_NAME).build()
        }
    }

    abstract fun getAccountDao(): AccountDao
}