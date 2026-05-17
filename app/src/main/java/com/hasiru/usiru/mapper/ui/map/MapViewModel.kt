package com.hasiru.usiru.mapper.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hasiru.usiru.mapper.data.local.TreeEntity
import com.hasiru.usiru.mapper.data.repository.TreeRepository
import kotlinx.coroutines.launch

class MapViewModel(private val repository: TreeRepository) : ViewModel() {
    val treesLiveData: LiveData<List<TreeEntity>> = repository.observeAllTrees().asLiveData()

    fun fetchCommunityTrees() = viewModelScope.launch { repository.fetchCommunityTrees() }

    class Factory(private val repository: TreeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MapViewModel(repository) as T
    }
}
