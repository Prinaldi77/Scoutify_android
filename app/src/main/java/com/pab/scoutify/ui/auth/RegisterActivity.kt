package com.pab.scoutify.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.pab.scoutify.databinding.ActivityRegisterBinding
import com.pab.scoutify.utils.Resource

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupObservers()

        binding.btnRegister.setOnClickListener {
            val name = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val gudep = binding.etGudep.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || gudep.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap lengkapi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(name, email, gudep, password)
        }

        binding.tvLogin.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    private fun setupObservers() {
        viewModel.registerState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Memproses..."
                }
                is Resource.Success -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar Sekarang"
                    Toast.makeText(this, "Pendaftaran Berhasil! Silakan Masuk.", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Daftar Sekarang"
                    Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
