package com.example.menstrualcycleapp.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.menstrualcycleapp.databinding.FragmentStatsBinding
import com.example.menstrualcycleapp.model.SymptomTypes
import com.example.menstrualcycleapp.ui.CycleViewModel
import com.example.menstrualcycleapp.utils.DateUtils
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _b: FragmentStatsBinding? = null
    private val b get() = _b!!
    private val vm: CycleViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentStatsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, saved: Bundle?) {
        super.onViewCreated(view, saved)

        vm.avgCycle.observe(viewLifecycleOwner)  { b.tvAvgCycle.text  = "%.0f дни".format(it) }
        vm.avgPeriod.observe(viewLifecycleOwner) { b.tvAvgPeriod.text = "%.0f дни".format(it) }
        vm.nextPeriod.observe(viewLifecycleOwner) {
            b.tvNext.text = if (it != null) DateUtils.format(it) else "—"
        }
        vm.ovulation.observe(viewLifecycleOwner) {
            b.tvOvulation.text = if (it != null) DateUtils.format(it) else "—"
        }

        lifecycleScope.launch {
            buildCycleChart()
            buildSymptomChart()
        }
    }

    private suspend fun buildCycleChart() {
        val entries = vm.getAllCyclesSync()
        if (entries.size < 2) {
            b.chartCycles.visibility = View.GONE
            b.tvNoCycle.visibility = View.VISIBLE
            return
        }
        b.chartCycles.visibility = View.VISIBLE
        b.tvNoCycle.visibility = View.GONE

        val lengths = entries.zipWithNext { a, n ->
            ((a.startDate - n.startDate) / 86_400_000f)
        }.filter { it in 15f..60f }

        val labels = entries.dropLast(1).map { DateUtils.formatShort(it.startDate) }
        val bars = lengths.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val ds = BarDataSet(bars, "Дни на цикъл").apply {
            color = requireContext().getColor(com.google.android.material.R.color.design_default_color_primary)
            valueTextSize = 10f
        }
        b.chartCycles.apply {
            data = BarData(ds)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                labelRotationAngle = -30f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            animateY(500)
            invalidate()
        }
    }

    private suspend fun buildSymptomChart() {
        val logs = vm.getAllSymptomsSync()
        if (logs.isEmpty()) {
            b.chartSymptoms.visibility = View.GONE
            b.tvNoSymptom.visibility = View.VISIBLE
            return
        }
        b.chartSymptoms.visibility = View.VISIBLE
        b.tvNoSymptom.visibility = View.GONE

        val counts = SymptomTypes.ALL
            .map { t -> t to logs.count { it.symptomType == t } }
            .filter { it.second > 0 }

        val slices = counts.map { (t, c) -> PieEntry(c.toFloat(), SymptomTypes.label(t)) }
        val ds = PieDataSet(slices, "").apply {
            colors = listOf(
                0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF3F51B5.toInt(),
                0xFF2196F3.toInt(), 0xFF4CAF50.toInt(), 0xFFFF9800.toInt(),
                0xFFFF5722.toInt(), 0xFF795548.toInt(), 0xFF607D8B.toInt(), 0xFF009688.toInt()
            )
            sliceSpace = 3f
            valueTextSize = 11f
        }
        b.chartSymptoms.apply {
            data = PieData(ds)
            isDrawHoleEnabled = true
            holeRadius = 38f
            description.isEnabled = false
            legend.isEnabled = true
            animateY(700)
            invalidate()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
