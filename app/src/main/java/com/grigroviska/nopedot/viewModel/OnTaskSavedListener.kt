package com.grigroviska.nopedot.viewModel

import com.grigroviska.nopedot.model.Task

interface OnTaskSavedListener{
    fun onTaskSaved(task: Task)
}