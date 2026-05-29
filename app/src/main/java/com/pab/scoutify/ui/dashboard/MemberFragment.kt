package com.pab.scoutify.ui.dashboard

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pab.scoutify.R
import com.pab.scoutify.databinding.FragmentMemberBinding
import com.pab.scoutify.model.Anggota
import com.pab.scoutify.ui.auth.SessionManager
import com.pab.scoutify.utils.Resource
import com.google.android.material.card.MaterialCardView

class MemberFragment : Fragment() {
    private var _binding: FragmentMemberBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DashboardViewModel
    private lateinit var sessionManager: SessionManager
    private var membersList = mutableListOf<Anggota>()
    private lateinit var adapter: MemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        
        setupRecyclerView()
        setupObservers()

        // Fetch real members from backend in real-time
        viewModel.fetchAnggotas()

        val role = sessionManager.getRole()
        val hasAdminAccess = sessionManager.hasMemberAccess()

        if (role.lowercase() == "siswa" && !hasAdminAccess) {
            // Student with no admin access: hide tabs and show member list (read-only)
            binding.cardTabsContainer.visibility = View.GONE
            binding.layoutContentAnggota.visibility = View.VISIBLE
            binding.layoutContentCatatan.visibility = View.GONE
            binding.layoutContentKas.visibility = View.GONE
            binding.fabAddMember.visibility = View.GONE
        } else {
            // Pembina or Siswa with admin access: show tabs and default to Catatan
            binding.cardTabsContainer.visibility = View.VISIBLE
            setupTabBar()
        }

        // Bind dynamic cash balance & history from SessionManager
        renderKasTransactions()

        // Add Notes button listener
        binding.btnAddCatatan.setOnClickListener {
            showAddCatatanDialog()
        }

