package com.grigroviska.nopedot.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.repository.CategoryRepository
import com.grigroviska.nopedot.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskActivityViewModel(private val repository: TaskRepository) : ViewModel() {

    fun saveTask(newTask : Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.addTask(newTask)
    }

    fun updateTask(existingTask : Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTask(existingTask)
    }

    fun deleteTask(existingTask: Task) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTask(existingTask)
    }

    fun deleteSubItem(subItemText: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteSubItem(subItemText)
    }

    fun searchTask(query: String): LiveData<List<Task>>
    {
        return repository.searchTask(query)
    }

    fun searchTasksByCategory(categoryName: String): LiveData<List<Task>> {
        return repository.searchTasksByCategory(categoryName)
    }

    fun getAllTasks(): LiveData<List<Task>> = repository.getTask()

    fun updateTaskDone(taskId: Int, done: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTaskDone(taskId, done)
    }

}