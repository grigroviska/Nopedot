package com.grigroviska.nopedot.listener

import com.grigroviska.nopedot.model.Task

interface TaskItemClickListener {
    fun onTaskItemClicked(task: Task)
}