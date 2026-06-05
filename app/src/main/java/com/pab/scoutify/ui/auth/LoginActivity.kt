package com.pab.scoutify.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.pab.scoutify.R
import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.databinding.ActivityLoginBinding
import com.pab.scoutify.ui.dashboard.DashboardActivity
import com.pab.scoutify.utils.Resource

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel
    private var lastEmail = ""
    private var lastPassword = ""
 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
 
        // Animate layouts
        val slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val scaleIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_in)
        binding.ivLogo.startAnimation(scaleIn)
        binding.cardLogin.startAnimation(slideUp)
 
        sessionManager = SessionManager(this)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
 
        setupObservers()
        checkBiometricAvailability()
 
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
 
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lastEmail = email
            lastPassword = password
            viewModel.login(email, password)
        }
    }

    private fun checkBiometricAvailability() {
        val biometricManager = androidx.biometric.BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
            androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        val savedEmail = sessionManager.prefs.getString("saved_email", null)
        val savedPassword = sessionManager.prefs.getString("saved_password", null)

        if (canAuthenticate == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS &&
            !savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()
        ) {
            binding.btnBiometricLogin.visibility = android.view.View.VISIBLE
            binding.btnBiometricLogin.setOnClickListener {
                showBiometricPrompt(savedEmail, savedPassword)
            }
        } else {
            binding.btnBiometricLogin.visibility = android.view.View.GONE
        }
    }

    private fun showBiometricPrompt(email: String, password: String) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(this)
        val biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(this@LoginActivity, "Autentikasi Biometrik Berhasil! 🔓", Toast.LENGTH_SHORT).show()
                    viewModel.login(email, password)
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // If canceled by user, don't show toast error
                    if (errorCode != androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        Toast.makeText(this@LoginActivity, "Autentikasi error: $errString", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginActivity, "Sidik jari tidak cocok!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login Biometrik")
            .setSubtitle("Gunakan sidik jari Anda untuk masuk ke akun")
            .setAllowedAuthenticators(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
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
 
                    val loginData = resource.data
                    val token = loginData.anyToken
 
                    if (!token.isNullOrEmpty()) {
                        // 1. Simpan Session
                        sessionManager.saveAuthToken(token)
                        sessionManager.saveRole(loginData.user?.role ?: "Siswa")
                        sessionManager.saveUserName(loginData.user?.name ?: "User")
                        sessionManager.saveUserEmail(loginData.user?.email ?: "")
                        sessionManager.saveGugusDepan(loginData.user?.gugusDepan ?: "")
                        sessionManager.saveNomorInduk(loginData.user?.nomorInduk ?: "")
                        sessionManager.saveJabatan(loginData.user?.jabatan ?: "")

                        // Simpan kredensial untuk login biometrik berikutnya
                        if (lastEmail.isNotEmpty() && lastPassword.isNotEmpty()) {
                            sessionManager.prefs.edit().apply {
                                putString("saved_email", lastEmail)
                                putString("saved_password", lastPassword)
                                apply()
                            }
                        }

                        // 2. Injeksi Token ke Retrofit Client secara global
                        RetrofitClient.authToken = token

                        Log.d("LoginSuccess", "Token saved: ${token.take(10)}...")
                        Toast.makeText(this, "Login Berhasil! Selamat Datang ${loginData.user?.name ?: ""}", Toast.LENGTH_SHORT).show()

                        // 3. PINDAH KE DASHBOARD
                        moveToDashboard()
                    } else {
                        Log.e("LoginError", "Token null dari server. Response: $loginData")
                        Toast.makeText(this, "Data token tidak ditemukan. Hubungi admin.", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Masuk Sekarang"
                    Log.e("LoginError", "Error: ${resource.message}")
                    Toast.makeText(this, "Gagal: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun moveToDashboard() {
        try {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("NavigationError", "Failed to move to Dashboard", e)
            Toast.makeText(this, "Gagal pindah ke Dashboard: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
