package com.grigroviska.nopedot.adapters

import androidx.recyclerview.widget.DiffUtil
import com.grigroviska.nopedot.model.Task

class TaskDiffUtilCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
