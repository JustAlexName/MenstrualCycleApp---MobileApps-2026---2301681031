package com.example.menstrualcycleapp.ui.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menstrualcycleapp.databinding.FragmentHomeBinding
import com.example.menstrualcycleapp.model.CycleEntry
import com.example.menstrualcycleapp.model.SymptomTypes
import com.example.menstrualcycleapp.ui.CycleViewModel
import com.example.menstrualcycleapp.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.*

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: CycleViewModel by activityViewModels()
    private lateinit var adapter: CycleAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, saved: Bundle?) {
        super.onViewCreated(view, saved)

        adapter = CycleAdapter(
            onEdit = { showEditDialog(it) },
            onDelete = { confirmDelete(it) }
        )
        b.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        b.rvHistory.adapter = adapter

        vm.toast.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }

        vm.nextPeriod.observe(viewLifecycleOwner) { date ->
            if (date == null) {
                b.tvNextPeriod.text = "Добавете цикъл за прогноза"
                b.tvNextPeriodDate.text = ""
            } else {
                val d = DateUtils.daysUntil(date)
                b.tvNextPeriod.text = when {
                    d < 0  -> "Може да е закъсняла"
                    d == 0 -> "Очаква се днес"
                    d == 1 -> "Очаква се утре"
                    else   -> "След $d дни"
                }
                b.tvNextPeriodDate.text = DateUtils.format(date)
            }
        }

        vm.ovulation.observe(viewLifecycleOwner) { date ->
            if (date != null) {
                val d = DateUtils.daysUntil(date)
                b.tvOvulation.text = when {
                    d < 0  -> "Вече е минала"
                    d == 0 -> "Днес"
                    else   -> "След $d дни"
                }
                b.tvOvulationDate.text = DateUtils.format(date)
            }
        }

        vm.avgCycle.observe(viewLifecycleOwner)  { b.tvAvgCycle.text  = "%.0f дни".format(it) }
        vm.avgPeriod.observe(viewLifecycleOwner) { b.tvAvgPeriod.text = "%.0f дни".format(it) }

        vm.allCycles.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val active = list.firstOrNull { it.endDate == null }
            if (active != null) {
                val day = DateUtils.daysBetween(active.startDate, System.currentTimeMillis()) + 1
                b.tvPhase.text = "🩸 Ден $day от менструацията"
                b.btnPeriod.text = "Запиши края на менструацията"
                b.btnPeriod.setOnClickListener { endPeriod() }
            } else {
                b.tvPhase.text = phaseText()
                b.btnPeriod.text = "Запиши начало на менструацията"
                b.btnPeriod.setOnClickListener { startPeriod() }
            }
        }

        b.btnSymptoms.setOnClickListener {
            showSymptomsDialog()
        }
    }

    private fun showSymptomsDialog() {
        val today = DateUtils.todayMidnight()
        viewLifecycleOwner.lifecycleScope.launch {
            val current = vm.getSymptomsForDateSync(today).map { it.symptomType }.toSet()
            val types = SymptomTypes.ALL
            val labels = types.map { SymptomTypes.label(it) }.toTypedArray()
            val checked = types.map { current.contains(it) }.toBooleanArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Запиши симптоми за днес")
                .setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                    checked[which] = isChecked
                }
                .setPositiveButton("Запази") { _, _ ->
                    val selected = types.filterIndexed { i, _ -> checked[i] }.toSet()
                    vm.saveSymptoms(today, selected)
                }
                .setNegativeButton("Откажи", null)
                .show()
        }
    }

    private fun phaseText(): String {
        val next = vm.nextPeriod.value ?: return "📊 Добавете данни"
        val ov   = vm.ovulation.value  ?: return "🌱 Фоликуларна фаза"
        val now  = System.currentTimeMillis()
        return when {
            now < ov   - 2 * 86_400_000L -> "🌱 Фоликуларна фаза"
            now < ov   + 2 * 86_400_000L -> "🥚 Овулационен период"
            now < next - 3 * 86_400_000L -> "🌙 Лутеална фаза"
            else                          -> "⚠️ Предменструален период"
        }
    }

    private fun startPeriod() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val c = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
            vm.addCycle(CycleEntry(startDate = c.timeInMillis))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun endPeriod() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val c = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
            vm.endActiveCycle(c.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showEditDialog(entry: CycleEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Редактирай цикъл")
            .setItems(arrayOf("Промени начална дата", "Промени крайна дата")) { _, w ->
                pickDate(if (w == 0) "Начална дата" else "Крайна дата") { ts ->
                    if (w == 0) vm.updateCycle(entry.copy(startDate = ts))
                    else {
                        val days = DateUtils.daysBetween(entry.startDate, ts) + 1
                        vm.updateCycle(entry.copy(endDate = ts, cycleLengthDays = days))
                    }
                }
            }
            .setNegativeButton("Откажи", null).show()
    }

    private fun pickDate(title: String, cb: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val c = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
            cb(c.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            .also { it.setTitle(title) }.show()
    }

    private fun confirmDelete(entry: CycleEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Изтриване")
            .setMessage("Изтрий цикъла от ${DateUtils.format(entry.startDate)}?")
            .setPositiveButton("Изтрий") { _, _ -> vm.deleteCycle(entry) }
            .setNegativeButton("Откажи", null).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
