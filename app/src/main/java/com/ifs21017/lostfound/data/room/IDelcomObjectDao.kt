package com.ifs21017.lostfound.data.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ifs21017.lostfound.data.entity.DelcomObjectEntity

@Dao
interface IDelcomObjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(delcomObject: DelcomObjectEntity)
    @Delete
    fun delete(delcomObject: DelcomObjectEntity)
    @Query("SELECT * FROM delcom_objects WHERE id = :id LIMIT 1")
    fun get(id: Int): LiveData<DelcomObjectEntity?>
    @Query("SELECT * FROM delcom_objects ORDER BY created_at DESC")
    fun getAllObjects(): LiveData<List<DelcomObjectEntity>?>
}