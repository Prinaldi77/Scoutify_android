package com.pab.scoutify.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pab.scoutify.R
import com.pab.scoutify.databinding.DialogManualAttendanceBinding
import com.pab.scoutify.databinding.FragmentAttendanceBinding
import com.pab.scoutify.model.Anggota
import com.pab.scoutify.model.AttendanceSession
import com.pab.scoutify.model.request.CheckOutRequest
import com.pab.scoutify.ui.auth.SessionManager
import com.pab.scoutify.utils.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: AttendanceAdapter
    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var sessionManager: SessionManager

    private var sessions = mutableListOf<AttendanceSession>()
    private var anggotasList = listOf<Anggota>()

    // Camera state
    private var selfiePhotoFile: File? = null
    private var selfieUri: Uri? = null
    // Pending check-in state (set before launching camera)
    private var pendingSession: AttendanceSession? = null
    private var pendingLocation: Location? = null
    private var pendingIsLate: Boolean = false
    // Track which session user has checked-in today (for checkout)
    private var checkedInSessionId: Long? = null

    // ─── Permission Launchers ─────────────────────────────────────────────────

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (!locationGranted) {
            showSnackbar("Izin lokasi diperlukan untuk presensi")
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            showSnackbar("Izin kamera diperlukan untuk selfie check-in")
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val session = pendingSession
            val location = pendingLocation
            if (session != null && location != null) {
                submitCheckInWithSelfie(session, location, pendingIsLate)
            }
        } else {
            // Camera cancelled — reset pending state
            pendingSession = null
            pendingLocation = null
            selfiePhotoFile = null
            selfieUri = null
            showSnackbar("Selfie dibatalkan. Presensi tidak dikirim.")
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attendanceViewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]
        dashboardViewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        setupRecyclerView()
        checkLocationPermissions()
        setupRoleBasedUI()
        setupObservers()
        setupCheckOutCard()
        fetchData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh today status every time fragment becomes visible
        attendanceViewModel.fetchTodayAttendance()
    }

    // ─── Setup ────────────────────────────────────────────────────────────────

    private fun fetchData() {
        dashboardViewModel.fetchKegiatanData()
        dashboardViewModel.fetchAnggotas()
        attendanceViewModel.fetchTodayAttendance()
    }

    // ─── Check-Out Card Setup ─────────────────────────────────────────────────

    private fun setupCheckOutCard() {
        binding.cardCheckOut.visibility = View.GONE // hidden by default until today status loads
        binding.btnCheckOut.setOnClickListener {
            // Find session matching the checked-in session ID
            val session = if (checkedInSessionId != null) {
                sessions.firstOrNull { it.id == checkedInSessionId } ?: sessions.firstOrNull()
            } else {
                sessions.firstOrNull()
            }
            if (session != null) {
                showCheckOutDialog(session)
            } else {
                showSnackbar("Tidak ada sesi aktif yang ditemukan")
            }
        }
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun setupRoleBasedUI() {
        val role = sessionManager.getRole().lowercase()
        if (role == "pembina" || role == "admin") {
            binding.btnNewAttendance.visibility = View.VISIBLE
            binding.btnNewAttendance.setOnClickListener { showManualAttendanceDialog() }
        } else {
            binding.btnNewAttendance.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        val role = sessionManager.getRole().lowercase()
        val isPembinaOrAdmin = (role == "pembina" || role == "admin")
        adapter = AttendanceAdapter(
            sessions = sessions,
            isPembina = isPembinaOrAdmin,
            onDeleteClick = { session -> showDeleteSessionDialog(session) },
            onItemClick = { session -> processAttendance(session) }
        )
        binding.rvAttendance.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AttendanceFragment.adapter
        }
    }

    // ─── Observers ────────────────────────────────────────────────────────────

    private fun setupObservers() {
        // Kegiatan list
        dashboardViewModel.kegiatanState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* swipe-refresh handled by parent */ }
                is Resource.Success -> {
                    val kegiatanList = resource.data.data
                    if (!kegiatanList.isNullOrEmpty()) {
                        val deletedIds =
                            sessionManager.prefs.getStringSet("deleted_kegiatan_ids", emptySet())
                                ?: emptySet()
                        val mapped = kegiatanList
                            .filter { !deletedIds.contains(it.id.toString()) }
                            .map { kegiatan ->
                                AttendanceSession(
                                    id = kegiatan.id,
                                    title = kegiatan.nama ?: "Latihan Rutin",
                                    date = kegiatan.tanggal ?: "-",
                                    presentCount = 0,
                                    totalCount = 48,
                                    latitude = kegiatan.latitude ?: -6.2000,
                                    longitude = kegiatan.longitude ?: 106.8166,
                                    radiusInMeters = kegiatan.radius ?: 100f,
                                    isPresent = false
                                )
                            }
                        sessions.clear()
                        sessions.addAll(mapped)
                        adapter.notifyDataSetChanged()
                    } else {
                        setupFallbackData()
                    }
                }
                is Resource.Error -> setupFallbackData()
            }
        }

        // Anggota list
        dashboardViewModel.anggotasState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                anggotasList = resource.data.data ?: emptyList()
            }
        }

        // Check-In result
        attendanceViewModel.checkInState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showSnackbar("Mengirim presensi...")
                is Resource.Success -> {
                    showSnackbar("✅ Presensi masuk berhasil!")
                    pendingSession = null
                    pendingLocation = null
                    selfiePhotoFile = null
                    selfieUri = null
                    attendanceViewModel.fetchTodayAttendance()
                    dashboardViewModel.fetchKegiatanData()
                }
                is Resource.Error -> showSnackbar("❌ Gagal presensi: ${resource.message}")
            }
        }

        // Check-Out result
        attendanceViewModel.checkOutState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showSnackbar("Memproses check-out...")
                is Resource.Success -> {
                    showSnackbar("✅ Check-out berhasil! Sampai jumpa.")
                    attendanceViewModel.fetchTodayAttendance()
                }
                is Resource.Error -> showSnackbar("❌ Gagal check-out: ${resource.message}")
            }
        }

        // Today status — drives check-in/check-out card visibility
        attendanceViewModel.todayState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    // If API returns data (user checked in today), show checkout card
                    val data = resource.data.data
                    if (data != null) {
                        binding.cardCheckOut.visibility = View.VISIBLE
                        // Try to extract kegiatanId from response for checkout
                        try {
                            @Suppress("UNCHECKED_CAST")
                            val map = data as? Map<String, Any>
                            val kId = (map?.get("kegiatanId") as? Number)?.toLong()
                                ?: (map?.get("kegiatan_id") as? Number)?.toLong()
                            checkedInSessionId = kId
                        } catch (e: Exception) {
                            // Keep last known session id
                        }
                    } else {
                        binding.cardCheckOut.visibility = View.GONE
                        checkedInSessionId = null
                    }
                }
                is Resource.Error -> {
                    // 404 = not checked in today
                    binding.cardCheckOut.visibility = View.GONE
                    checkedInSessionId = null
                }
                is Resource.Loading -> { /* no-op */ }
            }
        }

        // Permit result
        attendanceViewModel.permitState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> showSnackbar("Mengirim izin/sakit...")
                is Resource.Success -> showSnackbar("✅ Izin/Sakit berhasil dikirim!")
                is Resource.Error -> showSnackbar("❌ Gagal kirim izin: ${resource.message}")
            }
        }
    }

    // ─── Fallback Data ────────────────────────────────────────────────────────

    private fun setupFallbackData() {
        sessions.clear()
        val savedLat = sessionManager.getGeofenceLat()
        val savedLng = sessionManager.getGeofenceLng()
        val savedRadius = sessionManager.getGeofenceRadius()
        val deletedIds =
            sessionManager.prefs.getStringSet("deleted_kegiatan_ids", emptySet()) ?: emptySet()
        val raw = listOf(
            AttendanceSession(
                1, "Latihan Mingguan (Offline)", "2026-05-14",
                45, 48, savedLat, savedLng, savedRadius
            ),
            AttendanceSession(
                2, "Latihan Teknik Kepramukaan (Offline)", "2026-05-07",
                40, 48, savedLat, savedLng, savedRadius / 2f
            )
        )
        sessions.addAll(raw.filter { !deletedIds.contains(it.id.toString()) })
        adapter.notifyDataSetChanged()
    }

    // ─── Process Attendance (Check-In Dialog) ─────────────────────────────────

    private fun processAttendance(session: AttendanceSession) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermissions()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_attendance_slider, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvSessionName = dialogView.findViewById<TextView>(R.id.tvSessionDetailName)
        val tvSessionTarget = dialogView.findViewById<TextView>(R.id.tvSessionDetailTarget)
        val cardStatus =
            dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardLocationStatus)
        val imgStatus = dialogView.findViewById<ImageView>(R.id.imgLocationStatus)
        val tvDistance = dialogView.findViewById<TextView>(R.id.tvLocationDistance)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvLocationMessage)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBarCheckIn)
        val tvHint = dialogView.findViewById<TextView>(R.id.tvSliderHint)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancelSlider)

        tvSessionName.text = session.title
        tvSessionTarget.text =
            "Target GPS: %.4f, %.4f (Radius: %.0fm)".format(
                session.latitude, session.longitude, session.radiusInMeters
            )

        var userLocation: Location? = null
        var isWithinRadius = false
        var isWithinTimeWindow = false

        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
        val currentTimeStr = timeFormat.format(Calendar.getInstance().time)
        val startTimeStr = sessionManager.getAttendanceStartTime()
        val endTimeStr = sessionManager.getAttendanceEndTime()
        try {
            val cur = timeFormat.parse(currentTimeStr)
            val start = timeFormat.parse(startTimeStr)
            val end = timeFormat.parse(endTimeStr)
            if (cur != null && start != null && end != null) {
                isWithinTimeWindow = !cur.before(start) && !cur.after(end)
            }
        } catch (e: Exception) {
            isWithinTimeWindow = true
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                userLocation = location
                val results = FloatArray(1)
                Location.distanceBetween(
                    location.latitude, location.longitude,
                    session.latitude, session.longitude, results
                )
                val currentDistance = results[0]
                isWithinRadius = currentDistance <= session.radiusInMeters

                when {
                    !isWithinTimeWindow -> {
                        cardStatus.setCardBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaBg)
                        )
                        imgStatus.setImageResource(R.drawable.ic_cancel)
                        imgStatus.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaText)
                        )
                        tvDistance.text = "Jam: $currentTimeStr (Batas: $startTimeStr s/d $endTimeStr)"
                        tvDistance.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaText)
                        )
                        tvMessage.text = "Di Luar Waktu Presensi! Status dicatat Alpa."
                        tvMessage.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaText)
                        )
                    }
                    isWithinRadius -> {
                        cardStatus.setCardBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.statusHadirBg)
                        )
                        imgStatus.setImageResource(R.drawable.ic_check_circle)
                        imgStatus.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.statusHadirText)
                        )
                        tvDistance.text = "Jarak: %.1f meter (Dalam Radius ✓)".format(currentDistance)
                        tvDistance.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.statusHadirText)
                        )
                        tvMessage.text = "Lokasi Anda sesuai! Geser untuk absen."
                        tvMessage.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.statusHadirText)
                        )
                    }
                    else -> {
                        cardStatus.setCardBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaBg)
                        )
                        imgStatus.setImageResource(R.drawable.ic_cancel)
                        imgStatus.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaText)
                        )
                        tvDistance.text = "Jarak: %.1f meter (Di Luar Radius)".format(currentDistance)
                        tvDistance.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaText)
                        )
                        tvMessage.text = "Jarak Anda tidak sesuai. Sesuaikan jarak Anda!"
                        tvMessage.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.statusAlfaText)
                        )
                    }
                }
            } else {
                tvDistance.text = "Gagal mendeteksi lokasi GPS"
                tvMessage.text = "Harap pastikan GPS aktif dan miliki akurasi tinggi."
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvHint.alpha = (100 - progress) / 100f
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                if (seekBar.progress >= 90) {
                    val loc = userLocation
                    if (loc != null) {
                        if (!isWithinTimeWindow) {
                            dialog.dismiss()
                            // Submit as alpha (out of time) — open camera for selfie as proof
                            pendingSession = session
                            pendingLocation = loc
                            pendingIsLate = true
                            openCameraForSelfie()
                        } else if (isWithinRadius) {
                            dialog.dismiss()
                            pendingSession = session
                            pendingLocation = loc
                            pendingIsLate = false
                            openCameraForSelfie()
                        } else {
                            seekBar.progress = 0
                            tvHint.alpha = 1f
                            showSnackbar("Jarak Anda tidak sesuai, sesuaikan posisi Anda!")
                        }
                    } else {
                        seekBar.progress = 0
                        tvHint.alpha = 1f
                        showSnackbar("GPS belum siap. Tunggu sebentar lalu coba lagi.")
                    }
                } else {
                    seekBar.progress = 0
                    tvHint.alpha = 1f
                }
            }
        })

        dialog.show()
    }

    // ─── Camera for Selfie ────────────────────────────────────────────────────

    private fun openCameraForSelfie() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        launchCamera()
    }

    private fun launchCamera() {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageFile = File(
                requireContext().externalCacheDir,
                "selfie_checkin_$timestamp.jpg"
            )
            selfiePhotoFile = imageFile
            selfieUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, selfieUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                cameraLauncher.launch(intent)
            } else {
                showSnackbar("Tidak ada aplikasi kamera yang tersedia")
                pendingSession = null
                pendingLocation = null
            }
        } catch (e: Exception) {
            showSnackbar("Gagal membuka kamera: ${e.localizedMessage}")
            pendingSession = null
            pendingLocation = null
        }
    }

    // ─── Submit Check-In (Multipart with Selfie) ──────────────────────────────

    private fun submitCheckInWithSelfie(
        session: AttendanceSession,
        location: Location,
        isLate: Boolean
    ) {
        val mediaType = "text/plain".toMediaTypeOrNull()

        val latBody = location.latitude.toString().toRequestBody(mediaType)
        val lngBody = location.longitude.toString().toRequestBody(mediaType)
        val accBody = location.accuracy.toString().toRequestBody(mediaType)
        val kegiatanIdBody = session.id.toString().toRequestBody(mediaType)

        var selfiePart: MultipartBody.Part? = null
        val photoFile = selfiePhotoFile
        if (photoFile != null && photoFile.exists() && photoFile.length() > 0) {
            val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            selfiePart = MultipartBody.Part.createFormData("selfie", photoFile.name, requestFile)
        }

        attendanceViewModel.checkIn(latBody, lngBody, accBody, kegiatanIdBody, selfiePart)

        if (isLate) {
            showSnackbar("Status dicatat Alpa (di luar waktu). Tetap hadir dicatat.")
        }
    }

    // ─── Check-Out Flow ───────────────────────────────────────────────────────

    fun showCheckOutDialog(session: AttendanceSession) {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermissions()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Check-Out Presensi")
                    .setMessage(
                        "Konfirmasi keluar dari sesi '${session.title}'?\n\n" +
                                "Lokasi Anda: %.4f, %.4f".format(
                                    location.latitude, location.longitude
                                )
                    )
                    .setPositiveButton("Ya, Check-Out") { _, _ ->
                        val request = CheckOutRequest(
                            kegiatanId = session.id,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy.toDouble(),
                            isMockLocation = location.isFromMockProvider,
                            deviceId = sessionManager.getDeviceId() ?: "unknown"
                        )
                        attendanceViewModel.checkOut(request)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                showSnackbar("GPS tidak tersedia. Aktifkan GPS dan coba lagi.")
            }
        }
    }

    // ─── Permit / Izin / Sakit ────────────────────────────────────────────────

    fun showPermitDialog(session: AttendanceSession) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.dialog_manual_attendance, null)
        bottomSheet.setContentView(sheetView)

        val etNotes = sheetView.findViewById<android.widget.EditText>(R.id.etNotes)
        val spinnerStatus = sheetView.findViewById<android.widget.Spinner>(R.id.spinnerStatus)
        val btnSave = sheetView.findViewById<View>(R.id.btnSave)
        val btnCancel = sheetView.findViewById<View>(R.id.btnCancel)

        // Limit spinner to permit types only
        val permitTypes = listOf("Izin", "Sakit")
        val permitAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, permitTypes
        )
        permitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus?.adapter = permitAdapter

        btnCancel?.setOnClickListener { bottomSheet.dismiss() }
        btnSave?.setOnClickListener {
            val type = when (spinnerStatus?.selectedItem?.toString()?.lowercase()) {
                "sakit" -> "sakit"
                else -> "izin"
            }
            val reason = etNotes?.text?.toString()?.trim() ?: ""
            if (reason.isEmpty()) {
                showSnackbar("Harap isi keterangan izin/sakit")
                return@setOnClickListener
            }
            val mediaType = "text/plain".toMediaTypeOrNull()
            val kegiatanIdBody = session.id.toString().toRequestBody(mediaType)
            val reasonBody = reason.toRequestBody(mediaType)
            val typeBody = type.toRequestBody(mediaType)
            attendanceViewModel.submitPermit(kegiatanIdBody, reasonBody, typeBody, null)
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }

    // ─── Delete Session Dialog ────────────────────────────────────────────────

    private fun showDeleteSessionDialog(session: AttendanceSession) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Sesi Presensi")
            .setMessage("Hapus sesi '${session.title}' secara permanen?")
            .setPositiveButton("Hapus") { dialog, _ ->
                val deletedIds =
                    sessionManager.prefs.getStringSet("deleted_kegiatan_ids", emptySet())
                        ?.toMutableSet() ?: mutableSetOf()
                deletedIds.add(session.id.toString())
                sessionManager.prefs.edit()
                    .putStringSet("deleted_kegiatan_ids", deletedIds).apply()
                showSnackbar("Sesi '${session.title}' dihapus")
                dashboardViewModel.fetchKegiatanData()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // ─── Manual Attendance Dialog (Pembina/Admin) ─────────────────────────────

    private fun showManualAttendanceDialog() {
        if (anggotasList.isEmpty() || sessions.isEmpty()) {
            showSnackbar("Memuat data anggota/kegiatan. Harap tunggu...")
            return
        }

        val dialogBinding = DialogManualAttendanceBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val memberNames = anggotasList.map { "${it.nama ?: "Anggota"} (${it.kelas ?: "-"})" }
        val memberAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, memberNames)
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerMember.adapter = memberAdapter

        val activityTitles = sessions.map { it.title }
        val activityAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityTitles)
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerActivity.adapter = activityAdapter

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val memberIdx = dialogBinding.spinnerMember.selectedItemPosition
            val activityIdx = dialogBinding.spinnerActivity.selectedItemPosition
            val statusStr = dialogBinding.spinnerStatus.selectedItem.toString()
            val notes = dialogBinding.etNotes.text.toString().trim()

            if (memberIdx < 0 || activityIdx < 0) return@setOnClickListener

            val selectedMember = anggotasList[memberIdx]
            val selectedSession = sessions[activityIdx]

            val calendar = Calendar.getInstance()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            val currentTimeString = timeFormat.format(calendar.time)

            val mediaType = "text/plain".toMediaTypeOrNull()
            val latBody = selectedSession.latitude.toString().toRequestBody(mediaType)
            val lngBody = selectedSession.longitude.toString().toRequestBody(mediaType)
            val accBody = "0.0".toRequestBody(mediaType)
            val kegiatanIdBody = selectedSession.id.toString().toRequestBody(mediaType)

            attendanceViewModel.checkIn(latBody, lngBody, accBody, kegiatanIdBody, null)
            showSnackbar("Presensi manual anggota ${selectedMember.nama} dikirim...")
            dialog.dismiss()
        }
        dialog.show()
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun showSnackbar(message: String) {
        if (_binding != null) {
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
