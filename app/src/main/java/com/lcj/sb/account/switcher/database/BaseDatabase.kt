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
import com.lcj.sb.account.switcher.database.entity.FolderSync

@Database(entities = [Account::class, DungeonParty::class, FolderSync::class], version = 6)
abstract class BaseDatabase : RoomDatabase() {
    companion object {
        private const val DB_NAME: String = BuildConfig.APPLICATION_ID + ".db"
        private var instance: BaseDatabase? = null

        private val MIGRATION_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val command = "CREATE TABLE dungeon_party (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,account_id INTEGER NOT NULL,dungeon_type INTEGER NOT NULL,element_type INTEGER NOT NULL,title TEXT NOT NULL,image_path TEXT NOT NULL,FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE)"
                database.execSQL(command)
            }
        }

        private val MIGRATION_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                var command = "ALTER TABLE dungeon_party ADD icon_name TEXT"
                database.execSQL(command)

                command = "ALTER TABLE dungeon_party ADD event_title TEXT"
                database.execSQL(command)

                command = "ALTER TABLE dungeon_party ADD monster_name TEXT NOT NULL DEFAULT ''"
                database.execSQL(command)

                command = "ALTER TABLE dungeon_party ADD remark TEXT NOT NULL DEFAULT ''"
                database.execSQL(command)
            }
        }

        private val MIGRATION_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val command = "CREATE TABLE tbl_folder_sync (type INTEGER NOT NULL,lang INTEGER NOT NULL,update_time INTEGER NOT NULL,PRIMARY KEY (type,lang))"
                database.execSQL(command)
            }
        }

        private val MIGRATION_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val command = "ALTER TABLE accounts ADD hidden INTEGER NOT NULL DEFAULT 0"
                database.execSQL(command)
            }
        }

        private val MIGRATION_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val command = "CREATE UNIQUE INDEX index_accounts_folder ON accounts('folder')"
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
                    .addMigrations(MIGRATION_2)
                    .addMigrations(MIGRATION_3)
                    .addMigrations(MIGRATION_4)
                    .addMigrations(MIGRATION_5)
                    .addMigrations(MIGRATION_6)
                    .build()
        }
    }

    abstract fun accountDAO(): AccountDAO
    abstract fun dungeonPartyDAO(): DungeonPartyDAO
    abstract fun folderSyncDAO(): FolderSyncDAO
}