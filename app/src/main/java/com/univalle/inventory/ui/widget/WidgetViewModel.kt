package com.univalle.inventory.ui.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.inventory.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val repository: WidgetRepository
) : ViewModel() {

    private val _totalValue = MutableStateFlow<Long>(0)
    val totalValue: StateFlow<Long> = _totalValue

    /**
     * Obtiene el valor total del inventario desde el Repository
     */
    fun loadTotalValue() {
        viewModelScope.launch {
            val value = repository.getTotalValue()
            _totalValue.value = value
        }
    }
}
