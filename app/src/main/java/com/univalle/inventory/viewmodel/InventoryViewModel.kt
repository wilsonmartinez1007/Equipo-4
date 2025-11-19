package com.univalle.inventory.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.repository.InventoryRepository
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InventoryRepository(getApplication())

    // Se actualiza solo cuando Room cambie la tabla
    val listInventory: LiveData<List<Inventory>> = repository.observeInventories()

    private val _progressState = MutableLiveData(false)
    val progressState: LiveData<Boolean> = _progressState

    fun getListInventory() {
        // Opcional (solo para mostrar loader breve en la 1ª carga)
        viewModelScope.launch {
            _progressState.value = true
            try { repository.getListInventory() } finally { _progressState.value = false }
        }
    }

    fun saveInventory(inventory: Inventory, message: (String) -> Unit) {
        viewModelScope.launch {
            _progressState.value = true
            try { repository.saveInventory(inventory, message) }
            finally { _progressState.value = false }
        }
    }

    fun getInventoryById(itemId: Int): LiveData<Inventory?> {
        val out = MutableLiveData<Inventory?>()
        viewModelScope.launch {
            out.postValue(repository.getInventoryById(itemId))
        }
        return out
    }

    fun deleteInventoryById(itemId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _progressState.value = true
            try {
                repository.deleteById(itemId)
                onSuccess()
            } finally {
                _progressState.value = false
            }
        }
    }
}




