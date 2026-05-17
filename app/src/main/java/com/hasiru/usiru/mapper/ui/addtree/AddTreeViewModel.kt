package com.hasiru.usiru.mapper.ui.addtree

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hasiru.usiru.mapper.ai.GeminiTreeIdentifier
import com.hasiru.usiru.mapper.ai.TreeIdentificationData
import com.hasiru.usiru.mapper.ai.TreeIdentificationResult
import com.hasiru.usiru.mapper.data.local.TreeEntity
import com.hasiru.usiru.mapper.data.repository.TreeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class IdentificationState {
    data object Idle : IdentificationState()
    data object Loading : IdentificationState()
    data class Success(val data: TreeIdentificationData) : IdentificationState()
    data class Error(val message: String) : IdentificationState()
}

class AddTreeViewModel(
    private val repository: TreeRepository,
    private val identifier: GeminiTreeIdentifier
) : ViewModel() {
    private val _identificationState = MutableStateFlow<IdentificationState>(IdentificationState.Idle)
    val identificationState: StateFlow<IdentificationState> = _identificationState.asStateFlow()

    private val _oxygenScore = MutableStateFlow(0f)
    val oxygenScore: StateFlow<Float> = _oxygenScore.asStateFlow()

    private var speciesFactor = 1f
    private var girth = 0f

    fun identify(bitmap: Bitmap) = viewModelScope.launch {
        _identificationState.value = IdentificationState.Loading
        _identificationState.value = when (val result = identifier.identifyTree(bitmap)) {
            is TreeIdentificationResult.Success -> {
                updateSpeciesFactor(result.data.speciesFactor)
                IdentificationState.Success(result.data)
            }
            is TreeIdentificationResult.Error -> IdentificationState.Error(result.message)
        }
    }

    fun updateGirth(value: Float) {
        girth = value; recalculate()
    }

    fun updateSpeciesFactor(value: Float) {
        speciesFactor = value; recalculate()
    }

    private fun recalculate() { _oxygenScore.value = girth * speciesFactor }

    suspend fun saveTree(tree: TreeEntity) = repository.insertAndSync(tree)

    class Factory(private val repository: TreeRepository, private val identifier: GeminiTreeIdentifier) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AddTreeViewModel(repository, identifier) as T
    }
}
