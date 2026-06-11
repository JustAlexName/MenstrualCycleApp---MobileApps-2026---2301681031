package com.example.menstrualcycleapp.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.menstrualcycleapp.databinding.FragmentCalendarBinding
import com.example.menstrualcycleapp.model.SymptomTypes
import com.example.menstrualcycleapp.ui.CycleViewModel
import com.example.menstrualcycleapp.utils.DateUtils
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.*

class CalendarFragment : Fragment() {

    private var _b: FragmentCalendarBinding? = null
    private val b get() = _b!!
    private val vm: CycleViewModel by activityViewModels()
    private var selectedDate = DateUtils.todayMidnight()
    private val checkboxes = mutableMapOf<String, CheckBox>()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentCalendarBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, saved: Bundle?) {
        super.onViewCreated(view, saved)

        b.tvDate.text = DateUtils.format(selectedDate)

        b.calendarView.setOnDateChangedListener { _, date, _ ->
            selectedDate = date.calendar.timeInMillis
            b.tvDate.text = DateUtils.format(selectedDate)
            loadSymptoms()
        }

        vm.allCycles.observe(viewLifecycleOwner) { cycles ->
            val highlightDates = mutableSetOf<CalendarDay>()
            val avgPeriod = vm.avgPeriod.value?.toInt() ?: 5
            
            cycles.forEach { cycle ->
                val startCal = Calendar.getInstance().apply { 
                    timeInMillis = cycle.startDate 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val endCal = if (cycle.endDate != null) {
                    Calendar.getInstance().apply { 
                        timeInMillis = cycle.endDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                } else {
                    // Ако менструацията е активна, автоматично маркираме средната продължителност
                    (startCal.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, avgPeriod - 1)
                    }
                }
                
                val current = startCal.clone() as Calendar
                while (current.timeInMillis <= endCal.timeInMillis) {
                    highlightDates.add(CalendarDay.from(current))
                    current.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            b.calendarView.removeDecorators()
            val color = android.graphics.Color.parseColor("#B0004E") // pink_primary
            b.calendarView.addDecorator(HighlightDecorator(color, highlightDates))
        }

        // Build checkboxes dynamically
        SymptomTypes.ALL.forEach { type ->
            val cb = CheckBox(requireContext()).apply {
                text = SymptomTypes.label(type)
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 6, 0, 6) }
            }
            checkboxes[type] = cb
            b.llSymptoms.addView(cb)
        }

        b.btnSave.setOnClickListener {
            val checked = checkboxes.filter { it.value.isChecked }.keys.toSet()
            vm.saveSymptoms(selectedDate, checked)
        }

        loadSymptoms()
    }

    private fun loadSymptoms() {
        vm.symptomsForDate(selectedDate).observe(viewLifecycleOwner) { logs ->
            checkboxes.values.forEach { it.isChecked = false }
            logs.forEach { log -> checkboxes[log.symptomType]?.isChecked = true }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
