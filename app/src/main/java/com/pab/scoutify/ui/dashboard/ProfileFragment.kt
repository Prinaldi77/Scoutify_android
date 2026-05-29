package com.pab.scoutify.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.pab.scoutify.R
import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.databinding.FragmentProfileBinding
import com.pab.scoutify.model.request.UpdateProfileRequest
import com.pab.scoutify.ui.auth.LoginActivity
import com.pab.scoutify.ui.auth.SessionManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        loadProfileData()

        binding.btnMyProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnEditAvatar.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            Toast.makeText(requireContext(), "Berhasil keluar dari akun! 😊", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::sessionManager.isInitialized) {
            loadProfileData()
        }
    }

    private fun loadProfileData() {
        val userName = sessionManager.getUserName().ifEmpty { "Anggota Scoutify" }
        val userRole = sessionManager.getRole()
        val userEmail = sessionManager.getUserEmail()
        val userEmoji = sessionManager.getUserAvatarEmoji()
        val userColor = sessionManager.getUserAvatarColor()

        binding.tvProfileName.text = userName
        binding.tvProfileRole.text = when (userRole.lowercase()) {
            "pembina" -> "Pembina Pramuka"
            else -> "Anggota Muda (Siswa)"
        }
        binding.tvProfileEmail.text = userEmail
        binding.tvProfileInitial.text = userEmoji
        try {
            binding.cardProfileAvatar.setCardBackgroundColor(Color.parseColor(userColor))
        } catch (e: Exception) {
            binding.cardProfileAvatar.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.primaryLightBrown)
            )
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val etName = dialogView.findViewById<TextInputEditText>(R.id.etEditName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etEditEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etEditPassword)
        val layoutEmoji = dialogView.findViewById<LinearLayout>(R.id.layoutEmojiContainer)
        val layoutColor = dialogView.findViewById<LinearLayout>(R.id.layoutColorContainer)

        // Populate current values
        etName.setText(sessionManager.getUserName())
        etEmail.setText(sessionManager.getUserEmail())

        var selectedEmoji = sessionManager.getUserAvatarEmoji()
        var selectedColor = sessionManager.getUserAvatarColor()

        // Populate Emojis
        val emojiList = listOf("🏕️", "🧭", "🔥", "🦅", "🐯", "🦁", "🌟", "🎗️", "🎒", "🪓")
        val emojiCardViews = mutableListOf<MaterialCardView>()

        emojiList.forEach { emoji ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(120, 120).apply { setMargins(12, 8, 12, 8) }
                radius = 60f
                strokeWidth = if (emoji == selectedEmoji) 6 else 0
                strokeColor = ContextCompat.getColor(requireContext(), R.color.primaryBrown)
                cardElevation = 2f
                setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            }
            val tv = TextView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                gravity = Gravity.CENTER
                textSize = 24f
                text = emoji
            }
            card.addView(tv)
            card.setOnClickListener {
                selectedEmoji = emoji
                emojiCardViews.forEach { it.strokeWidth = 0 }
                card.strokeWidth = 6
            }
            emojiCardViews.add(card)
            layoutEmoji.addView(card)
        }

        // Populate Colors
        val colorList = listOf("#8D6E63", "#4E342E", "#2E7D32", "#1B5E20", "#EF6C00", "#D84315", "#00695C", "#1565C0")
        val colorCardViews = mutableListOf<MaterialCardView>()

        colorList.forEach { colorStr ->
            val card = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100).apply { setMargins(12, 8, 12, 8) }
                radius = 50f
                strokeWidth = if (colorStr == selectedColor) 6 else 0
                strokeColor = Color.parseColor("#FFFFFF")
                cardElevation = 2f
                setCardBackgroundColor(Color.parseColor(colorStr))
            }
            card.setOnClickListener {
                selectedColor = colorStr
                colorCardViews.forEach { it.strokeWidth = 0 }
                card.strokeWidth = 6
            }
            colorCardViews.add(card)
            layoutColor.addView(card)
        }

        dialogView.findViewById<View>(R.id.btnCancelEdit).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnSaveEdit).setOnClickListener {
            val newName = etName.text.toString().trim()
            val newPassword = etPassword.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(context, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                Toast.makeText(context, "Password baru minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save local cosmetics immediately
            sessionManager.saveUserAvatarEmoji(selectedEmoji)
            sessionManager.saveUserAvatarColor(selectedColor)

            // Call backend API
            val btnSave = dialogView.findViewById<View>(R.id.btnSaveEdit)
            btnSave.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val request = UpdateProfileRequest(
                        name = newName,
                        password = if (newPassword.isNotEmpty()) newPassword else null
                    )
                    val response = RetrofitClient.instance.updateProfile(request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        sessionManager.saveUserName(newName)
                        loadProfileData()
                        Toast.makeText(
                            context,
                            "Profil berhasil diperbarui! ✅" + if (newPassword.isNotEmpty()) " Password baru aktif." else "",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    } else {
                        val errorMsg = try {
                            val json = response.errorBody()?.string()
                            org.json.JSONObject(json ?: "{}").getString("message")
                        } catch (e: Exception) { "Gagal menyimpan perubahan." }
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnSave.isEnabled = true
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}