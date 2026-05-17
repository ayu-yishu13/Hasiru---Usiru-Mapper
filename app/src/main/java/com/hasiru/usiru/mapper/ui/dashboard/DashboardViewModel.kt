package com.hasiru.usiru.mapper.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.hasiru.usiru.mapper.data.repository.TreeRepository

class DashboardViewModel(repository: TreeRepository) : ViewModel() {
    private val trees = repository.observeAllTrees().asLiveData()
    val totalOxygenScore: LiveData<Float> = repository.getTotalOxygenScore().asLiveData()
    val treeCount: LiveData<Int> = trees.map { it.count { tree -> !tree.isEmptyPit } }
    val nativeCount: LiveData<Int> = trees.map { it.count { tree -> tree.isNative } }
    val emptyPitCount: LiveData<Int> = trees.map { it.count { tree -> tree.isEmptyPit } }
    val topSpecies: LiveData<List<Pair<String, Int>>> = trees.map { list ->
        list.filter { !it.isEmptyPit }.groupingBy { it.species.ifBlank { "Unknown" } }.eachCount()
            .toList().sortedByDescending { it.second }.take(5)
    }

    class Factory(private val repository: TreeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(repository) as T
    }
}
