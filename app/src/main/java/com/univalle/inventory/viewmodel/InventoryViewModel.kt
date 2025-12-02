package com.univalle.inventory.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.repository.InventoryRepository
import kotlinx.coroutines.launch
import com.univalle.inventory.ui.model.UserRequest
import com.univalle.inventory.ui.model.UserResponse

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = InventoryRepository(getApplication())

    // Se actualiza solo cuando Room cambie la tabla
    val listInventory: LiveData<List<Inventory>> = repository.observeInventories()

    private val _progressState = MutableLiveData(false)
    val progressState: LiveData<Boolean> = _progressState

    private val _isRegister = MutableLiveData<UserResponse>()
    val isRegister: LiveData<UserResponse> = _isRegister
    fun registerUser(userRequest: UserRequest) {
        viewModelScope.launch {
            repository.registerUser(userRequest){ userResponse ->
                _isRegister.value = userResponse
            }
        }
    }

    fun loginUser(email: String, password: String, isLogin: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.loginUser(email, password, isLogin)
        }
    }


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

    // Obtener item por ID desde FIRESTORE
    fun getInventoryByIdFromFirestore(itemId: Int): LiveData<Inventory?> {
        val out = MutableLiveData<Inventory?>()
        viewModelScope.launch {
            out.postValue(repository.getInventoryByIdFromFirestore(itemId))
        }
        return out
    }

    // Actualizar en FIRESTORE
    fun updateInventoryInFirestore(inventory: Inventory, message: (String) -> Unit) {
        viewModelScope.launch {
            _progressState.value = true
            try { repository.updateInventoryInFirestore(inventory, message) }
            finally { _progressState.value = false }
        }
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




