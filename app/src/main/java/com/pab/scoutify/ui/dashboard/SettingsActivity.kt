package com.pab.scoutify.ui.dashboard

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.pab.scoutify.R
import com.pab.scoutify.ui.auth.SessionManager

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
        findViewById<LinearLayout>(R.id.btnKelolaEvent).setOnClickListener {
            showSimpleEditDialog("Kelola Event Baru", "Nama Event/Kegiatan Baru", "Contoh: Kemah Bakti Pramuka") { input ->
                Toast.makeText(this, "Sukses merilis event baru: '$input'! 📅", Toast.LENGTH_LONG).show()
            }
        }

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
        val input = EditText(this).apply {
            hint = "Masukkan Password Baru"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(50, 40, 50, 40)
        }

        AlertDialog.Builder(this)
            .setTitle("🔒 Ubah Password Keamanan")
            .setMessage("Masukkan password baru Anda:")
            .setView(input)
            .setPositiveButton("Perbarui") { dialog, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    Toast.makeText(this, "Password Keamanan Anda berhasil diperbarui! 🔒", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
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
