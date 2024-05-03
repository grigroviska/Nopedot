package com.grigroviska.nopedot.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.databinding.TaskItemLayoutBinding
import com.grigroviska.nopedot.fragments.TaskFeedFragmentDirections
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.utils.hideKeyboard
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import java.text.DateFormatSymbols

class RvTasksAdapter(private val taskActivityViewModel: TaskActivityViewModel) : ListAdapter<Task, RvTasksAdapter.TaskViewHolder>(TaskDiffUtilCallback()) {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentBinding = TaskItemLayoutBinding.bind(itemView)
        val title: TextView = contentBinding.taskCheckBox
        val parent: MaterialCardView = contentBinding.taskItemLayout
        val day : TextView = contentBinding.day
        val month : TextView = contentBinding.month
        val year : TextView = contentBinding.year
        val subItems : TextView = contentBinding.subItems
        val checkboxTask : CheckBox = contentBinding.taskCheckBox
        val taskItemLayout : MaterialCardView = contentBinding.taskItemLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.task_item_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        getItem(position).let { task ->
            holder.apply {
                parent.transitionName = "recyclerView_${task.id}"
                title.text = task.title
                checkboxTask.isChecked = task.done
                title.setTextColor(task.color)
                day.text = task.day
                month.text = getMonthName(task.month.toInt())
                year.text = task.year
                val subItemsText = task.subItems.joinToString(", ")
                subItems.text = subItemsText

                taskItemLayout.setOnClickListener {
                    val action = TaskFeedFragmentDirections.actionTaskFeedFragmentToCreateTaskFragment(task)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${task.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action, extras)
                }

                title.apply {
                    if (task.done) {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                }

                title.setOnClickListener {
                    val action = TaskFeedFragmentDirections.actionTaskFeedFragmentToCreateTaskFragment(task)
                    val extras = FragmentNavigatorExtras(parent to "recyclerView_${task.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action, extras)
                }

                checkboxTask.setOnClickListener {
                    val isChecked = checkboxTask.isChecked
                    task.done = isChecked
                    taskActivityViewModel.updateTaskDone(task.id, isChecked)

                    title.apply {
                        if (isChecked) {
                            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        } else {
                            paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                        }
                    }
                }
            }
        }
    }

    private fun getMonthName(monthNumber: Int): String {
        return DateFormatSymbols().months[monthNumber - 1]
    }
}
