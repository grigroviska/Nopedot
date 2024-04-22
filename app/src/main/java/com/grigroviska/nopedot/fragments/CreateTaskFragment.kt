package com.grigroviska.nopedot.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen
import com.grigroviska.nopedot.databinding.FragmentCreateTaskBinding
import com.grigroviska.nopedot.model.Note
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.utils.hideKeyboard
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
    private var task: Task?=null
    private val taskActivityViewModel: TaskActivityViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val openedEditTextList = mutableListOf<EditText>()
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: CreateTaskFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor= Color.TRANSPARENT
            duration=300L
        }
        sharedElementEnterTransition=animation
        sharedElementReturnTransition=animation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding= FragmentCreateTaskBinding.bind(view)

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

        }

        contentBinding.dueDate.setOnClickListener{
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    contentBinding.dueDateValue.setText(selectedDate)
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

        setUpTask()
    }
    private fun setUpTask() {
        val task= args.task
        val title = contentBinding.etTitle
        val date : TextView = contentBinding.dueDateValue
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        if (task!=null){
            title.setText(task.title)
            if (task.dueDate == ""){
                date.setText(currentDate)
            }
            date.setText(task.dueDate)

            val subItems = task.subItems

            // Mevcut subItems listesindeki öğeleri döngüye alarak newEditText öğelerini oluşturup değerlerini yazdırma
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

            contentBinding.apply {
                job.launch {
                    delay(10)

                }

            }
        }
    }

    private fun showTimePickerDialog() {
        val currentTime = Calendar.getInstance()
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                contentBinding.timeReminderValue.setText(formattedTime)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }
}