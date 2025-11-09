package com.univalle.inventory.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.univalle.inventory.R
import com.univalle.inventory.databinding.ActivityLoginBinding
import com.univalle.inventory.utils.SessionManager
import com.univalle.inventory.view.MainActivity
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // 🔹 Si ya hay sesión, ir directo al Home
        if (SessionManager(this).isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Animación Lottie (huella)
        val lottieView: LottieAnimationView = binding.lottieLogin
        lottieView.setAnimation(R.raw.finger)
        lottieView.repeatCount = LottieDrawable.INFINITE
        lottieView.playAnimation()

        binding.lottieLogin.setOnClickListener { iniciarAutenticacionBiometrica() }

        configurarBiometria()
    }

    private fun configurarBiometria() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // ✅ Aquí se MARCA la sesión iniciada
                SessionManager(this@LoginActivity).setLoggedIn(true)

                // Ir al Home y evitar volver al login con back
                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_CANCELED) return
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // El sistema ya muestra el mensaje
            }
        })
    }

    private fun iniciarAutenticacionBiometrica() {
        val bm = BiometricManager.from(this)
        when (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Autenticación con Biometría")
                    .setSubtitle("Coloca tu huella digital")
                    .setNegativeButtonText("Cancelar")
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(this, "Este dispositivo no tiene sensor de huella", Toast.LENGTH_LONG).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Toast.makeText(this, "No hay huellas registradas en el dispositivo", Toast.LENGTH_LONG).show()
            else ->
                Toast.makeText(this, "Biometría no disponible", Toast.LENGTH_LONG).show()
        }
    }
}
