package com.univalle.inventory.viewmodel

import android.app.Application
import android.os.Message
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.repository.InventoryRepository
import kotlinx.coroutines.launch

class InventoryViewModel (application: Application): AndroidViewModel(application){
    val context = getApplication<Application>()
    private val inventoryRepository = InventoryRepository(context)
    private val _progresState = MutableLiveData(false)

    fun saveInventory(inventory: Inventory, message:(String)-> Unit){
        viewModelScope.launch {
            _progresState.value = true
            try {
                inventoryRepository.saveInventory(inventory){msg -> message(msg)
                }
                _progresState.value=false
            }catch (e: Exception){
                _progresState.value=false
            }
        }
    }
}