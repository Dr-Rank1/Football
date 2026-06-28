package com.rank.football.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras

class PlayerViewModelFactory(
    private val application: Application,
    private val fixtureId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        savedStateHandle["fixtureId"] = fixtureId
        return PlayerViewModel(application, savedStateHandle) as T
    }
}
