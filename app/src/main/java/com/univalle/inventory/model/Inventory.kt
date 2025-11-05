package com.univalle.inventory.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//creamos el objeto con sus atributos similares a los campos que rebido al comprar un producto
@Entity
data class Inventory (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val price: Int,
    val quantity: Int
): Serializable