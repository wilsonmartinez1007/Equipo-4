package com.univalle.inventory.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.univalle.inventory.data.InventoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository para el Widget de inventario
 * Implementa arquitectura MVVM con Repository Pattern
 * Utiliza Dagger Hilt para inyección de dependencias
 * Integra Firebase Auth, Firestore y Room Database
 */
@Singleton
class WidgetRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val localDao: InventoryDao
) {

    /**
     * VALIDAR AUTENTICACIÓN - Criterios 7, 10, 13, 14
     * Verifica si hay un usuario autenticado en Firebase Auth
     * @return true si el usuario está autenticado, false en caso contrario
     */
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * CALCULAR TOTAL INVENTARIO - Criterio 8
     * Calcula el valor total del inventario multiplicando precio * cantidad
     * de todos los productos en la base de datos local (Room)
     * @return Double con el valor total del inventario
     */
    suspend fun calculateTotalInventory(): Double = withContext(Dispatchers.IO) {
        try {
            localDao.getAllInventories().sumOf {
                (it.price * it.quantity).toDouble()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    /**
     * OBTENER VALOR TOTAL (para compatibilidad con Long)
     * Retorna el valor total del inventario como Long
     * @return Long con el valor total del inventario
     */
    suspend fun getTotalValue(): Long = withContext(Dispatchers.IO) {
        try {
            localDao.getAllInventories().sumOf {
                (it.price * it.quantity).toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    /**
     * FORMATEAR MONEDA - Criterio 9
     * Formatea un valor numérico a formato de moneda colombiana (COP)
     * @param value Valor a formatear
     * @return String con el valor formateado en pesos colombianos
     */
    fun formatCurrency(value: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 2
        return formatter.format(value)
    }

    /**
     * OBTENER BALANCE FORMATEADO
     * Obtiene el valor total del inventario y lo retorna formateado como moneda
     * @return String con el balance formateado en pesos colombianos
     */
    suspend fun getFormattedBalance(): String {
        val totalValue = getTotalValue()
        return formatCurrency(totalValue.toDouble())
    }
}