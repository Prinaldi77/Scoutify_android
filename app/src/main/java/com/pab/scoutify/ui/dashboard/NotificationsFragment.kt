package com.pab.scoutify.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pab.scoutify.databinding.FragmentNotificationsBinding
import com.pab.scoutify.model.Notification
import com.pab.scoutify.model.NotificationType

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val notifications = listOf(
            Notification(
                1, "Briefing Keamanan Wajib",
                "Semua anggota yang mengikuti Perkemahan Hutan Pinus wajib hadir di briefing keamanan Sabtu ini.",
                "🕒 2 JAM YANG LALU", NotificationType.WARNING
            ),
            Notification(
                2, "Syarat Kecakapan Baru Tersedia",
                "SKU tingkat baru untuk Teknologi Digital sudah tersedia. Hubungi Kak Wilson untuk detailnya.",
                "🕒 5 JAM YANG LALU", NotificationType.INFO
            ),
            Notification(
                3, "Update Pemeriksaan Atribut",
                "Mohon pastikan semua tanda kecakapan dijahit dengan benar sebelum inspeksi hari Kamis.",
                "🕒 KEMARIN", NotificationType.UPDATE
            ),
            Notification(
                4, "Target Penggalangan Dana Tercapai!",
                "Selamat! Kita telah melampaui target penggalangan dana untuk renovasi gudep.",
                "🕒 2 HARI YANG LALU", NotificationType.SUCCESS
            )
        )

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = NotificationAdapter(notifications)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}