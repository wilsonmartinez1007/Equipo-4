package com.univalle.inventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.repository.InventoryRepository
import com.univalle.inventory.ui.model.UserRequest
import com.univalle.inventory.ui.model.UserResponse
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val inventoryRepository = InventoryRepository()

    // --------- INVENTARIO (Firebase Firestore) ---------

    // Lista que usará el Home (HU 3.0)
    private val _listInventory = MutableLiveData<List<Inventory>>()
    val listInventory: LiveData<List<Inventory>> get() = _listInventory

    // Estado de progreso (loader)
    private val _progressState = MutableLiveData(false)
    val progressState: LiveData<Boolean> = _progressState

    // --------- AUTH (Firebase Auth) ---------

    // Resultado de registro
    private val _isRegister = MutableLiveData<UserResponse>()
    val isRegister: LiveData<UserResponse> = _isRegister

    // REGISTRO de usuario con Firebase Auth
    fun registerUser(userRequest: UserRequest) {
        viewModelScope.launch {
            inventoryRepository.registerUser(userRequest) { userResponse ->
                _isRegister.postValue(userResponse)
            }
        }
    }

    // LOGIN de usuario con Firebase Auth
    fun loginUser(email: String, password: String, isLogin: (Boolean) -> Unit) {
        viewModelScope.launch {
            inventoryRepository.loginUser(email, password, isLogin)
        }
    }

    // --------- CRUD INVENTARIO (Firestore) ---------

    // HU 3.0: obtener lista de inventario DESDE FIREBASE
    fun getListInventory() {
        viewModelScope.launch {
            _progressState.value = true
            try {
                val list = inventoryRepository.getListInventory()
                _listInventory.value = list
            } catch (e: Exception) {
                _listInventory.value = emptyList()
            } finally {
                _progressState.value = false
            }
        }
    }

    // HU 4.0: guardar inventario en Firestore
    fun saveInventory(inventory: Inventory, message: (String) -> Unit) {
        viewModelScope.launch {
            _progressState.value = true
            try {
                inventoryRepository.saveInventory(inventory, message)
            } finally {
                _progressState.value = false
            }
        }
    }

    // Obtener inventario por ID (usa alias del repo que ya va a Firestore)
    fun getInventoryById(itemId: Int): LiveData<Inventory?> {
        val out = MutableLiveData<Inventory?>()
        viewModelScope.launch {
            out.postValue(inventoryRepository.getInventoryById(itemId))
        }
        return out
    }

    // Si quieres diferenciar explícitamente "FromFirestore"
    fun getInventoryByIdFromFirestore(itemId: Int): LiveData<Inventory?> {
        val out = MutableLiveData<Inventory?>()
        viewModelScope.launch {
            out.postValue(inventoryRepository.getInventoryByIdFromFirestore(itemId))
        }
        return out
    }

    // Actualizar producto en Firestore
    fun updateInventoryInFirestore(inventory: Inventory, message: (String) -> Unit) {
        viewModelScope.launch {
            _progressState.value = true
            try {
                inventoryRepository.updateInventoryInFirestore(inventory, message)
            } finally {
                _progressState.value = false
            }
        }
    }

    // Eliminar producto en Firestore
    fun deleteInventoryById(itemId: Int, message: (String) -> Unit = {}) {
        viewModelScope.launch {
            _progressState.value = true
            try {
                inventoryRepository.deleteById(itemId, message)
            } finally {
                _progressState.value = false
            }
        }
    }
}
