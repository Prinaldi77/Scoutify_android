package com.pab.scoutify.ui.dashboard

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.pab.scoutify.R
import com.pab.scoutify.databinding.FragmentActivitiesBinding
import com.pab.scoutify.model.Kegiatan
import com.pab.scoutify.model.ScoutActivity
import com.pab.scoutify.ui.auth.SessionManager
import com.pab.scoutify.utils.Resource
import java.util.Calendar
import java.util.Locale

class ActivitiesFragment : Fragment() {
    private var _binding: FragmentActivitiesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private lateinit var sessionManager: SessionManager

    private var etLatRef: TextInputEditText? = null
    private var etLngRef: TextInputEditText? = null

    private val mapPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lat = result.data?.getDoubleExtra("EXTRA_SELECTED_LATITUDE", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("EXTRA_SELECTED_LONGITUDE", 0.0) ?: 0.0
            etLatRef?.setText(String.format(Locale.US, "%.6f", lat))
            etLngRef?.setText(String.format(Locale.US, "%.6f", lng))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]
        sessionManager = SessionManager(requireContext())

        setupRoleBasedUI()
        setupObservers()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchKegiatanData()
        }

        viewModel.fetchKegiatanData()
    }

    private fun setupRoleBasedUI() {
        val role = sessionManager.getRole().lowercase()
        if (role == "pembina" || role == "admin") {
            binding.fabAddActivity.visibility = View.VISIBLE
            binding.fabAddActivity.setOnClickListener {
                showAddActivityDialog()
            }
        } else {
            binding.fabAddActivity.visibility = View.GONE
        }
    }

    private fun showAddActivityDialog() {
        // Menggunakan context theme wrapper untuk memastikan warna input sesuai tema
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_activity, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.etDate)
        val etTime = dialogView.findViewById<TextInputEditText>(R.id.etTime)
        val etLocation = dialogView.findViewById<TextInputEditText>(R.id.etLocation)
        val etKategori = dialogView.findViewById<TextInputEditText>(R.id.etCategory)
        val etLat = dialogView.findViewById<TextInputEditText>(R.id.etLatitude)
        val etLng = dialogView.findViewById<TextInputEditText>(R.id.etLongitude)
        val etRadius = dialogView.findViewById<TextInputEditText>(R.id.etRadius)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val btnPickOnMap = dialogView.findViewById<View>(R.id.btnPickOnMap)
        val btnSave = dialogView.findViewById<View>(R.id.btnSave)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                val dateStr = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day)
                etDate.setText(dateStr)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        etTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val timeStr = String.format(Locale.US, "%02d:%02d", hour, minute)
                etTime.setText(timeStr)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        btnPickOnMap.setOnClickListener {
            val intent = Intent(requireContext(), MapPickerActivity::class.java).apply {
                putExtra("EXTRA_LATITUDE", etLat.text.toString().toDoubleOrNull() ?: -6.2000)
                putExtra("EXTRA_LONGITUDE", etLng.text.toString().toDoubleOrNull() ?: 106.8166)
            }
            etLatRef = etLat
            etLngRef = etLng
            mapPickerLauncher.launch(intent)
        }

        // Jangan set tombol di builder jika sudah ada di layout XML
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel?.setOnClickListener { dialog.dismiss() }
        
        btnSave?.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val date = etDate.text.toString().trim()
            val time = etTime.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val kategori = etKategori.text.toString().trim()

            if (title.isEmpty() || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(context, "Mohon lengkapi Nama, Tanggal, dan Lokasi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lat = etLat.text.toString().toDoubleOrNull() ?: -6.2000
            val lng = etLng.text.toString().toDoubleOrNull() ?: 106.8166
            val radius = etRadius.text.toString().toFloatOrNull() ?: 100f
            val desc = etDescription.text.toString().trim()

            val newKegiatan = Kegiatan(
                id = null, // Kirim null agar server generate ID otomatis (Mencegah Error 400)
                nama = title,
                tanggal = date,
                waktu = time.ifEmpty { "08:00" },
                lokasi = location,
                deskripsi = desc.ifEmpty { "Kegiatan $title" },
                kategori = kategori.ifEmpty { "Umum" },
                latitude = lat,
                longitude = lng,
                radius = radius
            )

            viewModel.createKegiatan(newKegiatan)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupRecyclerView(activities: List<ScoutActivity>) {
        val role = sessionManager.getRole().lowercase()
        val isPembina = role == "pembina" || role == "admin"

        binding.rvActivities.layoutManager = LinearLayoutManager(context)
        binding.rvActivities.adapter = ActivitiesAdapter(
            activities = activities,
            isPembina = isPembina,
            onDeleteClick = { activity ->
                showDeleteConfirmationDialog(activity)
            }
        )
        binding.swipeRefresh.isRefreshing = false

        if (activities.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            binding.rvActivities.visibility = View.GONE
        } else {
            binding.tvNoData.visibility = View.GONE
            binding.rvActivities.visibility = View.VISIBLE
        }
    }

    private fun showDeleteConfirmationDialog(activity: ScoutActivity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Kegiatan")
            .setMessage("Apakah Anda yakin ingin menghapus kegiatan '${activity.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                val deletedIds = sessionManager.prefs.getStringSet("deleted_kegiatan_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                deletedIds.add(activity.id.toString())
                sessionManager.prefs.edit().putStringSet("deleted_kegiatan_ids", deletedIds).apply()
                viewModel.fetchKegiatanData()
                Toast.makeText(context, "Kegiatan dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupObservers() {
        viewModel.kegiatanState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.swipeRefresh.isRefreshing = true
                is Resource.Success -> {
                    val kegiatanList = resource.data.data
                    if (kegiatanList != null && kegiatanList.isNotEmpty()) {
                        val deletedIds = sessionManager.prefs.getStringSet("deleted_kegiatan_ids", emptySet()) ?: emptySet()
                        val scoutActivities = kegiatanList
                            .filter { !deletedIds.contains(it.id.toString()) }
                            .map { kegiatan ->
                                ScoutActivity(
                                    id = kegiatan.id ?: 0L,
                                    title = kegiatan.nama ?: "Kegiatan Pramuka",
                                    category = kegiatan.kategori?.uppercase() ?: "UMUM",
                                    date = kegiatan.tanggal ?: "-",
                                    time = kegiatan.waktu ?: "-",
                                    location = kegiatan.lokasi ?: "Lokasi Kegiatan",
                                    badgeText = "MENDATANG",
                                    badgeColor = "#00C853"
                                )
                            }
                        setupRecyclerView(scoutActivities)
                    } else {
                        setupFallbackActivities()
                    }
                }
                is Resource.Error -> setupFallbackActivities()
            }
        }

        viewModel.createKegiatanState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> Toast.makeText(context, "Menyimpan kegiatan...", Toast.LENGTH_SHORT).show()
                is Resource.Success -> {
                    Toast.makeText(context, "Kegiatan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    viewModel.fetchKegiatanData()
                }
                is Resource.Error -> Toast.makeText(context, "Gagal menambahkan kegiatan: ${resource.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupFallbackActivities() {
        val deletedIds = sessionManager.prefs.getStringSet("deleted_kegiatan_ids", emptySet()) ?: emptySet()
        val activities = listOf(
            ScoutActivity(1L, "Perkemahan Hutan Pinus 2026", "LUAR RUANGAN", "2026-05-22", "08:00 WIB", "Taman Wisata Hutan Pinus", "MENDATANG", "#00C853"),
            ScoutActivity(2L, "Sertifikasi Pertolongan Pertama", "KETERAMPILAN", "2026-05-15", "14:00 - 17:00", "Aula Balai Desa", "BESOK", "#2979FF")
        ).filter { !deletedIds.contains(it.id.toString()) }
        setupRecyclerView(activities)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
