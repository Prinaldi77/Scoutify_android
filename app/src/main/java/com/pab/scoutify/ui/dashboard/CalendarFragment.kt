package com.pab.scoutify.ui.dashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pab.scoutify.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var tvEventCount: TextView
    private lateinit var layoutEventList: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        tvEventCount = view.findViewById(R.id.tvEventCount)
        layoutEventList = view.findViewById(R.id.layoutEventList)

        // Set initial selected date to today
        val today = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        tvSelectedDate.text = dateFormat.format(today.time)

        // Refresh click listener
        view.findViewById<View>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(context, "Sinkronisasi kalender selesai... 📅✨", Toast.LENGTH_SHORT).show()
        }

        // Handle CalendarView date selections
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance()
            selected.set(year, month, dayOfMonth)
            tvSelectedDate.text = dateFormat.format(selected.time)

            val dayOfWeek = selected.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                // Weekend - No shifts
                tvEventCount.text = "0"
                tvEventCount.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                tvEventCount.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                layoutEventList.removeAllViews()

                // Add empty state text
                val emptyView = TextView(context).apply {
                    text = "Tidak ada jadwal piket atau kegiatan Pramuka di hari libur. 🏖️"
                    textSize = 13f
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_text))
                    gravity = Gravity.CENTER
                    setPadding(0, 48, 0, 0)
                }
                layoutEventList.addView(emptyView)
            } else {
                // Weekday - show shift
                tvEventCount.text = "1"
                tvEventCount.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_sand))
                tvEventCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryBrown))
                layoutEventList.removeAllViews()

                createAndAddEventCard(dayOfWeek)
            }
        }
    }

    private fun createAndAddEventCard(dayOfWeek: Int) {
        val divisionName = when (dayOfWeek) {
            Calendar.MONDAY -> "Piket Divisi Hubungan Masyarakat"
            Calendar.TUESDAY -> "Piket Divisi Perlengkapan"
            Calendar.WEDNESDAY -> "Piket Divisi Logistik & Konsumsi"
            Calendar.THURSDAY -> "Piket Divisi Keamanan & Disiplin"
            else -> "Piket Divisi Olahraga & Kesenian"
        }

        val context = requireContext()

        val cardView = com.google.android.material.card.MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            radius = 32f
            cardElevation = 0f
            strokeWidth = 2
            setStrokeColor(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, R.color.divider)))
        }

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xFFFFFFFF.toInt())
        }

        val iconCard = com.google.android.material.card.MaterialCardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(88, 88)
            radius = 20f
            cardElevation = 0f
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.accent_sand))
        }

        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_calendar)
            layoutParams = FrameLayout.LayoutParams(40, 40, Gravity.CENTER)
            setColorFilter(ContextCompat.getColor(context, R.color.primaryBrown))
        }
        iconCard.addView(icon)

        val textGroup = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(24, 0, 0, 0)
            }
        }

        val title = TextView(context).apply {
            text = divisionName
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF212121.toInt())
        }

        val timeLoc = TextView(context).apply {
            text = "07:30 WIB • Sekretariat Pramuka"
            textSize = 11f
            setTextColor(ContextCompat.getColor(context, R.color.grey_text))
            setPadding(0, 8, 0, 0)
        }

        textGroup.addView(title)
        textGroup.addView(timeLoc)

        val tag = TextView(context).apply {
            text = "Piket"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.primaryBrown))
            setBackgroundResource(R.drawable.bg_badge_trend)
            backgroundTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(context, R.color.accent_sand))
            setPadding(20, 8, 20, 8)
        }

        content.addView(iconCard)
        content.addView(textGroup)
        content.addView(tag)
        cardView.addView(content)

        layoutEventList.addView(cardView)
    }
}
