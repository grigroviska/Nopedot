package com.grigroviska.nopedot.fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.databinding.FragmentSettingsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var contentBinding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentSettingsBinding.bind(view)

        contentBinding.firstDayOfWeekLayout.setOnClickListener {
            showFirstDayDialog()
        }
        contentBinding.selectDateLayout.setOnClickListener {
            showDateFormatDialog()
        }
        contentBinding.selectTimeLayout.setOnClickListener {
            showTimeFormatDialog()
        }
        contentBinding.feedbackLayout.setOnClickListener {
            openFeedbackEmail()
        }
    }

    private fun showFirstDayDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.first_day_of_week)

        val radioSunday = dialog.findViewById<RadioButton>(R.id.sunday)
        val radioMonday = dialog.findViewById<RadioButton>(R.id.monday)
        val radioSaturday = dialog.findViewById<RadioButton>(R.id.saturday)
        val buttonSave = dialog.findViewById<View>(R.id.buttonSave)
        val buttonCancel = dialog.findViewById<View>(R.id.buttonCancel)

        val sharedPreferences = requireActivity().getSharedPreferences("NopeDotSettings", Context.MODE_PRIVATE)
        val savedFirstDay = sharedPreferences.getString("firstDayOfWeek", "Sunday")

        when (savedFirstDay) {
            "Sunday" -> radioSunday.isChecked = true
            "Monday" -> radioMonday.isChecked = true
            "Saturday" -> radioSaturday.isChecked = true
            else -> radioSunday.isChecked = true
        }

        buttonSave.setOnClickListener {
            val selectedId = dialog.findViewById<RadioGroup>(R.id.radioGroupDateFormats).checkedRadioButtonId
            val selectedRadioButton = dialog.findViewById<RadioButton>(selectedId)
            val firstDay = selectedRadioButton.text.toString()

            saveToPreferences("firstDayOfWeek", firstDay)
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDateFormatDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.select_date_format)

        val radioDateFormat1 = dialog.findViewById<RadioButton>(R.id.radioDateFormat1)
        val radioDateFormat2 = dialog.findViewById<RadioButton>(R.id.radioDateFormat2)
        val radioDateFormat3 = dialog.findViewById<RadioButton>(R.id.radioDateFormat3)
        val buttonSave = dialog.findViewById<View>(R.id.buttonSave)
        val buttonCancel = dialog.findViewById<View>(R.id.buttonCancel)

        val currentDate = Date()

        val dateFormat1 = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateFormat2 = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val dateFormat3 = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

        val dateStr1 = dateFormat1.format(currentDate) + getString(R.string.day_month_year)
        val dateStr2 = dateFormat2.format(currentDate) + getString(R.string.month_day_year)
        val dateStr3 = dateFormat3.format(currentDate) + getString(R.string.year_month_day)

        radioDateFormat1.text = dateStr1
        radioDateFormat2.text = dateStr2
        radioDateFormat3.text = dateStr3

        val sharedPreferences = requireActivity().getSharedPreferences("NopeDotSettings", Context.MODE_PRIVATE)
        val savedDateFormat = sharedPreferences.getString("dateFormat", dateStr1)

        when (savedDateFormat) {
            dateStr1 -> radioDateFormat1.isChecked = true
            dateStr2 -> radioDateFormat2.isChecked = true
            dateStr3 -> radioDateFormat3.isChecked = true
            else -> radioDateFormat1.isChecked = true
        }

        buttonSave.setOnClickListener {
            val selectedId = dialog.findViewById<RadioGroup>(R.id.radioGroupDateFormats).checkedRadioButtonId
            val selectedRadioButton = dialog.findViewById<RadioButton>(selectedId)
            val dateFormat = selectedRadioButton.text.toString()

            saveToPreferences("dateFormat", dateFormat)
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showTimeFormatDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.select_time_format)

        val radioTimeFormatDefault = dialog.findViewById<RadioButton>(R.id.systemTimeDefault)
        val radioTimeFormat24 = dialog.findViewById<RadioButton>(R.id.hour24)
        val radioTimeFormat12 = dialog.findViewById<RadioButton>(R.id.hour12)
        val buttonSave = dialog.findViewById<View>(R.id.buttonSave)
        val buttonCancel = dialog.findViewById<View>(R.id.buttonCancel)

        val sharedPreferences = requireActivity().getSharedPreferences("NopeDotSettings", Context.MODE_PRIVATE)
        val savedTimeFormat = sharedPreferences.getString("timeFormat", null)

        when (savedTimeFormat) {
            "12 Hour" -> radioTimeFormat12.isChecked = true
            "24 Hour" -> radioTimeFormat24.isChecked = true
            "System Default" -> radioTimeFormatDefault.isChecked = true
        }

        buttonSave.setOnClickListener {
            val selectedId = dialog.findViewById<RadioGroup>(R.id.radioGroupTimeFormats).checkedRadioButtonId
            val selectedRadioButton = dialog.findViewById<RadioButton>(selectedId)
            val timeFormat = selectedRadioButton?.text?.toString() ?: "24 Hour"

            saveToPreferences("timeFormat", timeFormat)
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            if (savedTimeFormat == null) {
                saveToPreferences("timeFormat", "24 Hour")
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveToPreferences(key: String, value: String) {
        try {
            val sharedPreferences = requireActivity().getSharedPreferences("NopeDotSettings", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putString(key, value)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("alperkaragozoglu3@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback")
            putExtra(Intent.EXTRA_TEXT, "Merhaba,\n\n")
        }

        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(Intent.createChooser(intent, "Send E-mail"))
        } else {
            Toast.makeText(requireContext(), "Email app not found", Toast.LENGTH_SHORT).show()
        }
    }
}
