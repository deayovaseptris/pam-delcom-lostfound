package com.ifs21017.lostfound.presentation.lostfound

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ifs21017.lostfound.data.entity.DelcomObjectEntity
import com.ifs21017.lostfound.presentation.ViewModelFactory
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.data.remote.response.DataAddObjectResponse
import com.ifs21017.lostfound.data.remote.response.DelcomObjectResponse
import com.ifs21017.lostfound.data.remote.response.DelcomResponse
import com.ifs21017.lostfound.data.repository.LocalObjectRepository
import com.ifs21017.lostfound.data.repository.ObjectRepository
import okhttp3.MultipartBody

class ObjectViewModel (
    private val objectRepository : ObjectRepository,
    private val localObjectRepository: LocalObjectRepository
) : ViewModel() {

    fun getObject(lostfoundId: Int) : LiveData<MyResult<DelcomObjectResponse>> {
        return objectRepository.getObject(lostfoundId).asLiveData()
    }

    fun postObject(
        title: String,
        description : String,
        status: String,
    ) : LiveData<MyResult<DataAddObjectResponse>> {
        return objectRepository.postObject(
            title,
            description,
            status
        ).asLiveData()
    }

    fun putObject(
        lostfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ) : LiveData<MyResult<DelcomResponse>> {
        return objectRepository.putObject(
            lostfoundId,
            title,
            description,
            status,
            isCompleted
        ).asLiveData()
    }

    fun delete(lostfoundId: Int) : LiveData<MyResult<DelcomResponse>> {
        return objectRepository.deleteObject(lostfoundId).asLiveData()
    }

    fun getLocalObjects(): LiveData<List<DelcomObjectEntity>?> {
        return localObjectRepository.getAllObjects()
    }
    fun getLocalObject(lostfoundId: Int): LiveData<DelcomObjectEntity?> {
        return localObjectRepository.get(lostfoundId)
    }
    fun insertLocalObject(lostfound: DelcomObjectEntity) {
        localObjectRepository.insert(lostfound)
    }
    fun deleteLocalTodo(todo: DelcomObjectEntity) {
        localObjectRepository.delete(todo)
    }

    fun addCoverObject(
        lostfoundId: Int,
        cover: MultipartBody.Part,
    ): LiveData<MyResult<DelcomResponse>> {
        return objectRepository.addCoverObject(lostfoundId, cover).asLiveData()
    }

    companion object {
        @Volatile
        private var INSTANCE: ObjectViewModel? = null
        fun getInstance (
            objectRepository: ObjectRepository,
            localObjectRepository: LocalObjectRepository,
        ) : ObjectViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = ObjectViewModel(
                    objectRepository,
                    localObjectRepository
                )
            }
            return INSTANCE as ObjectViewModel
        }
    }
}