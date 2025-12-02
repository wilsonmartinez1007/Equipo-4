package com.univalle.inventory.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.univalle.inventory.R
import com.univalle.inventory.data.InventoryDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.univalle.inventory.model.Inventory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}