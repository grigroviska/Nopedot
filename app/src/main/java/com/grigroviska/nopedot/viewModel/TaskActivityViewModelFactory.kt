package com.grigroviska.nopedot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.nopedot.repository.TaskRepository

class TaskActivityViewModelFactory(private val repository: TaskRepository) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskActivityViewModel(repository) as T
    }

}