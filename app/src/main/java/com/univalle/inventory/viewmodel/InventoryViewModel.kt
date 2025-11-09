package com.univalle.inventory.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.repository.InventoryRepository
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(getApplication())

    // LiveData para la lista del Home
    private val _listInventory = MutableLiveData<List<Inventory>>(emptyList())
    val listInventory: LiveData<List<Inventory>> = _listInventory

    // Loader
    private val _progressState = MutableLiveData(false)
    val progressState: LiveData<Boolean> = _progressState

    /** Carga la lista desde Room */
    fun getListInventory() {
        viewModelScope.launch {
            _progressState.value = true
            try {
                _listInventory.value = repository.getListInventory()
            } catch (e: Exception) {
                _listInventory.value = emptyList()
            } finally {
                _progressState.value = false
            }
        }
    }

    /** Guardar (HU 4.0) */
    fun saveInventory(inventory: Inventory, message: (String) -> Unit) {
        viewModelScope.launch {
            _progressState.value = true
            try {
                repository.saveInventory(inventory, message)
                // refrescar lista si hace falta:
                _listInventory.value = repository.getListInventory()
            } finally {
                _progressState.value = false
            }
        }
    }

    // Nueva función para obtener un producto por ID
    fun getInventoryById(itemId: Int): LiveData<Inventory?> {
        val inventoryLiveData = MutableLiveData<Inventory?>()
        viewModelScope.launch {
            try {
                val inventory = repository.getInventoryById(itemId)
                inventoryLiveData.postValue(inventory)
            } catch (e: Exception) {
                inventoryLiveData.postValue(null)
            }
        }
        return inventoryLiveData
    }
}

