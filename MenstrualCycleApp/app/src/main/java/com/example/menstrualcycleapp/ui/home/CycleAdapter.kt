package com.example.menstrualcycleapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.menstrualcycleapp.databinding.ItemCycleBinding
import com.example.menstrualcycleapp.model.CycleEntry
import com.example.menstrualcycleapp.utils.DateUtils

class CycleAdapter(
    private val onEdit: (CycleEntry) -> Unit,
    private val onDelete: (CycleEntry) -> Unit
) : ListAdapter<CycleEntry, CycleAdapter.VH>(Diff()) {

    inner class VH(private val b: ItemCycleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: CycleEntry) {
            b.tvStart.text = "Начало: ${DateUtils.format(e.startDate)}"
            b.tvEnd.text = if (e.endDate != null) {
                val days = DateUtils.daysBetween(e.startDate, e.endDate) + 1
                "Край: ${DateUtils.format(e.endDate)} · $days дни"
            } else "В момента тече 🩸"
            b.tvFlow.text = when (e.flowIntensity) { 1 -> "Лека" 3 -> "Силна" else -> "Средна" }
            b.btnEdit.setOnClickListener { onEdit(e) }
            b.btnDelete.setOnClickListener { onDelete(e) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCycleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    class Diff : DiffUtil.ItemCallback<CycleEntry>() {
        override fun areItemsTheSame(o: CycleEntry, n: CycleEntry) = o.id == n.id
        override fun areContentsTheSame(o: CycleEntry, n: CycleEntry) = o == n
    }
}
