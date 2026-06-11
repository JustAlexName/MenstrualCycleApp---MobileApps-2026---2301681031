package com.example.menstrualcycleapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.menstrualcycleapp.databinding.ActivityOnboardingBinding
import java.util.*

class OnboardingActivity : AppCompatActivity() {
    private lateinit var b: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(b.root)

        val cal = Calendar.getInstance()
        b.datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH), null)

        b.btnStart.setOnClickListener {
            val cycle  = b.etCycleLength.text.toString().toIntOrNull() ?: 28
            val period = b.etPeriodLength.text.toString().toIntOrNull() ?: 5
            if (cycle !in 15..60) {
                Toast.makeText(this, "Цикълът трябва да е 15–60 дни", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val c = Calendar.getInstance().apply {
                set(b.datePicker.year, b.datePicker.month, b.datePicker.dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit()
                .putBoolean("first_launch", false)
                .putInt("cycle_length", cycle)
                .putInt("period_length", period)
                .putLong("last_period_date", c.timeInMillis)
                .apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
