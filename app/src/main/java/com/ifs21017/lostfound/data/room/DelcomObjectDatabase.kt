package com.ifs21017.lostfound.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ifs21017.lostfound.data.entity.DelcomObjectEntity

@Database(entities = [DelcomObjectEntity::class], version = 1, exportSchema = false)
abstract class DelcomObjectDatabase : RoomDatabase() {
    abstract fun delcomObjectDao(): IDelcomObjectDao
    companion object {
        private const val Database_NAME = "DelcomObject.db"
        @Volatile
        private var INSTANCE: DelcomObjectDatabase? = null
        @JvmStatic
        fun getInstance(context: Context): DelcomObjectDatabase {
            if (INSTANCE == null) {
                synchronized(DelcomObjectDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DelcomObjectDatabase::class.java,
                        Database_NAME
                    ).build()
                }
            }
            return INSTANCE as DelcomObjectDatabase
        }
    }
}