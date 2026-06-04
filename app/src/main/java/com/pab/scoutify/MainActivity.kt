// C:/FOLDER KULIAH/SEMESTER 4/PRAKTIKUM PEMOGRAMAN APLIKASI BERGERAK/Scoutify/app/src/main/java/com/pab/scoutify/MainActivity.kt

package com.pab.scoutify

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pab.scoutify.ui.auth.LoginActivity
import com.pab.scoutify.ui.auth.SessionManager
import com.pab.scoutify.ui.dashboard.DashboardActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Gunakan layout splash screen Anda
        setContentView(R.layout.activity_main)

        // Animate elements
        val cardLogo = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardLogo)
        val tvAppName = findViewById<android.widget.TextView>(R.id.tvAppName)
        val tvSlogan = findViewById<android.widget.TextView>(R.id.tvSlogan)
        val scaleInAnim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_in)
        cardLogo?.startAnimation(scaleInAnim)
        tvAppName?.startAnimation(scaleInAnim)
        tvSlogan?.startAnimation(scaleInAnim)

        val sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.isUserLogin()) {
                // Injeksi token agar API dashboard tidak error
                com.pab.scoutify.api.RetrofitClient.authToken = sessionManager.fetchAuthToken()

                val intent = Intent(this, DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 3000)
    }
}