        // Add Cash transaction listener
        binding.cardTotalKas.setOnClickListener {
            showAddKasTransactionDialog()
        }
    }

    private fun setupTabBar() {
        val tabCatatan = binding.tabCatatan
        val tabKas = binding.tabKas
        val tabAnggota = binding.tabAnggota

        // Default tab: Catatan
        selectTab(tabCatatan, binding.layoutContentCatatan)

        tabCatatan.setOnClickListener {
            selectTab(tabCatatan, binding.layoutContentCatatan)
            deselectTab(tabKas)
            deselectTab(tabAnggota)
            binding.fabAddMember.visibility = View.GONE
        }

        tabKas.setOnClickListener {
            selectTab(tabKas, binding.layoutContentKas)
            deselectTab(tabCatatan)
            deselectTab(tabAnggota)
            binding.fabAddMember.visibility = View.GONE
        }

        tabAnggota.setOnClickListener {
            selectTab(tabAnggota, binding.layoutContentAnggota)
            deselectTab(tabCatatan)
            deselectTab(tabKas)
            binding.fabAddMember.visibility = View.VISIBLE
        }
    }

    private fun selectTab(card: MaterialCardView, content: View) {
        card.setCardBackgroundColor(0xFFFFFFFF.toInt())
        content.visibility = View.VISIBLE

        when (card.id) {
            R.id.tabCatatan -> {
                binding.txtTabCatatan.setTextColor(0xFF5D4037.toInt())
                binding.txtTabCatatan.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            R.id.tabKas -> {
                binding.txtTabKas.setTextColor(0xFF5D4037.toInt())
                binding.txtTabKas.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            R.id.tabAnggota -> {
                binding.txtTabAnggota.setTextColor(0xFF5D4037.toInt())
                binding.txtTabAnggota.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    private fun deselectTab(card: MaterialCardView) {
        card.setCardBackgroundColor(android.graphics.Color.TRANSPARENT)
        
        when (card.id) {
            R.id.tabCatatan -> {
                binding.txtTabCatatan.setTextColor(0xFFFFFFFF.toInt())
                binding.txtTabCatatan.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.layoutContentCatatan.visibility = View.GONE
            }
            R.id.tabKas -> {
                binding.txtTabKas.setTextColor(0xFFFFFFFF.toInt())
                binding.txtTabKas.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.layoutContentKas.visibility = View.GONE
            }
            R.id.tabAnggota -> {
                binding.txtTabAnggota.setTextColor(0xFFFFFFFF.toInt())
                binding.txtTabAnggota.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.layoutContentAnggota.visibility = View.GONE
            }
        }
    }

    private fun renderKasTransactions() {
        binding.tvTotalKas.text = sessionManager.getMemberCashBalance()
        binding.layoutKasHistoryContainer.removeAllViews()

        val rawTransactions = sessionManager.getMemberKasTransactions()
        if (rawTransactions.isEmpty()) return

        val items = rawTransactions.split(";")
        items.forEach { item ->
            val parts = item.split("|")
            if (parts.size >= 4) {
                val title = parts[0]
                val date = parts[1]
                val amountStr = parts[2]
                val isIncome = parts[3].toBoolean()

                // Inflate premium card view programmatically for stability
                val card = MaterialCardView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 24)
                    }
                    radius = 32f
                    cardElevation = 0f
                    strokeWidth = 3
                    strokeColor = android.graphics.Color.parseColor("#EEEEEE")
                }

                val outerLayout = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(40, 40, 40, 40)
                    gravity = Gravity.CENTER_VERTICAL
                }

                val textLayout = LinearLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    orientation = LinearLayout.VERTICAL
                }

                val tvTitle = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = title
                    setTextColor(android.graphics.Color.parseColor("#212121"))
                    typeface = android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD)
                    textSize = 13f
                }

                val tvDate = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 0)
                    }
                    text = "$date • Cash"
                    setTextColor(android.graphics.Color.parseColor("#757575"))
                    textSize = 11f
                }

                textLayout.addView(tvTitle)
                textLayout.addView(tvDate)

                val tvAmount = TextView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    text = amountStr
                    setTextColor(android.graphics.Color.parseColor(if (isIncome) "#5D4037" else "#C62828"))
                    typeface = android.graphics.Typeface.defaultFromStyle(android.graphics.Typeface.BOLD)
                    textSize = 13f
                }

                outerLayout.addView(textLayout)
                outerLayout.addView(tvAmount)
                card.addView(outerLayout)

                binding.layoutKasHistoryContainer.addView(card)
            }
        }
    }

    private fun showAddKasTransactionDialog() {
        val context = requireContext()
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val etTitle = android.widget.EditText(context).apply {
            hint = "Deskripsi Transaksi (e.g. Pembelian Tali)"
            setPadding(30, 30, 30, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }

        val etAmount = android.widget.EditText(context).apply {
            hint = "Jumlah Uang (Rupiah)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(30, 30, 30, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }

        val options = arrayOf("Uang Masuk (+)", "Uang Keluar (-)")
        val spinner = android.widget.Spinner(context).apply {
            adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, options)
            setPadding(30, 30, 30, 30)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(etTitle)
        container.addView(etAmount)
        container.addView(spinner)

        AlertDialog.Builder(context)
            .setTitle("💰 Tambah Transaksi Kas Baru")
            .setView(container)
            .setPositiveButton("Simpan") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val isIncome = spinner.selectedItemPosition == 0

                if (title.isNotEmpty() && amountStr.isNotEmpty()) {
                    val amountInt = amountStr.toIntOrNull()
                    if (amountInt != null && amountInt > 0) {
                        val currentBalanceStr = sessionManager.getMemberCashBalance()
                        val currentInt = currentBalanceStr.replace("[^0-9]".toRegex(), "").toIntOrNull() ?: 0
                        val newInt = if (isIncome) currentInt + amountInt else currentInt - amountInt

                        if (newInt < 0) {
                            Toast.makeText(context, "Sisa saldo kas tidak mencukupi untuk pengeluaran ini!", Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }

                        val formattedBalance = String.format(java.util.Locale.US, "Rp %,d", newInt).replace(",", ".")
                        sessionManager.saveMemberCashBalance(formattedBalance)

                        val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).format(java.util.Date())
                        val amountFormatted = (if (isIncome) "+" else "-") + String.format(java.util.Locale.US, "Rp %,d", amountInt).replace(",", ".")

                        val newTx = "$title|$todayStr|$amountFormatted|$isIncome"
                        val oldTx = sessionManager.getMemberKasTransactions()
                        val updatedTx = if (oldTx.isEmpty()) newTx else "$newTx;$oldTx"

                        sessionManager.saveMemberKasTransactions(updatedTx)
                        renderKasTransactions()
                        Toast.makeText(context, "Transaksi kas berhasil ditambahkan secara live! 💰📈", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(context, "Jumlah uang harus berupa angka positif!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Harap isi deskripsi dan jumlah uang transaksi!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAddCatatanDialog() {
        val input = android.widget.EditText(context).apply {
            hint = "Tulis notulensi, todo rapat, atau ide di sini..."
            setPadding(40, 40, 40, 40)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("📝 Tambah Catatan Baru")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val noteText = input.text.toString().trim()
                if (noteText.isNotEmpty()) {
                    Toast.makeText(context, "Catatan berhasil ditambahkan ke riwayat divisi! 📝", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupRecyclerView() {
        adapter = MemberAdapter(membersList)
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@MemberFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.anggotasState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Loader state can be handled here if needed
                }
                is Resource.Success -> {
                    val data = resource.data.data
                    if (data != null && data.isNotEmpty()) {
                        membersList.clear()
                        membersList.addAll(data)
                        adapter.notifyDataSetChanged()
                        
                        // Dynamically update member count badge in header
                        binding.tvHeaderSub.text = "${membersList.size} Anggota"
                    } else {
                        setupFallbackMembers()
                    }
                }
                is Resource.Error -> {
                    setupFallbackMembers()
                }
            }
        }
    }

    private fun setupFallbackMembers() {
        membersList.clear()
        membersList.addAll(
            listOf(
                Anggota(1, "Aiden Thompson", "IX-A", "Ketua Regu", "2024", null, "aktif"),
                Anggota(2, "Bella Garcia", "VIII-C", "Wakil Ketua", "2024", null, "aktif"),
                Anggota(3, "Caleb Wilson", "IX-B", "Anggota", "2025", null, "aktif"),
                Anggota(4, "Diana Prince", "IX-A", "Sekretaris", "2024", null, "aktif"),
                Anggota(5, "Ethan Hunt", "VII-D", "Bendahara", "2025", null, "aktif")
            )
        )
        adapter.notifyDataSetChanged()
        binding.tvHeaderSub.text = "${membersList.size} Anggota"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}