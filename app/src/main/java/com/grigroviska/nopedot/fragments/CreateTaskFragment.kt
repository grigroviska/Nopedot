package com.grigroviska.nopedot.fragments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen
import com.grigroviska.nopedot.databinding.BottomSheetLayoutBinding
import com.grigroviska.nopedot.databinding.ColorPickerTaskLayoutBinding
import com.grigroviska.nopedot.databinding.FragmentCreateTaskBinding
import com.grigroviska.nopedot.model.Category
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.receiver.AlarmReceiver
import com.grigroviska.nopedot.utils.hideKeyboard
import com.grigroviska.nopedot.viewModel.CategoryActivityViewModel
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateTaskFragment : Fragment(R.layout.fragment_create_task) {

    private lateinit var navController : NavController
    private lateinit var contentBinding: FragmentCreateTaskBinding
    private var selectedRepeatOption: String = "No"
    private var taskRem: Task? = null
    private val taskActivityViewModel: TaskActivityViewModel by activityViewModels()
    private val categoryActivityViewModel: CategoryActivityViewModel by activityViewModels()
    private val openedEditTextList = mutableListOf<EditText>()
    private var color: Int = -1
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: CreateTaskFragmentArgs by navArgs()
    private lateinit var dateFormat : String
    private lateinit var firstDayOfWeek : String
    private lateinit var hourFormat : String
    var taskId : Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentCreateTaskBinding.bind(view)

        navController = Navigation.findNavController(view)
        activity as HomeScreen

        val sharedPreferences = requireActivity().getSharedPreferences("NopeDotSettings", Context.MODE_PRIVATE)
        dateFormat = sharedPreferences.getString("dateFormat", "dd/MM/yyyy").toString()
        firstDayOfWeek = sharedPreferences.getString("firstDayOfWeek", "Sunday") ?: "Sunday"
        hourFormat = sharedPreferences.getString("hourFormat", "24 Hour").toString()

        taskId = arguments?.getLong("NOTIFICATION_ID", 0L)!!
        setupTaskWithTaskId(taskId)
        if(args.task!=null){
            setupTaskWithTask(args.task!!)
        }

        contentBinding.backButton.setOnClickListener {
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        contentBinding.saveTask.setOnClickListener {
            if (args.task != null) {
                taskRem = args.task
                processTask(taskRem)
            } else {
                taskActivityViewModel.getTaskById(taskId).observe(viewLifecycleOwner) { task ->
                    if (task != null) {

                        taskRem = task
                        processTask(taskRem)
                    }
                }
            }
        }

        setupCategorySelection(contentBinding.category)

        contentBinding.dueDate.setOnClickListener{
            showDatePickerDialog()
        }


        contentBinding.timeReminder.setOnClickListener {
            showTimePickerDialog()
        }

        contentBinding.repeatTask.setOnClickListener {
            showRepeatTaskDialog()
        }

        contentBinding.addSubTask.setOnClickListener {
            addSubTaskEditText()
        }

        contentBinding.chooseColorMode.setOnClickListener{
            showColorPickerDialog()
        }
    }

    private fun processTask(task: Task?) {
        task?.let { currentTask ->
            val newColor = color

            val dueDateText = contentBinding.dueDateValue.text.toString()
            val timeReminderText = contentBinding.timeReminderValue.text.toString()

            val defaultDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val defaultTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            val dueDateForDB = if (dueDateText.isNotEmpty()) {
                try {
                    val userDate = SimpleDateFormat(dateFormat, Locale.getDefault()).parse(dueDateText)
                    defaultDateFormat.format(userDate!!)
                } catch (e: Exception) {
                    Log.e("DateParseError", "Failed to parse date", e)
                    dueDateText // Kullanıcı formatı ayrıştırılamazsa orijinali kullan
                }
            } else {
                ""
            }

            val timeReminderForDB = if (timeReminderText.isNotEmpty() && timeReminderText != "No") {
                try {
                    val userTime = SimpleDateFormat(if (hourFormat == "24 Hour") "HH:mm" else "hh:mm a", Locale.getDefault()).parse(timeReminderText)
                    defaultTimeFormat.format(userTime!!)
                } catch (e: Exception) {
                    Log.e("TimeParseError", "Failed to parse time", e)
                    timeReminderText // Kullanıcı formatı ayrıştırılamazsa orijinali kullan
                }
            } else {
                "" // Hiç zaman hatırlatıcısı yoksa boş bırak
            }

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val context = requireContext()

            val existingIntent = PendingIntent.getBroadcast(
                context,
                currentTask.id.toInt(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            val selectedDate: Date? = if (dueDateText.isNotEmpty() && timeReminderText.isNotEmpty()) {
                try {
                    Task.parseDate(dueDateText)?.let { date ->
                        Task.parseTime(timeReminderText)?.let { time ->
                            Calendar.getInstance().apply {
                                set(Calendar.YEAR, date.year + 1900) // Java Calendar'da yıl 1900'den başlar
                                set(Calendar.MONTH, date.month) // 0-tabanlı, bu yüzden değişiklik yok
                                set(Calendar.DAY_OF_MONTH, date.date)
                                set(Calendar.HOUR_OF_DAY, time.hours)
                                set(Calendar.MINUTE, time.minutes)
                            }.time
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DateTimeParseError", "Failed to parse date and time", e)
                    null
                }
            } else {
                null
            }

            existingIntent?.let { alarmManager.cancel(it) }

            if (selectedDate != null && !isPastDate(selectedDate)) {
                val calendar = Calendar.getInstance().apply { time = selectedDate }
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    putExtra("TASK_NAME", contentBinding.etTitle.text.toString())
                    putExtra("NOTIFICATION_ID", currentTask.id)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    currentTask.id.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    when (selectedRepeatOption) {
                        "No" -> alarmManager.set(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                        else -> {
                            val repeatInterval = getRepeatInterval(selectedRepeatOption)
                            if (repeatInterval > 0) {
                                alarmManager.setRepeating(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    repeatInterval,
                                    pendingIntent
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AlarmSetError", "Failed to set alarm", e)
                    Toast.makeText(context, "Failed to set alarm", Toast.LENGTH_SHORT).show()
                }
            }

            try {
                taskActivityViewModel.updateTask(
                    Task(
                        currentTask.id,
                        contentBinding.etTitle.text.toString(),
                        false,
                        getOpenedEditTexts(),
                        contentBinding.category.text.toString(),
                        dueDateForDB,
                        timeReminderForDB,
                        selectedRepeatOption,
                        newColor
                    )
                )
            } catch (e: Exception) {
                Log.e("TaskUpdateError", "Failed to update task", e)
                Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
            }

            requireView().hideKeyboard()
            navController.popBackStack()
        } ?: run {
            Log.e("TaskError", "Task is null")
            Toast.makeText(requireContext(), "Task is null, cannot process", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPastDate(selectedDate: Date): Boolean {
        val currentTime = Calendar.getInstance().time
        return selectedDate.before(currentTime)
    }

    private fun getRepeatInterval(repeatOption: String): Long {
        return when (repeatOption) {
            "One Time" -> 0
            "Hourly" -> AlarmManager.INTERVAL_HOUR
            "Daily" -> AlarmManager.INTERVAL_DAY
            "Weekly" -> AlarmManager.INTERVAL_DAY * 7
            "Monthly" -> 30L * 24 * 60 * 60 * 1000
            "Yearly" -> 365L * 24 * 60 * 60 * 1000
            else -> 0
        }
    }


    private fun addSubTaskEditText(subItemText: String = "") {
        val newEditText = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.margin_top)
                leftMargin = resources.getDimensionPixelSize(R.dimen.margin_start)
                rightMargin = resources.getDimensionPixelSize(R.dimen.margin_end)
            }
            hint = getString(R.string.new_subtext)
            setText(subItemText)
            background = null
            requestFocus()
        }

        openedEditTextList.add(newEditText)

        val removeIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.remove)
        newEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, removeIcon, null)
        contentBinding.taskContainer.addView(newEditText)

        removeIcon?.let { icon ->
            icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
            newEditText.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val removeBounds = (newEditText.right - newEditText.compoundDrawables[2].bounds.width())
                    if (event.rawX >= removeBounds) {
                        showDeleteConfirmationDialog(newEditText, newEditText.text.toString())
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        }
    }

    private fun showRepeatTaskDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.repeat_layout)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val done = dialog.findViewById<TextView>(R.id.done)
        val hour = dialog.findViewById<TextView>(R.id.hour)
        val daily = dialog.findViewById<TextView>(R.id.daily)
        val weekly = dialog.findViewById<TextView>(R.id.weekly)
        val monthly = dialog.findViewById<TextView>(R.id.monthly)
        val yearly = dialog.findViewById<TextView>(R.id.yearly)

        cancel.setOnClickListener {
            dialog.dismiss()
        }
        done.setOnClickListener {
            selectedRepeatOption = when {
                hour.alpha == 1.0f -> "Hourly"
                daily.alpha == 1.0f -> "Daily"
                weekly.alpha == 1.0f -> "Weekly"
                monthly.alpha == 1.0f -> "Monthly"
                yearly.alpha == 1.0f -> "Yearly"
                else -> "No"
            }
            contentBinding.repeatTaskValue.text = selectedRepeatOption
            dialog.dismiss()
        }

        hour.setOnClickListener { selectRepeatOption(hour, dialog) }
        daily.setOnClickListener { selectRepeatOption(daily, dialog) }
        weekly.setOnClickListener { selectRepeatOption(weekly, dialog) }
        monthly.setOnClickListener { selectRepeatOption(monthly, dialog) }
        yearly.setOnClickListener { selectRepeatOption(yearly, dialog) }

        dialog.show()
    }

    private fun selectRepeatOption(selectedOption: TextView, dialog: Dialog) {
        val hour = dialog.findViewById<TextView>(R.id.hour)
        val daily = dialog.findViewById<TextView>(R.id.daily)
        val weekly = dialog.findViewById<TextView>(R.id.weekly)
        val monthly = dialog.findViewById<TextView>(R.id.monthly)
        val yearly = dialog.findViewById<TextView>(R.id.yearly)

        hour.alpha = 0.5f
        daily.alpha = 0.5f
        weekly.alpha = 0.5f
        monthly.alpha = 0.5f
        yearly.alpha = 0.5f

        selectedOption.alpha = 1.0f
    }

    private fun setupTaskWithTaskId(taskId: Long) {
        taskActivityViewModel.getTaskById(taskId).observe(viewLifecycleOwner) { task ->
            task?.let {
                setUpTask(task)
            }
        }
    }

    private fun setupTaskWithTask(task: Task) {
        setUpTask(task)
    }

    private fun setUpTask(task: Task?) {
        task?.let { currentTask ->
            val title = contentBinding.etTitle
            val date: TextView = contentBinding.dueDateValue
            val timeReminder: TextView = contentBinding.timeReminderValue
            val repeatTask: TextView = contentBinding.repeatTaskValue
            val category: MaterialButton = contentBinding.category

            title.setText(currentTask.title)
            category.setText(currentTask.category)
            contentBinding.apply {
                etTitle.setTextColor(currentTask.color)
                color = currentTask.color
            }
            if (currentTask.dueDate.isNotEmpty()) {
                val storedDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Veritabanında nasıl saklandığı varsayımı
                val userDateFormatter = SimpleDateFormat(dateFormat, Locale.getDefault())

                try {
                    val dateToParse = storedDateFormat.parse(currentTask.dueDate)
                    date.text = userDateFormatter.format(dateToParse!!)
                } catch (e: Exception) {
                    // Eğer format uyuşmazlığı varsa, orijinal değeri kullan
                    date.text = currentTask.dueDate
                }
            }

            if (currentTask.timeReminder.isNotEmpty()) {
                // Saat formatını belirliyoruz
                val is24HourFormat = hourFormat == "24 Hour"
                val timeFormat = if (is24HourFormat) "HH:mm" else "hh:mm a"
                val timeFormatter = SimpleDateFormat(timeFormat, Locale.getDefault())

                try {
                    // Veritabanından gelen saati 24 saatlik formatta alıyoruz ve ardından kullanıcı formatına dönüştürüyoruz
                    val timeToParse = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(currentTask.timeReminder)
                    timeReminder.text = timeFormatter.format(timeToParse!!)
                } catch (e: Exception) {
                    // Eğer format uyumsuzluğu varsa, orijinal değeri kullan
                    timeReminder.text = currentTask.timeReminder
                }
            }

            repeatTask.setText(currentTask.repeatTask)

            val subItems = currentTask.subItems
            for (subItem in subItems) {
                addSubTaskEditText(subItem)
            }
        }
    }

    private fun showDeleteConfirmationDialog(newEditText: EditText, subItemText: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_sub_item))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                openedEditTextList.removeIf { it.text.toString() == subItemText }
                contentBinding.taskContainer.removeView(newEditText)
                taskActivityViewModel.deleteSubItem(subItemText)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Delete Sub item")
        alert.show()
    }

    private fun showDatePickerDialog() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

        val currentDueDate = contentBinding.dueDateValue.text.toString()
        var selectedYear = year
        var selectedMonth = month
        var selectedDay = dayOfMonth

        if (currentDueDate.isNotEmpty()) {
            try {
                // Kullanıcı tercihine göre tarihi ayrıştır
                val dateFormatParser = SimpleDateFormat(dateFormat, Locale.getDefault())
                val date = dateFormatParser.parse(currentDueDate)
                val calendar = Calendar.getInstance()
                calendar.time = date!!
                selectedYear = calendar.get(Calendar.YEAR)
                selectedMonth = calendar.get(Calendar.MONTH)
                selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)

                // Kullanıcı tercihine göre tarihi formatla
                val formattedDate = SimpleDateFormat(dateFormat, Locale.getDefault()).format(selectedCalendar.time)
                contentBinding.dueDateValue.text = formattedDate
            },
            selectedYear,
            selectedMonth,
            selectedDay
        )

        datePickerDialog.setOnShowListener {
            val datePicker = datePickerDialog.datePicker
            when (firstDayOfWeek) {
                "Monday" -> datePicker.firstDayOfWeek = Calendar.MONDAY
                "Saturday" -> datePicker.firstDayOfWeek = Calendar.SATURDAY
                else -> datePicker.firstDayOfWeek = Calendar.SUNDAY
            }
        }

        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()

        // SharedPreferences veya global değişken ile saat formatını al
        val sharedPreferences = requireActivity().getSharedPreferences("NopeDotSettings", Context.MODE_PRIVATE)
        val savedTimeFormat = sharedPreferences.getString("timeFormat", "24 Hour") // Varsayılan değer 24 Hour

        val is24HourFormat = savedTimeFormat == "24 Hour" // 12 Hour veya 24 Hour kontrolü

        // TimePickerDialog oluşturuluyor
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // Saat formatına göre zaman formatını belirliyoruz
                val timeFormat = if (is24HourFormat) "HH:mm" else "hh:mm a"

                // Zamanı uygun formatta gösteriyoruz
                contentBinding.timeReminderValue.text = SimpleDateFormat(timeFormat, Locale.getDefault()).format(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            is24HourFormat // Seçilen formata göre saat formatı
        )

        timePickerDialog.show()
    }



    private fun getOpenedEditTexts(): MutableList<String> {
        val textList = mutableListOf<String>()
        for (editText in openedEditTextList) {
            textList.add(editText.text.toString())
        }
        return textList
    }

    private fun setupCategorySelection(categoryButton: MaterialButton) {
        categoryActivityViewModel.getAllCategories().observe(viewLifecycleOwner) { categories ->
            val popupMenu = PopupMenu(requireContext(), categoryButton)
            categories.forEach { category ->
                popupMenu.menu.add(category.categoryName)
            }
            popupMenu.menu.add("New Category")
            popupMenu.setOnMenuItemClickListener { item ->
                val categoryName = item.title.toString()
                if (categoryName == "New Category") {
                    showNewCategoryDialog()
                    categoryButton.text = categoryName
                } else {
                    categoryButton.text = categoryName
                }
                true
            }
            categoryButton.setOnClickListener { popupMenu.show() }
        }
    }

    private fun showNewCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.create_new_category, null)
        val newCategoryNameEditText = dialogView.findViewById<EditText>(R.id.newCategory)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.save).setOnClickListener {
            val newCategoryName = newCategoryNameEditText.text.toString().trim()

            if (newCategoryName.isNotEmpty()) {
                if (isExistingCategory(newCategoryName)) {
                    Toast.makeText(requireContext(), "Category already exists.", Toast.LENGTH_SHORT).show()
                } else {
                    val newCategory = Category(0, newCategoryName)
                    categoryActivityViewModel.saveCategory(newCategory)
                    contentBinding.category.text = newCategoryName
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a category name.", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<TextView>(R.id.cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun isExistingCategory(categoryName: String): Boolean {
        val existingCategories = categoryActivityViewModel.getAllCategories().value
        existingCategories?.let { categories ->
            for (category in categories) {
                if (category.categoryName.equals(categoryName, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun showColorPickerDialog() {
        val bottomSheetDialog = BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialogTheme
        )
        val bottomSheetView: View = layoutInflater.inflate(
            R.layout.color_picker_task_layout,
            null
        )

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        val colorPickerTaskLayoutBinding = ColorPickerTaskLayoutBinding.bind(bottomSheetView)

        colorPickerTaskLayoutBinding.colorPicker.apply {

            setSelectedColor(color)
            setOnColorSelectedListener { value ->
                color = value
                contentBinding.etTitle.setTextColor(color)
            }
        }

        bottomSheetView.post {
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

}
