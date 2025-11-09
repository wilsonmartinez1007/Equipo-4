package com.univalle.inventory.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.univalle.inventory.R
import com.univalle.inventory.data.InventoryDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.univalle.inventory.model.Inventory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // INSERTAR DATOS DE PRUEBA
        insertarDatosPrueba()
    }
    private fun insertarDatosPrueba() {
        CoroutineScope(Dispatchers.IO).launch {
            val dao = InventoryDB.getDatabase(this@MainActivity).inventoryDao()

            // Insertar productos
            if (dao.getCount() == 0) {
                dao.saveInventory(Inventory(name = "Laptop Dell", price = 1500000, quantity = 5))
                dao.saveInventory(Inventory(name = "Mouse Logitech", price = 45000, quantity = 20))
                dao.saveInventory(Inventory(name = "Teclado Mecánico", price = 180000, quantity = 15))
                dao.saveInventory(Inventory(name = "Monitor Samsung 24", price = 650000, quantity = 8))
                dao.saveInventory(Inventory(name = "Webcam HD", price = 120000, quantity = 12))
            }
        }

    }
}