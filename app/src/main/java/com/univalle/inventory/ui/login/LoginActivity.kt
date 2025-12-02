package com.univalle.inventory.ui.login

import android.appwidget.AppWidgetManager
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
import android.content.res.ColorStateList
import android.text.InputType
import android.text.method.PasswordTransformationMethod

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private lateinit var sharedPreferences: SharedPreferences

    private val inventoryViewModel: InventoryViewModel by viewModels()

    // --- Variables para validación de password ---
    private var visible = false
    private var started = false
    private val MIN_PASS = 6

    // --- Variables para soporte widget (HU1) ---
    private var fromWidget = false
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        session = SessionManager(this)
        sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE)

        // Recibe datos del widget
        fromWidget = intent.getBooleanExtra("from_widget", false)
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

        if (session.isLoggedIn()) {
            handleAfterAuth()
            return
        }

        setup()
        setupPasswordField()
        viewModelObserver()
    }

    private fun viewModelObserver() {
        inventoryViewModel.isRegister.observe(this) { userResponse ->
            if (userResponse.isRegister) {
                Toast.makeText(this, userResponse.message, Toast.LENGTH_SHORT).show()
                sharedPreferences.edit().putString("email", userResponse.email).apply()
                session.setLoggedIn(true)
                handleAfterAuth()
            } else {
                Toast.makeText(this, userResponse.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setup() {
        validarDatos()

        binding.btRegister.setOnClickListener {
            registerUser()
        }
        binding.btLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun registerUser() {
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            inventoryViewModel.registerUser(UserRequest(email, password))
        } else {
            Toast.makeText(this, "Campos Vacios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser() {
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        inventoryViewModel.loginUser(email, password) { isLogin ->
            if (isLogin) {
                sharedPreferences.edit().putString("email", email).apply()
                session.setLoggedIn(true)
                handleAfterAuth()
            } else {
                Toast.makeText(this, "Login incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarDatos() {
        val listEditText = listOf(binding.inputEmail, binding.inputPassword)

        for (editText in listEditText) {
            editText.addTextChangedListener {
                val isListFull = listEditText.all { it.text.toString().isNotEmpty() }
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

    // --- Flujo unificado para manejar después de autenticación (HU1) ---
    private fun handleAfterAuth() {
        if (fromWidget && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            returnToWidget()
            return
        }
        goToHome()
    }

    private fun returnToWidget() {
        finish()
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    // --- Validación y toggle de password ---
    private fun setupPasswordField() = with(binding) {
        txPassword.setEndIconOnClickListener { togglePass() }
        paintPassOk()

        inputPassword.addTextChangedListener {
            val p = it?.toString().orEmpty()
            if (p.isNotEmpty()) started = true
            if (!started) paintPassOk() else if (p.length < MIN_PASS) paintPassError() else paintPassOk()
        }
    }

    private fun togglePass() = with(binding) {
        visible = !visible
        val pos = inputPassword.selectionEnd.coerceAtLeast(0)

        if (visible) {
            inputPassword.transformationMethod = null
            inputPassword.inputType = InputType.TYPE_CLASS_NUMBER
            txPassword.endIconDrawable = ContextCompat.getDrawable(this@LoginActivity, R.drawable.eye_close)
        } else {
            inputPassword.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            inputPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            txPassword.endIconDrawable = ContextCompat.getDrawable(this@LoginActivity, R.drawable.eye_open)
        }
        inputPassword.setSelection(pos)
    }

    private fun paintPassError() = with(binding) {
        txPassword.error = "Mínimo 6 dígitos"
        txPassword.setErrorTextColor(ColorStateList.valueOf(ContextCompat.getColor(this@LoginActivity, R.color.red_error)))
        txPassword.setBoxStrokeColor(ContextCompat.getColor(this@LoginActivity, R.color.red_error))
    }

    private fun paintPassOk() = with(binding) {
        txPassword.error = null
        txPassword.setBoxStrokeColor(ContextCompat.getColor(this@LoginActivity, R.color.white))
    }
}