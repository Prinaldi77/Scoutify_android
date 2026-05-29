package com.pab.scoutify.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pab.scoutify.databinding.FragmentRoleManagementBinding
import com.pab.scoutify.ui.auth.SessionManager

class RoleManagementFragment : Fragment() {
    private var _binding: FragmentRoleManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Load current access status
        binding.cbSiswaMemberAccess.isChecked = sessionManager.hasMemberAccess()

        binding.btnSaveAccess.setOnClickListener {
            val isChecked = binding.cbSiswaMemberAccess.isChecked
            sessionManager.setMemberAccess(isChecked)
            
            Toast.makeText(requireContext(), "Perubahan akses berhasil disimpan! Siswa sekarang ${if (isChecked) "memiliki" else "tidak memiliki"} akses Anggota.", Toast.LENGTH_LONG).show()
        }

        binding.btnOpenSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
