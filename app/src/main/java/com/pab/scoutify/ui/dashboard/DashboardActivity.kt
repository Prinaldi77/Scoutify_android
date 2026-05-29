package com.pab.scoutify.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pab.scoutify.R
import com.pab.scoutify.databinding.ActivityDashboardBinding
import com.pab.scoutify.ui.auth.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // 1. Ambil Role dari Session
        val role = sessionManager.getRole()
        
        // 2. Terapkan RBAC pada Bottom Navigation
        applyRBAC(role)

        // 3. Cek Token & Injeksi ke RetrofitClient
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            com.pab.scoutify.api.RetrofitClient.authToken = token
        } else {
            Toast.makeText(this, "Silakan Login Kembali", Toast.LENGTH_SHORT).show()
        }

        // Setup Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        androidx.navigation.ui.NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
    }

    private fun applyRBAC(role: String) {
        val menu = binding.bottomNavigation.menu
        menu.clear()

        // 1. Permanent item: Home (always present)
        menu.add(0, R.id.nav_home, 0, "Beranda").setIcon(R.drawable.ic_dashboard)

        // 2. Conditional items based on role
        when (role.lowercase()) {
            "pembina" -> {
                // Pembina holds absolute administrative rights (Akses, Anggota, Presensi)
                menu.add(0, R.id.nav_role_mgmt, 1, "Akses").setIcon(R.drawable.ic_security)
                menu.add(0, R.id.nav_anggota, 2, "Anggota").setIcon(R.drawable.ic_people)
                menu.add(0, R.id.nav_presensi, 3, "Presensi").setIcon(R.drawable.ic_attendance)
            }
            "siswa" -> {
                menu.add(0, R.id.nav_presensi, 1, "Presensi").setIcon(R.drawable.ic_attendance)
                menu.add(0, R.id.nav_kegiatan, 2, "Kegiatan").setIcon(R.drawable.ic_calendar)
                menu.add(0, R.id.nav_anggota, 3, "Anggota").setIcon(R.drawable.ic_people)
            }
            else -> {
                // Fallback for default siswa
                menu.add(0, R.id.nav_presensi, 1, "Presensi").setIcon(R.drawable.ic_attendance)
                menu.add(0, R.id.nav_kegiatan, 2, "Kegiatan").setIcon(R.drawable.ic_calendar)
                menu.add(0, R.id.nav_anggota, 3, "Anggota").setIcon(R.drawable.ic_people)
            }
        }

        // 3. Permanent item: Profil (always present at the end)
        menu.add(0, R.id.nav_profil, 4, "Profil").setIcon(R.drawable.ic_person)
    }
}
