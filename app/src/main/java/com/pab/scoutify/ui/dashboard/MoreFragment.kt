package com.pab.scoutify.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.pab.scoutify.R
import com.google.android.material.card.MaterialCardView

class MoreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Jadwal Piket -> Membuka Halaman Kalender Piket
        view.findViewById<MaterialCardView>(R.id.cardPiket).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.nav_host_fragment, CalendarFragment())
                .addToBackStack(null)
                .commit()
            Toast.makeText(context, "Navigasi ke Kalender Piket sekretariat... 🧹📅", Toast.LENGTH_SHORT).show()
        }

        // 2. Peraturan Organisasi -> Dialog interaktif peraturan
        view.findViewById<MaterialCardView>(R.id.cardPeraturan).setOnClickListener {
            showPeraturanDialog()
        }

        // 3. Leaderboard -> Dialog daftar peringkat keaktifan (Wow effect!)
        view.findViewById<MaterialCardView>(R.id.cardLeaderboard).setOnClickListener {
            showLeaderboardDialog()
        }

        // 4. Tentang Scoutify -> Dialog pengembang & tech stack
        view.findViewById<MaterialCardView>(R.id.cardAbout).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showPeraturanDialog() {
        val rules = arrayOf(
            "1. Menjaga sopan santun dan tata krama sesama anggota.",
            "2. Wajib melakukan check-in piket tepat waktu di koordinat sekretariat.",
            "3. Mengenakan seragam Pramuka lengkap sesuai aturan di hari kegiatan.",
            "4. Menjaga kebersihan dan ketertiban area sekretariat.",
            "5. Saling menghormati perbedaan pendapat di dalam forum rapat."
        )

        AlertDialog.Builder(requireContext())
            .setTitle("⚖️ Pedoman & Tata Tertib")
            .setItems(rules, null)
            .setPositiveButton("Saya Mengerti", null)
            .show()
    }

    private fun showLeaderboardDialog() {
        val members = arrayOf(
            "🥇 1. Muhamad Prinaldi K. A. - 98 Poin (Sangat Aktif)",
            "🥈 2. Jameson Carter - 95 Poin (Aktif)",
            "🥉 3. Siswa Scoutify - 90 Poin (Aktif)",
            "🎖️ 4. Asep Sumantri - 82 Poin",
            "🎖️ 5. Budi Setiawan - 78 Poin"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("🏆 Peringkat Keaktifan Anggota")
            .setItems(members, null)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("🏕️ Tentang Scoutify")
            .setMessage(
                "Scoutify adalah aplikasi manajemen administrasi dan absensi GPS Pramuka berbasis mobile.\n\n" +
                "🚀 Fitur Utama:\n" +
                "- Absensi Geolocation Presisi\n" +
                "- Dynamic Role-Based Access (RBAC)\n" +
                "- Piket & Kegiatan Terintegrasi\n\n" +
                "💻 Tech Stack:\n" +
                "- Android Kotlin\n" +
                "- Retrofit2 & Leaflet WebView Map\n" +
                "- Node.js Express & MySQL Backend\n\n" +
                "Dibuat dengan ❤️ oleh Kelompok 4 Praktikum PAB."
            )
            .setPositiveButton("Keren!", null)
            .show()
    }
}
