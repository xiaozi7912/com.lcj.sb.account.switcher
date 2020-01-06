package com.lcj.sb.account.switcher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lcj.sb.account.switcher.BuildConfig
import com.lcj.sb.account.switcher.database.entity.Account
import com.lcj.sb.account.switcher.database.entity.DungeonParty

@Database(entities = [Account::class, DungeonParty::class], version = 2)
abstract class BaseDatabase : RoomDatabase() {
    companion object {
        private const val DB_NAME: String = BuildConfig.APPLICATION_ID + ".db"
        private var instance: BaseDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val command = "CREATE TABLE dungeon_party (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,account_id INTEGER NOT NULL,dungeon_type INTEGER NOT NULL,element_type INTEGER NOT NULL,title TEXT NOT NULL,image_path TEXT NOT NULL,FOREIGN KEY (account_id) REFERENCES accounts(id)  ON DELETE CASCADE)"
                database.execSQL(command)
            }
        }

        fun getInstance(context: Context): BaseDatabase {
            if (instance == null) {
                instance = create(context)
            }
            return instance!!
        }

        private fun create(context: Context): BaseDatabase {
            return Room.databaseBuilder(context, BaseDatabase::class.java, DB_NAME)
                    .addMigrations(MIGRATION_1_2).build()
        }
    }

    abstract fun accountDAO(): AccountDao
    abstract fun dungeonPartyDAO(): DungeonPartyDao
}