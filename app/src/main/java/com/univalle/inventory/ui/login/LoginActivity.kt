package com.univalle.inventory.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.univalle.inventory.databinding.ActivityLoginBinding
import com.univalle.inventory.utils.SessionManager
import com.univalle.inventory.view.MainActivity
import com.univalle.inventory.ui.model.UserRequest
import com.univalle.inventory.viewmodel.InventoryViewModel
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.univalle.inventory.R


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    private val inventoryViewModel: InventoryViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        session = SessionManager(this)

        // Si ya hay sesión guardada, saltar el login
        if (session.isLoggedIn()) {
            goToHome()
            return
        }
        sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE)
        setup()
        viewModelObserver()

    }
    private fun viewModelObserver(){
        observerIsRegister()
    }
    private  fun observerIsRegister(){
        inventoryViewModel.isRegister.observe(this){userResponse ->
            if (userResponse.isRegister){
                Toast.makeText(this, userResponse.message, Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putString("email", userResponse.email).apply()
                session.setLoggedIn(true)
                goToHome()
            } else {
                Toast.makeText(this, userResponse.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setup(){
        validarDatos()
        binding.btRegister.setOnClickListener {
            registerUser()
        }
        binding.btLogin.setOnClickListener {
            loginUser()
        }
    }
    private fun registerUser(){
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()
        val userRequest = UserRequest(email,password)

        if (email.isNotEmpty() && password.isNotEmpty()){
            inventoryViewModel.registerUser(userRequest)

        }else{
            Toast.makeText(this, "Campos Vacios", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loginUser() {
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()
        inventoryViewModel.loginUser(email,password){ isLogin ->
            if (isLogin){
                sharedPreferences.edit().putString("email",email).apply()
                session.setLoggedIn(true)
                goToHome()
            }else{
                Toast.makeText(this, "Login incorrecto", Toast.LENGTH_SHORT).show()
            }
        }


    }
    //verificamos si todos los campos han sido llenados
    private fun validarDatos(){
        val listEditText = listOf(binding.inputEmail, binding.inputPassword)

        for (editText in listEditText) {
            editText.addTextChangedListener {
                val isListFull = listEditText.all{
                    it.text.toString().isNotEmpty()
                }
                actualizarBoton(isListFull) { color ->
                    binding.btLogin.apply {
                        isEnabled = isListFull
                        setTextColor(ContextCompat.getColor(context, color))
                    }
                    binding.btRegister.isEnabled = isListFull
                }

            }
        }
    }
    private fun actualizarBoton(estado: Boolean, accion: (Int) -> Unit) {
        val color = if (estado) R.color.white else R.color.grisToolbar
        accion(color)
    }


    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            //  crea nueva task y limpia la pila para que la app no “se salga”
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        // no hace falta finish(): CLEAR_TASK ya quita el Login de la pila
    }
}
