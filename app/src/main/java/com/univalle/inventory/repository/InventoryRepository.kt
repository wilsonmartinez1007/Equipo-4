package com.univalle.inventory.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.ui.model.UserRequest
import com.univalle.inventory.ui.model.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class InventoryRepository {

    // ✅ AUTENTICACIÓN (Firebase Auth)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // ✅ FIRESTORE
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRef = firestore.collection("products")

    // ----------------------
    // AUTH: LOGIN / REGISTRO
    // ----------------------

    suspend fun loginUser(
        email: String,
        password: String,
        isLogin: (Boolean) -> Unit
    ) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    isLogin(it.isSuccessful)
                }
        } else {
            isLogin(false)
        }
    }

    suspend fun registerUser(
        userRequest: UserRequest,
        userResponse: (UserResponse) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                firebaseAuth.createUserWithEmailAndPassword(
                    userRequest.email,
                    userRequest.password
                )
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

    // ----------------------
    // INVENTARIO (Firestore)
    // ----------------------

    // HU 4.0: guardar SOLO para el usuario logueado
    suspend fun saveInventory(
        inventory: Inventory,
        messageResponse: (String) -> Unit
    ) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail == null) {
            messageResponse("No hay usuario autenticado")
            return
        }

        try {
            withContext(Dispatchers.IO) {
                collectionRef
                    .document(inventory.id.toString())
                    .set(
                        hashMapOf(
                            "id" to inventory.id,
                            "name" to inventory.name,
                            "price" to inventory.price,
                            "quantity" to inventory.quantity,
                            // campo para saber de quién es el producto
                            "userEmail" to currentUserEmail
                        )
                    )
                    .await()
            }
            messageResponse("El inventario ha sido guardado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al guardar el inventario: ${e.message}")
        }
    }

    // HU 3.0: lista SOLO de productos del usuario actual
    suspend fun getListInventory(): List<Inventory> =
        withContext(Dispatchers.IO) {
            val currentUserEmail = firebaseAuth.currentUser?.email
                ?: return@withContext emptyList<Inventory>()  // si no hay usuario, lista vacía

            try {
                val snapshot = collectionRef
                    .whereEqualTo("userEmail", currentUserEmail)   // 👈 filtro por usuario
                    .get()
                    .await()

                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Inventory::class.java)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

    // Obtener por ID DESDE Firestore (podrías usarlo solo sobre items ya filtrados)
    suspend fun getInventoryByIdFromFirestore(itemId: Int): Inventory? =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = collectionRef.document(itemId.toString()).get().await()
                snapshot.toObject(Inventory::class.java)
            } catch (e: Exception) {
                null
            }
        }

    // Alias para mantener compatibilidad con getInventoryById() que usa el ViewModel
    suspend fun getInventoryById(itemId: Int): Inventory? =
        getInventoryByIdFromFirestore(itemId)

    // Actualizar en Firestore (asume que solo actualizas ítems que ya llegaron filtrados)
    suspend fun updateInventoryInFirestore(
        inventory: Inventory,
        messageResponse: (String) -> Unit
    ) {
        val currentUserEmail = firebaseAuth.currentUser?.email
        if (currentUserEmail == null) {
            messageResponse("No hay usuario autenticado")
            return
        }

        try {
            withContext(Dispatchers.IO) {
                collectionRef
                    .document(inventory.id.toString())
                    .set(
                        hashMapOf(
                            "id" to inventory.id,
                            "name" to inventory.name,
                            "price" to inventory.price,
                            "quantity" to inventory.quantity,
                            "userEmail" to currentUserEmail
                        )
                    )
                    .await()
            }
            messageResponse("Producto actualizado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al actualizar: ${e.message}")
        }
    }

    // LiveData observando cambios en Firestore (en tiempo real, también filtrable por usuario si quisieras)
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

    // Eliminar por id en Firestore (para el usuario actual)
    suspend fun deleteById(
        itemId: Int,
        messageResponse: (String) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                collectionRef
                    .document(itemId.toString())
                    .delete()
                    .await()
            }
            messageResponse("Producto eliminado con éxito")
        } catch (e: Exception) {
            messageResponse("Error al eliminar: ${e.message}")
        }
    }
}
