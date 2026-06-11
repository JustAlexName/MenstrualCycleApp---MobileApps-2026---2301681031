package com.example.menstrualcycleapp.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.menstrualcycleapp.databinding.FragmentSettingsBinding
import com.example.menstrualcycleapp.notifications.NotificationScheduler
import com.example.menstrualcycleapp.ui.CycleViewModel

class SettingsFragment : Fragment() {

    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!
    private val vm: CycleViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSettingsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, saved: Bundle?) {
        super.onViewCreated(view, saved)
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        b.etCycle.setText(prefs.getInt("cycle_length", 28).toString())
        b.etPeriod.setText(prefs.getInt("period_length", 5).toString())
        b.switchNotif.isChecked = prefs.getBoolean("notifications_enabled", true)
        b.switchDark.isChecked  = prefs.getBoolean("dark_mode", false)

        b.btnSave.setOnClickListener {
            val cycle  = b.etCycle.text.toString().toIntOrNull() ?: 28
            val period = b.etPeriod.text.toString().toIntOrNull() ?: 5
            if (cycle !in 15..60) {
                Toast.makeText(context, "Цикълът трябва да е 15–60 дни", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit()
                .putInt("cycle_length", cycle)
                .putInt("period_length", period)
                .putBoolean("notifications_enabled", b.switchNotif.isChecked)
                .putBoolean("dark_mode", b.switchDark.isChecked)
                .apply()

            if (b.switchNotif.isChecked) NotificationScheduler.schedule(requireContext())
            else NotificationScheduler.cancelAll(requireContext())

            AppCompatDelegate.setDefaultNightMode(
                if (b.switchDark.isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            vm.refreshStats()
            Toast.makeText(context, "Настройките са запазени ✓", Toast.LENGTH_SHORT).show()
        }

        b.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Изтрий всички данни")
                .setMessage("Това не може да бъде отменено. Продължи?")
                .setPositiveButton("Изтрий") { _, _ ->
                    vm.allCycles.value?.forEach { vm.deleteCycle(it) }
                }
                .setNegativeButton("Откажи", null).show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
