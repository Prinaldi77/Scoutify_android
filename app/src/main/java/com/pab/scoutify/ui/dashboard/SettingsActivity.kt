package com.pab.scoutify.ui.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.pab.scoutify.R
import com.pab.scoutify.api.RetrofitClient
import com.pab.scoutify.model.request.UpdateProfileRequest
import com.pab.scoutify.ui.auth.LoginActivity
import com.pab.scoutify.ui.auth.SessionManager
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sessionManager = SessionManager(this)

        // 1. Back button click handler
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 2. Dynamic RBAC gating - Admin Section is strictly held by Pembina only!
        val role = sessionManager.getRole().lowercase()
        val layoutAdmin = findViewById<LinearLayout>(R.id.layoutAdminSection)

        if (role == "pembina") {
            layoutAdmin.visibility = View.VISIBLE
        } else {
            layoutAdmin.visibility = View.GONE
        }

        // 3. Set dynamic handlers for standard settings options
        findViewById<LinearLayout>(R.id.btnEditProfile).setOnClickListener {
            showEditProfileDialog()
        }

        findViewById<LinearLayout>(R.id.btnSettingsLogout).setOnClickListener {
            showLogoutConfirmationDialog()
        }

        findViewById<LinearLayout>(R.id.btnDigitalIdCard).setOnClickListener {
            Toast.makeText(this, "Kartu ID Digital Pramuka Anda sedang dimuat... 🪪✨", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btnNotifications).setOnClickListener {
            Toast.makeText(this, "Pengaturan notifikasi berhasil diperbarui! 🔔", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.btnSecurity).setOnClickListener {
            showChangePasswordDialog()
        }

        // 4. Set dynamic editable handlers for Admin Section

        findViewById<LinearLayout>(R.id.btnKelolaLokasi).setOnClickListener {
            showEditGeofenceDialog()
        }

        findViewById<LinearLayout>(R.id.btnKelolaDivisi).setOnClickListener {
            showSimpleEditDialog("💰 Kelola Kas Divisi", "Masukkan Saldo Kas Baru", "Contoh: Rp 500.000") { input ->
                sessionManager.saveMemberCashBalance(input)
                Toast.makeText(this, "Saldo Kas Divisi berhasil diperbarui dinamis menjadi '$input'! 💰✨", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<LinearLayout>(R.id.btnKelolaPiket).setOnClickListener {
            showSimpleEditDialog("Atur Jadwal Piket", "Nama Anggota Piket Baru", "Contoh: Aiden Thompson") { input ->
                Toast.makeText(this, "Jadwal piket untuk '$input' berhasil disimpan! 🧹", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<LinearLayout>(R.id.btnKelolaPeraturan).setOnClickListener {
            showSimpleEditDialog("Kelola Tata Tertib", "Edit/Tambah Peraturan Baru", "Contoh: 6. Selalu menjaga sopan santun") { input ->
                Toast.makeText(this, "Sukses memperbarui peraturan: '$input'! ⚖️", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<LinearLayout>(R.id.btnKelolaAnggota).setOnClickListener {
            showManageAccessDialog()
        }

        findViewById<LinearLayout>(R.id.btnRekapAbsensi).setOnClickListener {
            Toast.makeText(this, "Mengekspor laporan rekap kehadiran anggota Pramuka format PDF... 📈📄", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditGeofenceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_geofence, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etLat = dialogView.findViewById<TextInputEditText>(R.id.etLatitude)
        val etLng = dialogView.findViewById<TextInputEditText>(R.id.etLongitude)
        val etRad = dialogView.findViewById<TextInputEditText>(R.id.etRadius)
        val etStart = dialogView.findViewById<TextInputEditText>(R.id.etStartTime)
        val etEnd = dialogView.findViewById<TextInputEditText>(R.id.etEndTime)

        // Prepopulate current values from SessionManager
        etLat.setText(sessionManager.getGeofenceLat().toString())
        etLng.setText(sessionManager.getGeofenceLng().toString())
        etRad.setText(sessionManager.getGeofenceRadius().toString())
        etStart.setText(sessionManager.getAttendanceStartTime())
        etEnd.setText(sessionManager.getAttendanceEndTime())

        // Time pickers for etStart and etEnd
        etStart.isFocusable = false
        etStart.isClickable = true
        etStart.setOnClickListener {
            val currentVal = etStart.text.toString().trim()
            var hour = 0
            var minute = 0
            if (currentVal.isNotEmpty() && currentVal.contains(":")) {
                val parts = currentVal.split(":")
                hour = parts[0].toIntOrNull() ?: 0
                minute = parts[1].toIntOrNull() ?: 0
            }
            android.app.TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                etStart.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true).show()
        }

        etEnd.isFocusable = false
        etEnd.isClickable = true
        etEnd.setOnClickListener {
            val currentVal = etEnd.text.toString().trim()
            var hour = 23
            var minute = 59
            if (currentVal.isNotEmpty() && currentVal.contains(":")) {
                val parts = currentVal.split(":")
                hour = parts[0].toIntOrNull() ?: 23
                minute = parts[1].toIntOrNull() ?: 59
            }
            android.app.TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                etEnd.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true).show()
        }

        dialogView.findViewById<View>(R.id.btnCancelGeofence).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnSaveGeofence).setOnClickListener {
            val lat = etLat.text.toString().toDoubleOrNull()
            val lng = etLng.text.toString().toDoubleOrNull()
            val rad = etRad.text.toString().toFloatOrNull()
            val startTime = etStart.text.toString().trim()
            val endTime = etEnd.text.toString().trim()

            if (lat != null && lng != null && rad != null && startTime.isNotEmpty() && endTime.isNotEmpty()) {
                sessionManager.saveGeofenceLat(lat)
                sessionManager.saveGeofenceLng(lng)
                sessionManager.saveGeofenceRadius(rad)
                sessionManager.saveAttendanceStartTime(startTime)
                sessionManager.saveAttendanceEndTime(endTime)
                Toast.makeText(this, "Pengaturan Koordinat, Radius, & Batas Jam Absensi Berhasil Disimpan! 📍⏰", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Harap masukkan nilai koordinat, radius, dan waktu yang valid!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showSimpleEditDialog(title: String, hintText: String, sample: String, callback: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = sample
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Masukkan data baru untuk di-edit/disimpan:")
            .setView(input)
            .setPositiveButton("Simpan") { dialog, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    callback(text)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Data tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showChangePasswordDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }
        
        val etOldPassword = EditText(this).apply {
            hint = "Password Saat Ini"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 20)
            }
        }
        
        val etNewPassword = EditText(this).apply {
            hint = "Password Baru (Min. 6 Karakter)"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        
        container.addView(etOldPassword)
        container.addView(etNewPassword)

        AlertDialog.Builder(this)
            .setTitle("🔒 Ubah Password Keamanan")
            .setMessage("Masukkan password saat ini dan password baru Anda:")
            .setView(container)
            .setPositiveButton("Perbarui") { dialog, _ ->
                val oldPwd = etOldPassword.text.toString().trim()
                val newPwd = etNewPassword.text.toString().trim()
                
                if (oldPwd.isEmpty() || newPwd.isEmpty()) {
                    Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPwd.length < 6) {
                    Toast.makeText(this, "Password baru minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                lifecycleScope.launch {
                    try {
                        val request = com.pab.scoutify.model.request.ChangePasswordRequest(oldPwd, newPwd)
                        val response = RetrofitClient.instance.changePassword(request)
                        if (response.isSuccessful && response.body()?.success == true) {
                            // Update saved password for biometric login
                            sessionManager.prefs.edit().putString("saved_password", newPwd).apply()
                            Toast.makeText(this@SettingsActivity, "Password berhasil diubah! ✅", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            val errorMsg = try {
                                val json = response.errorBody()?.string()
                                org.json.JSONObject(json ?: "{}").getString("message")
                            } catch (e: Exception) { "Gagal mengubah password." }
                            Toast.makeText(this@SettingsActivity, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val dialog = AlertDialog.Builder(this)
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
        etEmail.isEnabled = false // Email is read-only

        var selectedEmoji = sessionManager.getUserAvatarEmoji()
        var selectedColor = sessionManager.getUserAvatarColor()

        // Populate Emojis
        val emojiList = listOf("🏕️", "🧭", "🔥", "🦅", "🐯", "🦁", "🌟", "🎗️", "🎒", "🪓")
        val emojiCardViews = mutableListOf<MaterialCardView>()

        emojiList.forEach { emoji ->
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(120, 120).apply { setMargins(12, 8, 12, 8) }
                radius = 60f
                strokeWidth = if (emoji == selectedEmoji) 6 else 0
                strokeColor = ContextCompat.getColor(this@SettingsActivity, R.color.primaryBrown)
                cardElevation = 2f
                setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            }
            val tv = TextView(this).apply {
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
            val card = MaterialCardView(this).apply {
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
                Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                Toast.makeText(this, "Password baru minimal 6 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save local cosmetics immediately
            sessionManager.saveUserAvatarEmoji(selectedEmoji)
            sessionManager.saveUserAvatarColor(selectedColor)

            // Call backend API
            val btnSave = dialogView.findViewById<View>(R.id.btnSaveEdit)
            btnSave.isEnabled = false

            lifecycleScope.launch {
                try {
                    val request = UpdateProfileRequest(
                        name = newName,
                        password = if (newPassword.isNotEmpty()) newPassword else null
                    )
                    val response = RetrofitClient.instance.updateProfile(request)
                    if (response.isSuccessful && response.body()?.success == true) {
                        sessionManager.saveUserName(newName)
                        if (newPassword.isNotEmpty()) {
                            sessionManager.prefs.edit().putString("saved_password", newPassword).apply()
                        }
                        Toast.makeText(
                            this@SettingsActivity,
                            "Profil berhasil diperbarui! ✅" + if (newPassword.isNotEmpty()) " Password baru aktif." else "",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    } else {
                        val errorMsg = try {
                            val json = response.errorBody()?.string()
                            org.json.JSONObject(json ?: "{}").getString("message")
                        } catch (e: Exception) { "Gagal menyimpan perubahan." }
                        Toast.makeText(this@SettingsActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btnSave.isEnabled = true
                }
            }
        }

        dialog.show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Keluar") { dialog, _ ->
                lifecycleScope.launch {
                    try {
                        RetrofitClient.instance.logout()
                    } catch (e: Exception) {
                        // Ignore and clear session anyway
                    }
                    sessionManager.clearSession()
                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    Toast.makeText(this@SettingsActivity, "Berhasil keluar dari akun! 😊", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun showManageAccessDialog() {
        val context = this
        val students = arrayOf("Aiden Thompson", "Bella Garcia", "Caleb Wilson", "Diana Prince", "Ethan Hunt")

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val tvLabelSelect = android.widget.TextView(context).apply {
            text = "Pilih Siswa:"
            setTextColor(0xFF212121.toInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val spinner = android.widget.Spinner(context).apply {
            adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, students)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 40)
            }
        }

        val tvLabelPerms = android.widget.TextView(context).apply {
            text = "Hak Akses yang Diperbolehkan:"
            setTextColor(0xFF212121.toInt())
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }

        val cbCatatan = com.google.android.material.checkbox.MaterialCheckBox(context).apply {
            text = "Akses Catatan (Notulensi & Ide)"
            textSize = 13f
        }

        val cbKas = com.google.android.material.checkbox.MaterialCheckBox(context).apply {
            text = "Akses Buku Kas (Uang Kas)"
            textSize = 13f
        }

        val cbAnggota = com.google.android.material.checkbox.MaterialCheckBox(context).apply {
            text = "Akses Kelola Daftar Anggota"
            textSize = 13f
        }

        container.addView(tvLabelSelect)
        container.addView(spinner)
        container.addView(tvLabelPerms)
        container.addView(cbCatatan)
        container.addView(cbKas)
        container.addView(cbAnggota)

        // Helper to update UI when spinner changes
        fun updateCheckboxes(student: String) {
            val keyCatatan = "access_catatan_${student.lowercase().replace(" ", "_")}"
            val keyKas = "access_kas_${student.lowercase().replace(" ", "_")}"
            val keyAnggota = "access_anggota_${student.lowercase().replace(" ", "_")}"

            cbCatatan.isChecked = sessionManager.prefs.getBoolean(keyCatatan, true)
            cbKas.isChecked = sessionManager.prefs.getBoolean(keyKas, true)
            cbAnggota.isChecked = sessionManager.prefs.getBoolean(keyAnggota, true)
        }

        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateCheckboxes(students[position])
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Load initial checkboxes for the first student
        updateCheckboxes(students[0])

        AlertDialog.Builder(context)
            .setTitle("🛡️ Manajemen Hak Akses Siswa")
            .setView(container)
            .setPositiveButton("Simpan") { dialog, _ ->
                val selectedStudent = students[spinner.selectedItemPosition]
                val keyCatatan = "access_catatan_${selectedStudent.lowercase().replace(" ", "_")}"
                val keyKas = "access_kas_${selectedStudent.lowercase().replace(" ", "_")}"
                val keyAnggota = "access_anggota_${selectedStudent.lowercase().replace(" ", "_")}"

                sessionManager.prefs.edit().apply {
                    putBoolean(keyCatatan, cbCatatan.isChecked)
                    putBoolean(keyKas, cbKas.isChecked)
                    putBoolean(keyAnggota, cbAnggota.isChecked)
                    apply()
                }

                Toast.makeText(context, "Hak akses untuk $selectedStudent berhasil diperbarui! 🛡️✨", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
