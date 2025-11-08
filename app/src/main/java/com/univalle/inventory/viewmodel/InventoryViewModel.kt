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

    //funcion que permite enviarle al repository el inventario(campos sobre el producto)
    //ademas recibe y envia a addItemFragment el mensaje verificanto si el proceso de la BD resulto correcto
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

    fun getListInventory(){
        viewModelScope.launch {
            _progresState.value = true
            try {
                _listInventory.value = inventoryRepository.getListInventory()
                _progresState.value = false
            }catch (e: Exception){
                _progresState.value = false
            }
        }

    }
}