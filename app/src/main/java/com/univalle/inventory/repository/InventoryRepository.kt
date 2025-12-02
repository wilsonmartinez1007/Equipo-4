package com.univalle.inventory.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.univalle.inventory.data.InventoryDB
import com.univalle.inventory.data.InventoryDao
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.ui.model.UserRequest
import com.univalle.inventory.ui.model.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class InventoryRepository(context: Context) {

    private val inventoryDao: InventoryDao = InventoryDB.getDatabase(context).inventoryDao()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRef = firestore.collection("products")

    suspend fun loginUser(email: String, password: String, isLogin: (Boolean) -> Unit) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        isLogin(true)
                    } else {
                        isLogin(false)
                    }
                }
        } else {
            isLogin(false)
        }
    }

    suspend fun registerUser(userRequest: UserRequest, userResponse: (UserResponse) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                firebaseAuth.createUserWithEmailAndPassword(userRequest.email, userRequest.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
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
                            if (error is FirebaseAuthUserCollisionException) {
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
            } catch (e: Exception) {
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
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail == null) {
            messageResponse("No hay usuario autenticado")
            return
        }
        try {
            withContext(Dispatchers.IO) {
                // Guardar en Firestore
                firestore.collection("products").document(inventory.id.toString()).set(
                    hashMapOf(
                        "id" to inventory.id,
                        "name" to inventory.name,
                        "price" to inventory.price,
                        "quantity" to inventory.quantity,
                        "userEmail" to currentUserEmail
                    )
                ).await()

                //  Guardar también en Room para el widget
                inventoryDao.saveInventory(inventory)
            }
            messageResponse("El inventario ha sido guardado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al guardar el inventario: ${e.message}")
        }
    }

    // HU 3.0: lista SOLO de productos del usuario actual + sincronización con Room
    suspend fun getListInventory(): List<Inventory> =
        withContext(Dispatchers.IO) {
            val currentUserEmail = firebaseAuth.currentUser?.email
                ?: return@withContext emptyList<Inventory>()

            try {
                // Obtener de Firestore
                val snapshot = collectionRef
                    .whereEqualTo("userEmail", currentUserEmail)
                    .get()
                    .await()

                val firestoreList = snapshot.documents.mapNotNull {
                    it.toObject(Inventory::class.java)
                }

                //  Sincronizar con Room para el widget
                // Limpiar Room antes de insertar (para evitar datos viejos)
                val roomList = inventoryDao.getAllInventories()
                roomList.forEach { inventoryDao.deleteInventoryById(it.id) }

                // Insertar datos actualizados de Firestore en Room
                firestoreList.forEach { inventoryDao.saveInventory(it) }

                firestoreList
            } catch (e: Exception) {
                emptyList()
            }
        }

    // Obtener por ID desde FIRESTORE
    suspend fun getInventoryByIdFromFirestore(itemId: Int): Inventory? =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = collectionRef.document(itemId.toString()).get().await()
                snapshot.toObject(Inventory::class.java)
            } catch (e: Exception) {
                null
            }
        }

    // Alias para compatibilidad
    suspend fun getInventoryById(itemId: Int): Inventory? =
        getInventoryByIdFromFirestore(itemId)

    // Actualizar en FIRESTORE
    suspend fun updateInventoryInFirestore(inventory: Inventory, messageResponse: (String) -> Unit) {
        try {
            withContext(Dispatchers.IO) {
                // Actualizar en Firestore
                collectionRef.document(inventory.id.toString())
                    .set(inventory)
                    .await()

                //  Actualizar también en Room
                inventoryDao.update(inventory)
            }
            messageResponse("Producto actualizado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al actualizar: ${e.message}")
        }
    }

    // LiveData en tiempo real (también filtrado por usuario)
    fun observeInventories(): LiveData<List<Inventory>> {
        val liveData = MutableLiveData<List<Inventory>>()

        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail == null) {
            liveData.postValue(emptyList())
            return liveData
        }

        collectionRef
            .whereEqualTo("userEmail", currentUserEmail)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Inventory::class.java) }
                    ?: emptyList()
                liveData.postValue(list)
            }

        return liveData
    }

    // Version en firebase
    suspend fun deleteFromFirestore(itemId: Int) {
        withContext(Dispatchers.IO) {
            try {
                // Eliminar de Firestore
                collectionRef.document(itemId.toString()).delete().await()

                //  Eliminar también de Room
                inventoryDao.deleteInventoryById(itemId)
            } catch (e: Exception) {
                // Manejar error si es necesario
            }
        }
    }
}