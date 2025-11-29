package com.univalle.inventory.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.univalle.inventory.data.InventoryDB
import com.univalle.inventory.data.InventoryDao
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.ui.model.UserRequest
import com.univalle.inventory.ui.model.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InventoryRepository(context: Context) {

    private val inventoryDao: InventoryDao = InventoryDB.getDatabase(context).inventoryDao()


    private val firebaseAuth = FirebaseAuth.getInstance()
    suspend fun registerUser(userRequest: UserRequest, userResponse: (UserResponse) -> Unit){
        withContext(Dispatchers.IO){
            try{
                firebaseAuth.createUserWithEmailAndPassword(userRequest.email, userRequest.password)
                    .addOnCompleteListener {task ->
                        if (task.isSuccessful){
                            val email = task.result?.user?.email
                            userResponse(
                                UserResponse(
                                    email = email,
                                    isRegister = true,
                                    message = "Registro Exitoso"
                                )
                            )
                        } else {
                            val error = task.exception
                            if (error is FirebaseAuthUserCollisionException){
                                userResponse(
                                    UserResponse(
                                        isRegister = false,
                                        message = "El usuario ya existe"
                                    )
                                )
                            } else {
                                userResponse(
                                    UserResponse(
                                        isRegister = false,
                                        message = "Error en el registro"
                                    )
                                )
                            }
                        }
                    }
            } catch (e: Exception){
                userResponse(
                    UserResponse(
                        isRegister = false,
                        message = e.message ?: "Error desconocido"
                    )
                )
            }
        }

    }

    // HU 4.0: guardar
    suspend fun saveInventory(inventory: Inventory, messageResponse: (String) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                inventoryDao.saveInventory(inventory)
            }
            messageResponse("El inventario ha sido guardado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al guardar el inventario: ${e.message}")
        }
    }

    // HU 3.0: lista para el Home

    suspend fun getListInventory(): List<Inventory> =
        withContext(Dispatchers.IO) { inventoryDao.getAllInventories() }

    suspend fun getInventoryById(itemId: Int): Inventory? =
        withContext(Dispatchers.IO) { inventoryDao.getInventoryById(itemId) }
    fun observeInventories(): LiveData<List<Inventory>> =
        inventoryDao.observeInventories()
    // (Opcional) eliminar por id
    suspend fun deleteById(itemId: Int) =
        withContext(Dispatchers.IO) { inventoryDao.deleteInventoryById(itemId) }

}
