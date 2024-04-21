package com.ifs21017.lostfound.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.ifs21017.lostfound.data.entity.DelcomObjectEntity
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.data.remote.response.LostFoundsItemResponse

class Utils {
    companion object{
        fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
            val observerWrapper = object : Observer<T> {
                override fun onChanged(value: T) {
                    observer(value)
                    if (value is MyResult.Success<*> || value is MyResult.Error) {
                        removeObserver(this)
                    }
                }
            }
            observeForever(observerWrapper)
        }
        fun entitiesToResponses(entities: List<DelcomObjectEntity>):
                List<LostFoundsItemResponse> {
            val responses = ArrayList<LostFoundsItemResponse>()
            entities.map {
                val response = LostFoundsItemResponse(
                    cover = it.cover,
                    updatedAt = it.updatedAt,
                    description = it.description,
                    createdAt = it.createdAt,
                    id = it.id,
                    title = it.title,
                    isCompleted = it.isCompleted,
                    status = it.status
                )
                responses.add(response)
            }
            return responses
        }
    }
}


