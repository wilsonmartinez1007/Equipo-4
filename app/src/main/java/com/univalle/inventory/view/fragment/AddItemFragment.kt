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
import com.univalle.inventory.R
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController

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
        val toolbar = requireView().findViewById<Toolbar>(R.id.toolbarBase)
        toolbar.title = "Agregar Producto"
    }

    //llamamos los metodos
    private fun controladores(){
        regresarFragment()
        validarDatos()
        binding.btGuardarItem.setOnClickListener {
            getDataInventario()
        }
    }

    //recibimos los datos digitado en los campos de nuestro fragment y le
    //enviamos a inventoryViewModel un objeto Inventory con estos campos juntos
    //recibimos el mensaje de si el proceso resulto con exito
    private fun getDataInventario(){
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
            findNavController().popBackStack()
        }
        Log.d("test", inventory.toString())
    }

    //verificamos si todos los campos han sido llenados
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
    private fun regresarFragment(){
        val toolbar = requireView().findViewById<Toolbar>(R.id.toolbarBase)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

}