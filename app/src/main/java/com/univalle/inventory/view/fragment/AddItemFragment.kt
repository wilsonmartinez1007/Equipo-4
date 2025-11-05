package com.univalle.inventory.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.univalle.inventory.model.Inventory
import com.univalle.inventory.databinding.FragmentAddItemBinding
import com.univalle.inventory.viewmodel.InventoryViewModel


class AddItemFragment : Fragment() {

    private lateinit var binding: FragmentAddItemBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddItemBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return  binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        controladores()
    }

    private fun controladores(){
        validarDatos()
        binding.btGuardarItem.setOnClickListener {
            getDataInventario()
        }
    }
    private fun getDataInventario(){
        //codigo producto es entero
        val codigoProducto = binding.inputCodigoProducto.text.toString().toInt()
        val nombreProducto = binding.inputNombreProducto.text.toString()
        val precioProducto = binding.inputPrecioProducto.text.toString().toInt()
        val cantidadArticulos = binding.inputCantidadArticulos.text.toString().toInt()

        val inventory = Inventory(
            id = codigoProducto,
            name = nombreProducto,
            price = precioProducto,
            quantity = cantidadArticulos)
        inventoryViewModel.saveInventory(inventory){ message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        }
        Log.d("test", inventory.toString())
    }

    private fun validarDatos(){
        val listEditText = listOf(binding.inputCodigoProducto, binding.inputNombreProducto, binding.inputPrecioProducto, binding.inputCantidadArticulos)

        for (editText in listEditText) {
            editText.addTextChangedListener {
                val isListFull = listEditText.all{
                    it.text.toString().isNotEmpty()
                }
                binding.btGuardarItem.isEnabled = isListFull
            }
        }
    }

}