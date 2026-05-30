package com.pab.scoutify.ui.dashboard

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.pab.scoutify.R
import com.pab.scoutify.databinding.ActivityDashboardBinding
import com.pab.scoutify.ui.auth.SessionManager
import com.pab.scoutify.api.RetrofitClient

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. View Binding
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // 2. Token Injection
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            RetrofitClient.authToken = token
        }

        // 3. Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 4. Dynamic Menu Inflation based on Role
        val role = sessionManager.getRole() ?: "siswa"
        val menuRes = if (role.lowercase() == "pembina" || role.lowercase() == "admin") {
            R.menu.bottom_nav_menu_pembina
        } else {
            R.menu.bottom_nav_menu_siswa
        }
        binding.bottomNavigation.inflateMenu(menuRes)

        // Menghubungkan BottomNav dengan NavController
        binding.bottomNavigation.setupWithNavController(navController)
    }
}