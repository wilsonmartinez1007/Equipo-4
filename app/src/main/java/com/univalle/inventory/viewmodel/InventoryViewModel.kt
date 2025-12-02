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

    // Lista que va a observar el Home
    private val _listInventory = MutableLiveData<List<Inventory>>()
    val listInventory: LiveData<List<Inventory>> get() = _listInventory

    // Loader/progreso
    private val _progressState = MutableLiveData(false)
    val progressState: LiveData<Boolean> = _progressState

    // Resultado de registro

    private val _isRegister = MutableLiveData<UserResponse>()
    val isRegister: LiveData<UserResponse> = _isRegister


    fun registerUser(userRequest: UserRequest) {
        viewModelScope.launch {
            repository.registerUser(userRequest) { userResponse ->
                _isRegister.value = userResponse
            }
        }
    }

    fun loginUser(email: String, password: String, isLogin: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.loginUser(email, password, isLogin)
        }
    }


    // HU 3.0: obtener lista SOLO del usuario logueado (Firestore)
    fun getListInventory() {
        viewModelScope.launch {
            _progressState.value = true
            try {
                val list = repository.getListInventory()   // 👈 ya viene filtrada por userEmail
                _listInventory.value = list
            } catch (e: Exception) {
                _listInventory.value = emptyList()
            } finally {
                _progressState.value = false
            }
        }
    }

    fun saveInventory(inventory: Inventory, message: (String) -> Unit) {
        viewModelScope.launch {
            _progressState.value = true
            try {
                repository.saveInventory(inventory, message)
            } finally {
                _progressState.value = false
            }
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
    //
    fun updateInventoryInFirestore(inventory: Inventory, message: (String) -> Unit) {
            viewModelScope.launch {
                _progressState.value = true
                try {
                    repository.updateInventoryInFirestore(inventory, message)
                } finally {
                    _progressState.value = false
                }
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




