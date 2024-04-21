package com.ifs21017.lostfound.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ifs21017.lostfound.data.entity.DelcomObjectEntity
import com.ifs21017.lostfound.data.room.DelcomObjectDatabase
import com.ifs21017.lostfound.data.room.IDelcomObjectDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
class LocalObjectRepository(context: Context) {
    private val mDelcomObjectDao: IDelcomObjectDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    init {
        val db = DelcomObjectDatabase.getInstance(context)
        mDelcomObjectDao = db.delcomObjectDao()
    }
    fun getAllObjects(): LiveData<List<DelcomObjectEntity>?> = mDelcomObjectDao.getAllObjects()
    fun get(lostfoundId: Int): LiveData<DelcomObjectEntity?> = mDelcomObjectDao.get(lostfoundId)
    fun insert(lostfound: DelcomObjectEntity) {
        executorService.execute { mDelcomObjectDao.insert(lostfound) }
    }
    fun delete(lostfound: DelcomObjectEntity) {
        executorService.execute { mDelcomObjectDao.delete(lostfound) }
    }
    companion object {
        @Volatile
        private var INSTANCE: LocalObjectRepository? = null
        fun getInstance(
            context: Context
        ): LocalObjectRepository {
            synchronized(LocalObjectRepository::class.java) {
                INSTANCE = LocalObjectRepository(
                    context
                )
            }
            return INSTANCE as LocalObjectRepository
        }
    }
}