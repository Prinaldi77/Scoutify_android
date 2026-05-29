package com.pab.scoutify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.pab.scoutify.databinding.ActivityMainBinding
import com.pab.scoutify.ui.auth.LoginActivity
import com.pab.scoutify.ui.auth.SessionManager
import com.pab.scoutify.ui.dashboard.DashboardActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Animasi Scale & Fade untuk Container Logo (Modern Design)
        val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        binding.cardLogo.startAnimation(scaleIn)

        // 2. Animasi Fade In untuk Brand Identity (AppName & Slogan)
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 1500
            startOffset = 500 // Muncul secara elegan setelah logo mulai membesar
        }
        binding.tvAppName.startAnimation(fadeIn)
        binding.tvSlogan.startAnimation(fadeIn)

        val sessionManager = SessionManager(this)

        // Splash Screen duration 3 detik
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isUserLogin()) {
                // Ensure auth token is injected into RetrofitClient upon launch
                com.pab.scoutify.api.RetrofitClient.authToken = sessionManager.fetchAuthToken()
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            
            // Smooth page transition
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }
}
