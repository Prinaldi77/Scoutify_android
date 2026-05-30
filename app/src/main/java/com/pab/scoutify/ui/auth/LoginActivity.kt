package com.pab.scoutify.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.databinding.ActivityLoginBinding
import com.pab.scoutify.ui.dashboard.DashboardActivity
import com.pab.scoutify.utils.Resource

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        // Cek jika sudah login, masuk menggunakan sidik jari
        if (sessionManager.isUserLogin()) {
            showBiometricDialog()
        }

        setupObservers()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Memproses..."
                }
                is Resource.Success -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Masuk Sekarang"
                    
                    val loginResponse = resource.data
                    if (loginResponse.success == true) {
                        val token = loginResponse.tokens?.accessToken
                        val user = loginResponse.user
                        val role = user?.role
                        
                        if (token != null && role != null) {
                            sessionManager.saveAuthToken(token)
                            sessionManager.saveRole(role)
                            
                            val userIdRaw = user.id
                            val userId = when (userIdRaw) {
                                is Number -> userIdRaw.toInt()
                                is String -> userIdRaw.toIntOrNull() ?: -1
                                else -> -1
                            }
                            val userName = user.name ?: ""

                            sessionManager.saveUserId(userId)
                            sessionManager.saveUserName(userName)
                            
                            // Injeksi Token ke HttpClient secara dinamis
                            RetrofitClient.authToken = token
                            
                            Toast.makeText(this, "Login Sukses sebagai $role!", Toast.LENGTH_SHORT).show()
                            moveToDashboard()
                        } else {
                            Toast.makeText(this, "Respons login tidak lengkap dari server.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val message = loginResponse.message ?: "Email/Password salah"
                        Toast.makeText(this, "Login Gagal: $message", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Masuk Sekarang"
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showBiometricDialog() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            // User canceled or error occurred
                            Toast.makeText(applicationContext, "Autentikasi biometrik dibatalkan. Silakan login manual.", Toast.LENGTH_SHORT).show()
                        }

                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            Toast.makeText(applicationContext, "Autentikasi Biometrik Berhasil! ✅", Toast.LENGTH_SHORT).show()
                            RetrofitClient.authToken = sessionManager.fetchAuthToken()
                            moveToDashboard()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(applicationContext, "Sidik jari tidak cocok. Silakan coba lagi.", Toast.LENGTH_SHORT).show()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Masuk Ke Scoutify")
                    .setSubtitle("Gunakan Sidik Jari Anda untuk melanjutkan")
                    .setNegativeButtonText("Batal")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                // Device does not support or hasn't enrolled biometrics
                Toast.makeText(this, "Perangkat tidak mendukung biometrik atau belum diatur. Silakan login manual.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun moveToDashboard() {
        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}
