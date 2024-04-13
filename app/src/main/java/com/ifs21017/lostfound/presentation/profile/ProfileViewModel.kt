package com.ifs21017.lostfound.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ifs21017.lostfound.data.remote.MyResult
import com.ifs21017.lostfound.data.remote.response.DataUserResponse
import com.ifs21017.lostfound.data.repository.AuthRepository
import com.ifs21017.lostfound.data.repository.UserRepository
import com.ifs21017.lostfound.ViewModelFactory
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
    fun getMe(): LiveData<MyResult<DataUserResponse>> {
        return userRepository.getMe().asLiveData()
    }
    companion object {
        @Volatile
        private var INSTANCE: ProfileViewModel? = null
        fun getInstance(
            authRepository: AuthRepository,
            userRepository: UserRepository
        ): ProfileViewModel {
            synchronized(ViewModelFactory::class.java) {
                INSTANCE = ProfileViewModel(
                    authRepository,
                    userRepository
                )
            }
            return INSTANCE as ProfileViewModel
        }
    }
}