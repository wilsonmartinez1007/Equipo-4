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
import com.univalle.inventory.databinding.FragmentEditItemBinding
import com.univalle.inventory.viewmodel.InventoryViewModel
import com.univalle.inventory.R
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController

class EditItemFragment : Fragment() {

    private lateinit var binding: FragmentEditItemBinding
    private val inventoryViewModel: InventoryViewModel by viewModels()

    // Variable para almacenar el ID del producto que se está editando
    private var currentProductId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditItemBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return  binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireView().findViewById<Toolbar>(R.id.toolbarBase)
        toolbar.title = "Editar Producto"

        // Cargar datos del item con id 1
        cargarDatosItem(3)
        controladores()
    }

    // Cargar datos desde la base de datos
    private fun cargarDatosItem(itemId: Int) {
        inventoryViewModel.getInventoryById(itemId).observe(viewLifecycleOwner) { inventory ->
            inventory?.let {
                // Guardar el ID actual
                currentProductId = it.id

                // Mostrar el ID en el TextView
                binding.tvId.text = "ID: ${it.id}"

                // Precargar los datos en los campos
                binding.inputNombreProducto.setText(it.name)
                binding.inputPrecioProducto.setText(it.price.toString())
                binding.inputCantidadArticulos.setText(it.quantity.toString())

                Log.d("EditItem", "Datos cargados: $it")
            } ?: run {
                Toast.makeText(context, "No se encontró el producto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun controladores(){
        validarDatos()
        binding.btGuardarItem.setOnClickListener {
            actualizarInventario()
        }
    }

    private fun actualizarInventario(){
        val nombreProducto = binding.inputNombreProducto.text.toString()
        val precioProducto = binding.inputPrecioProducto.text.toString().toInt()
        val cantidadArticulos = binding.inputCantidadArticulos.text.toString().toInt()

        // Validación
        if (nombreProducto.isBlank()) {
            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }
        if (precioProducto <= 0) {
            Toast.makeText(context, "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }
        if (cantidadArticulos < 0) {
            Toast.makeText(context, "La cantidad no puede ser negativa", Toast.LENGTH_SHORT).show()
            return
        }

        val inventory = Inventory(
            id = currentProductId,  // ID almacenado
            name = nombreProducto,
            price = precioProducto,
            quantity = cantidadArticulos)

        inventoryViewModel.saveInventory(inventory){ message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // Navegar de regreso después de actualizar
            // findNavController().navigateUp()
        }
        Log.d("test", inventory.toString())
    }

    //verificamos si todos los campos han sido llenados
    private fun validarDatos(){
        val listEditText = listOf(
            binding.inputNombreProducto,
            binding.inputPrecioProducto,
            binding.inputCantidadArticulos
        )

        // Validación del nombre en tiempo real
        binding.inputNombreProducto.addTextChangedListener {
            if (it.toString().length > 40) {
                binding.inputNombreProducto.error = "Máximo 40 caracteres"
            } else {
                binding.inputNombreProducto.error = null
            }
        }

        // Validación del precio en tiempo real
        binding.inputPrecioProducto.addTextChangedListener {
            if (it.toString().length > 20) {
                binding.inputPrecioProducto.error = "Máximo 20 dígitos"
            } else {
                binding.inputPrecioProducto.error = null
            }
        }

        // Validación de la cantidad en tiempo real
        binding.inputCantidadArticulos.addTextChangedListener {
            if (it.toString().length > 4) {
                binding.inputCantidadArticulos.error = "Máximo 4 dígitos"
            } else {
                binding.inputCantidadArticulos.error = null
            }
        }

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