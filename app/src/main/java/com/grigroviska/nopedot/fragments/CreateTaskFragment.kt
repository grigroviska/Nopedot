package com.grigroviska.nopedot.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
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
import com.grigroviska.nopedot.databinding.FragmentCreateTaskBinding
import com.grigroviska.nopedot.model.Category
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.utils.hideKeyboard
import com.grigroviska.nopedot.viewModel.CategoryActivityViewModel
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateTaskFragment : Fragment(R.layout.fragment_create_task) {

    private lateinit var navController : NavController
    private lateinit var contentBinding: FragmentCreateTaskBinding
    private lateinit var selectedRepeatOption: String
    private var task: Task? = null
    private val taskActivityViewModel: TaskActivityViewModel by activityViewModels()
    private val categoryActivityViewModel: CategoryActivityViewModel by activityViewModels()
    private val openedEditTextList = mutableListOf<EditText>()
    private var color: Int = -1
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: CreateTaskFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding = FragmentCreateTaskBinding.bind(view)

        navController = Navigation.findNavController(view)
        val activity = activity as HomeScreen

        ViewCompat.setTransitionName(
            contentBinding.taskContentFragmentParent,
            "recyclerView_${args.task?.id}"
        )

        contentBinding.backButton.setOnClickListener {
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        contentBinding.saveTask.setOnClickListener {
            task = args.task

            val iterator = openedEditTextList.iterator()
            while (iterator.hasNext()) {
                val editText = iterator.next()
                if (editText.text.isBlank()) {
                    contentBinding.taskContainer.removeView(editText)
                    iterator.remove()
                }
            }

            val newSubItems = getOpenedEditTexts()

            if (task != null) {
                val newColor = color

                if (newColor == task!!.color) {
                    taskActivityViewModel.updateTask(
                        Task(
                            task!!.id,
                            contentBinding.etTitle.text.toString(),
                            false,
                            newSubItems,
                            contentBinding.category.text.toString(),
                            contentBinding.dueDateValue.text.toString(),
                            contentBinding.timeReminderValue.text.toString(),
                            contentBinding.repeatTaskValue.text.toString(),
                            task!!.color
                        )
                    )
                } else {
                    taskActivityViewModel.updateTask(
                        Task(
                            task!!.id,
                            contentBinding.etTitle.text.toString(),
                            false,
                            newSubItems,
                            contentBinding.category.text.toString(),
                            contentBinding.dueDateValue.text.toString(),
                            contentBinding.timeReminderValue.text.toString(),
                            contentBinding.repeatTaskValue.text.toString(),
                            newColor
                        )
                    )
                }
            }

            requireView().hideKeyboard()
            navController.popBackStack()
        }


        setupCategorySelection(contentBinding.category)

        contentBinding.dueDate.setOnClickListener{
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    contentBinding.dueDateValue.text = selectedDate
                },
                year,
                month,
                dayOfMonth
            )
            datePickerDialog.show()
        }


        contentBinding.timeReminder.setOnClickListener {
            showTimePickerDialog()
        }

        contentBinding.repeatTask.setOnClickListener {
            showRepeatTaskDialog()
        }

        contentBinding.addSubTask.setOnClickListener {
            val newEditText = EditText(requireContext())
            newEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.margin_top)
                leftMargin = resources.getDimensionPixelSize(R.dimen.margin_start)
                rightMargin = resources.getDimensionPixelSize(R.dimen.margin_end)
            }
            newEditText.hint = "New subtext"
            newEditText.requestFocus()
            newEditText.background = null

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
                            contentBinding.taskContainer.removeView(newEditText)
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

        contentBinding.chooseColorMode.setOnClickListener{

            val bottomSheetDialog = BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )
            val bottomSheetView: View =layoutInflater.inflate(
                R.layout.bottom_sheet_layout,
                null
            )

            with(bottomSheetDialog){

                setContentView(bottomSheetView)
                show()

            }

            val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)
            bottomSheetBinding.apply {
                colorPicker.apply {
                    setSelectedColor(color!!)
                    setOnColorSelectedListener {
                            value ->
                        color = value
                        contentBinding.apply {
                            etTitle.setTextColor(color!!)
                        }
                    }
                }

            }
            bottomSheetView.post {
                bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        setUpTask()
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


    private fun setUpTask() {
        val task= args.task
        val title = contentBinding.etTitle
        val date : TextView = contentBinding.dueDateValue
        val timeReminder : TextView = contentBinding.timeReminderValue
        val repeatTask : TextView = contentBinding.repeatTaskValue
        val category : MaterialButton = contentBinding.category

        if (task!=null){
            title.setText(task.title)
            category.setText(task.category)
            contentBinding.apply {
                etTitle.setTextColor(task.color)
            }
            date.text = task.dueDate
            timeReminder.setText(task.timeReminder)
            repeatTask.setText(task.repeatTask)

            color = task.color

            val subItems = task.subItems

            for (subItem in subItems) {
                val newEditText = EditText(requireContext())
                newEditText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.margin_top)
                    leftMargin = resources.getDimensionPixelSize(R.dimen.margin_start)
                    rightMargin = resources.getDimensionPixelSize(R.dimen.margin_end)
                }

                newEditText.setText(subItem)
                newEditText.background = null

                openedEditTextList.add(newEditText)

                val removeIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.remove)
                newEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    removeIcon,
                    null
                )
                contentBinding.taskContainer.addView(newEditText)

                // Inside onViewCreated method, where you set up the remove icon
                removeIcon?.let { icon ->
                    icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                    newEditText.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_UP) {
                            val removeBounds =
                                (newEditText.right - newEditText.compoundDrawables[2].bounds.width())
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

                contentBinding.apply {
                job.launch {
                    delay(10)
                }
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

    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                contentBinding.timeReminderValue.text = formattedTime
            },
            hour,
            minute,
            true
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
}
