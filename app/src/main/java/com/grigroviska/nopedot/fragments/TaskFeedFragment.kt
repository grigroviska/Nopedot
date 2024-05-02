package com.grigroviska.nopedot.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen
import com.grigroviska.nopedot.adapters.RvNotesAdapter
import com.grigroviska.nopedot.adapters.RvTaskCategoryAdapter
import com.grigroviska.nopedot.adapters.RvTasksAdapter
import com.grigroviska.nopedot.databinding.FragmentTaskFeedBinding
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.receiver.AlarmReceiver
import com.grigroviska.nopedot.utils.SwipeToDelete
import com.grigroviska.nopedot.utils.hideKeyboard
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class TaskFeedFragment : Fragment() {

    private lateinit var binding : FragmentTaskFeedBinding
    private lateinit var dialog : BottomSheetDialog
    private lateinit var rvAdapter: RvTasksAdapter
    private lateinit var rvCategory: RvTaskCategoryAdapter
    lateinit var categories : ArrayList<String>
    private val openedEditTextList = mutableListOf<EditText>()
    var selectedLastDate : String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    var selectedLastTime : String = "No"
    private val taskActivityViewModel : TaskActivityViewModel by activityViewModels()

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
        val activity= activity as HomeScreen
        val navController= Navigation.findNavController(view)
        requireView().hideKeyboard()

        swipeToDelete(binding.rvTask)
        try{
        recyclerViewDisplay() }
        catch (e: Exception){
            Log.e("Hata", "ff" ,e)

        }

        categories = ArrayList()
        categories.add("Work")
        categories.add("Personal")
        categories.add("Wishlist")
        categories.add("Shopping")

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

    private fun showBottomSheet(){
        val dialogView = layoutInflater.inflate(R.layout.new_task, null)
        dialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        dialog.setContentView(dialogView)

        val branchImageView = dialogView.findViewById<ImageView>(R.id.branch)
        val calendarImageView : ImageView = dialogView.findViewById<ImageView>(R.id.calendar)
        val save : MaterialButton = dialogView.findViewById<MaterialButton>(R.id.saveTask)
        val category: MaterialButton = dialogView.findViewById<MaterialButton>(R.id.category)
        val calendarText : TextView = dialogView.findViewById<TextView>(R.id.calendarText)
        val newTask : EditText = dialogView.findViewById<EditText>(R.id.newTask)
        val taskContainer = dialogView.findViewById<LinearLayout>(R.id.taskContainer)

        category.setOnClickListener {

            val popupMenu = PopupMenu(requireContext(), it)
            for (category in categories) {
                popupMenu.menu.add(category)
            }
            popupMenu.menu.add("New Category")
            popupMenu.setOnMenuItemClickListener { item ->
                val categoryName = item.title.toString()
                if (categoryName == "Yeni kategori ekle") {

                } else {
                    category.text = categoryName
                }
                true
            }
            popupMenu.show()

        }

        branchImageView.setOnClickListener {
            val newEditText = EditText(requireContext())
            newEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.margin_top)
            }
            newEditText.hint = "New subtext"

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
        calendarText.text = dayOfMonth.toString()
        selectedLastDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(currentDate.time).toString()
        calendarImageView.setOnClickListener {
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val day = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val timePickerDialog = TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    selectedDate.set(Calendar.HOUR_OF_DAY, selectedHour)
                    selectedDate.set(Calendar.MINUTE, selectedMinute)

                    val selectedDayOfMonth = selectedDate.get(Calendar.DAY_OF_MONTH)
                    calendarText.text = selectedDayOfMonth.toString()

                    selectedLastDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(selectedDate.time).toString()
                    selectedLastTime = SimpleDateFormat("hh:mm:mm", Locale.getDefault()).format(selectedDate.time).toString()

                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true)

                timePickerDialog.show()
            }, year, month, day)

            datePickerDialog.setOnCancelListener {
                selectedLastDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(currentDate.time).toString()
            }

            datePickerDialog.show()
        }

        save.setOnClickListener {

            try {
                if (newTask.text.isNotEmpty()){

                    val task = Task(0,newTask.text.toString(), getOpenedEditTexts(), category.text.toString() , selectedLastDate, selectedLastTime, "No", -1)
                    taskActivityViewModel.saveTask(task)
                    dialog.dismiss()
                }
            }catch (e: Exception){

                Toast.makeText(requireContext(), e.stackTraceToString(), Toast.LENGTH_LONG).show()
                Log.e("Hata", "Bir hata oluştu", e)

            }

        }

        dialog.show()
    }

    private fun swipeToDelete(rvNote: RecyclerView) {

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
                            taskActivityViewModel.saveTask(task)
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
                        R.color.yellowOrange
                    )
                )
                snackBar.show()
            }
        }
        val itemTouchHelper= ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)
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
                layoutManager= StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                setHasFixedSize(true)
                rvAdapter= RvTasksAdapter()
                rvAdapter.stateRestorationPolicy=
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                adapter=rvAdapter
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

    /*private fun alarm(taskName : String, taskValue : String, year : Int, month : Int, day : Int, hour : Int, minute : Int){
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), AlarmReceiver::class.java)

        intent.putExtra(taskName, taskValue)

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

    }*/

    private fun observerDataChanges() {
        taskActivityViewModel.getAllTasks().observe(viewLifecycleOwner){list->
            binding.noData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)
        }
    }

}