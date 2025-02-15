package com.grigroviska.nopedot.fragments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen
import com.grigroviska.nopedot.adapters.RvTasksAdapter
import com.grigroviska.nopedot.databinding.FragmentTaskFeedBinding
import com.grigroviska.nopedot.listener.TaskItemClickListener
import com.grigroviska.nopedot.viewModel.OnTaskSavedListener
import com.grigroviska.nopedot.model.Category
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.receiver.AlarmReceiver
import com.grigroviska.nopedot.utils.SwipeToDelete
import com.grigroviska.nopedot.utils.hideKeyboard
import com.grigroviska.nopedot.viewModel.CategoryActivityViewModel
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class TaskFeedFragment : Fragment(), TaskItemClickListener {

    private lateinit var binding : FragmentTaskFeedBinding
    private lateinit var dialog : BottomSheetDialog
    private lateinit var rvAdapter: RvTasksAdapter

    private val openedEditTextList = mutableListOf<EditText>()
    private var selectedLastDate : String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    private var selectedLastTime : String = ""
    private val taskActivityViewModel : TaskActivityViewModel by activityViewModels()
    private val categoryActivityViewModel : CategoryActivityViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_task_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskFeedBinding.bind(view)
        activity as HomeScreen
        Navigation.findNavController(view)

        requireView().hideKeyboard()

        insertDefaultCategoriesIfNotExist()
        swipeToDelete(binding.rvTask)
        try{
        recyclerViewDisplay() }
        catch (e: Exception){
            Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()

        }

        binding.floatingActionButton.setOnClickListener {
            showBottomSheet()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (!query.isNullOrEmpty()) {
                    val searchText = "%$query"
                    taskActivityViewModel.searchTask(searchText).observe(viewLifecycleOwner) { tasks ->
                        rvAdapter.submitList(tasks)
                    }
                } else {
                    observerDataChanges()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    observerDataChanges()
                } else {
                    val searchText = "%$newText"
                    taskActivityViewModel.searchTask(searchText).observe(viewLifecycleOwner) { tasks ->
                        rvAdapter.submitList(tasks)}
                }
                return true
            }
        })

        binding.searchView.setOnSearchClickListener {
            requireView().hideKeyboard()
        }

    }

    private fun showBottomSheet() {
        openedEditTextList.clear()
        val dialogView = layoutInflater.inflate(R.layout.new_task, null)
        dialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        dialog.setContentView(dialogView)

        val branchImageView = dialogView.findViewById<ImageView>(R.id.branch)
        val calendarImageView : ImageView = dialogView.findViewById(R.id.calendar)
        val save : MaterialButton = dialogView.findViewById(R.id.saveTask)
        val category: MaterialButton = dialogView.findViewById(R.id.category)
        val calendarText : TextView = dialogView.findViewById(R.id.calendarText)
        val newTask : EditText = dialogView.findViewById(R.id.newTask)
        val taskContainer = dialogView.findViewById<LinearLayout>(R.id.taskContainer)

        var isDateTimePicked = false

        setupCategorySelection(category)

        branchImageView.setOnClickListener {
            val newEditText = EditText(requireContext())
            newEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.margin_top)
            }
            newEditText.hint = getString(R.string.new_subtext)
            newEditText.requestFocus()
            openedEditTextList.add(newEditText)

            val removeIcon = AppCompatResources.getDrawable(requireContext(), R.drawable.remove)
            newEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, removeIcon, null)
            taskContainer.addView(newEditText)

            removeIcon?.let { icon ->
                icon.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                newEditText.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        val removeBounds = (newEditText.right - newEditText.compoundDrawables[2].bounds.width())
                        if (event.rawX >= removeBounds) {
                            taskContainer.removeView(newEditText)
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

        val currentDate = Calendar.getInstance()
        val dayOfMonth = currentDate.get(Calendar.DAY_OF_MONTH)
        calendarText.text = "$dayOfMonth"
        selectedLastDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentDate.time).toString()
        calendarImageView.setOnClickListener {
            isDateTimePicked = true
            val currentDate = Calendar.getInstance()

            if (selectedLastDate.isNotEmpty()) {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedLastDate)?.apply {
                    currentDate.time = this
                }
            }

            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val day = currentDate.get(Calendar.DAY_OF_MONTH)
            val hour = if (selectedLastTime.isNotEmpty()) {
                selectedLastTime.split(":")[0].toInt()
            } else {
                currentDate.get(Calendar.HOUR_OF_DAY)
            }
            val minute = if (selectedLastTime.isNotEmpty()) {
                selectedLastTime.split(":")[1].toInt()
            } else {
                currentDate.get(Calendar.MINUTE)
            }

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour)
                    selectedDate.set(Calendar.MINUTE, selectedMinute)

                    val selectedDayOfMonth = selectedDate.get(Calendar.DAY_OF_MONTH)
                    calendarText.text = "$selectedDayOfMonth"

                    selectedLastDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time).toString()
                    selectedLastTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                }, hour, minute, true)

                timePickerDialog.show()
            }, year, month, day)

            datePickerDialog.setOnCancelListener {
                selectedLastDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(currentDate.time).toString()
            }

            datePickerDialog.show()
        }


        save.setOnClickListener {
            try {
                if (newTask.text.isNotEmpty()) {
                    val iterator = openedEditTextList.iterator()
                    while (iterator.hasNext()) {
                        val editText = iterator.next()
                        if (editText.text.isBlank()) {
                            taskContainer.removeView(editText)
                            iterator.remove()
                        }
                    }

                    val task = Task(
                        0,
                        newTask.text.toString(),
                        false,
                        getOpenedEditTexts(),
                        category.text.toString(),
                        selectedLastDate,
                        selectedLastTime,
                        "No",
                        -1
                    )

                    taskActivityViewModel.saveTask(task, object : OnTaskSavedListener {
                        override fun onTaskSaved(taskWithId: Task) {
                            val taskId = taskWithId.id
                            if (taskId > 0) {

                                if (isDateTimePicked) {
                                    try {
                                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        val selectedDate = dateFormat.parse("$selectedLastDate $selectedLastTime")

                                        selectedDate?.let {
                                            val calendar = Calendar.getInstance().apply {
                                                time = it
                                            }

                                            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                            val intent = Intent(context, AlarmReceiver::class.java).apply {
                                                putExtra("TASK_NAME", newTask.text.toString())
                                                putExtra("NOTIFICATION_ID", taskId)
                                            }
                                            val pendingIntent = PendingIntent.getBroadcast(
                                                context,
                                                taskId.toInt(),
                                                intent,
                                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                            )
                                            alarmManager.set(
                                                AlarmManager.RTC_WAKEUP,
                                                calendar.timeInMillis,
                                                pendingIntent
                                            )
                                            activity?.runOnUiThread {
                                            }
                                        } ?: run {
                                            activity?.runOnUiThread {
                                                Toast.makeText(requireContext(),
                                                    getString(R.string.error_parsing_date_and_time), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        activity?.runOnUiThread {
                                            Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                                dialog.dismiss()
                            } else {
                                Log.e("TaskSave", getString(R.string.task_could_not_be_saved))
                            }
                        }
                    })
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.please_enter_a_task_name), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), e.stackTraceToString(), Toast.LENGTH_LONG).show()
                }
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
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
                    showNewCategoryDialog(categoryButton)
                } else {
                    categoryButton.text = categoryName
                }
                true
            }
            categoryButton.setOnClickListener { popupMenu.show() }
        }
    }

    private fun showNewCategoryDialog(category: MaterialButton) {
        val dialogView = layoutInflater.inflate(R.layout.create_new_category, null)
        val newCategoryNameEditText = dialogView.findViewById<EditText>(R.id.newCategory)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.save).setOnClickListener {
            val newCategoryName = newCategoryNameEditText.text.toString().trim()

            if (newCategoryName.isNotEmpty()) {
                categoryActivityViewModel.getCategoryByName(newCategoryName).observe(viewLifecycleOwner) { categories ->
                    if (categories.isEmpty()) {
                        val newCategory = Category(0, newCategoryName)
                        categoryActivityViewModel.saveCategory(newCategory)
                        dialog.dismiss()
                    } else {
                        category.text = newCategoryName
                        dialog.dismiss()
                    }
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


    private fun swipeToDelete(rvTask: RecyclerView) {

        val swipeToDeleteCallback = object : SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val task= rvAdapter.currentList[position]
                var actionBtnTapped= false
                taskActivityViewModel.deleteTask(task)
                binding.searchView.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if (binding.searchView.toString().isEmpty()){
                    observerDataChanges()

                }
                val snackBar= Snackbar.make(
                    requireView(), "Task Deleted!", Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {

                        transientBottomBar?.setAction("UNDO"){
                            taskActivityViewModel.undoTask(task)
                            actionBtnTapped=true
                            binding.noData.isVisible =false
                        }

                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode= Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.floatingActionButton)
                }
                snackBar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.soft_yellow2
                    )
                )
                snackBar.show()
            }
        }
        val itemTouchHelper= ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvTask)
    }

    private fun getOpenedEditTexts(): MutableList<String> {
        val textList = mutableListOf<String>()
        for (editText in openedEditTextList) {
            textList.add(editText.text.toString())
        }
        return textList
    }

    private fun recyclerViewDisplay() {
        when(resources.configuration.orientation){
            Configuration.ORIENTATION_PORTRAIT-> setUpRecyclerView(1)
            Configuration.ORIENTATION_LANDSCAPE-> setUpRecyclerView(2)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {

        try {
            binding.rvTask.apply {
                layoutManager =
                    StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                setHasFixedSize(true)
                rvAdapter = RvTasksAdapter(taskActivityViewModel, this@TaskFeedFragment)
                rvAdapter.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                adapter = rvAdapter

                postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
            }
        }catch (e: Exception){

            Toast.makeText(requireContext(), e.stackTraceToString(), Toast.LENGTH_LONG).show()

        }

        observerDataChanges()

    }

    private fun observerDataChanges() {
        taskActivityViewModel.getAllTasks().observe(viewLifecycleOwner){list->
            binding.noData.isVisible = list.isEmpty()
            rvAdapter.submitList(list){
                binding.rvTask.layoutManager?.smoothScrollToPosition(binding.rvTask, null, 0)
            }
        }
    }

    private fun insertDefaultCategoriesIfNotExist() {
        val defaultCategories = listOf("Work", "Personal", "Wishlist", "Shopping")

        val existingCategories = categoryActivityViewModel.getAllCategories()
        existingCategories.observe(viewLifecycleOwner) { categories ->
            val existingCategoryNames = categories.map { it.categoryName }

            val newCategories = defaultCategories.filter { !existingCategoryNames.contains(it) }
            newCategories.forEach { categoryName ->
                val category = Category(0, categoryName)
                categoryActivityViewModel.saveCategory(category)
            }
        }
    }

    override fun onTaskItemClicked(task: Task) {
        val action = TaskFeedFragmentDirections.actionTaskFeedFragmentToCreateTaskFragment(task)
        view?.let { Navigation.findNavController(it).navigate(action) }

    }

}