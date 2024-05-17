package com.grigroviska.nopedot.repository

import androidx.lifecycle.LiveData
import com.grigroviska.nopedot.db.TaskDatabase
import com.grigroviska.nopedot.model.Task

class TaskRepository(private val db: TaskDatabase) {

    fun getTask()= db.getTaskDao().getAllTask()

    fun getTaskById(taskId: Long): LiveData<Task> {
        return db.getTaskDao().getTaskById(taskId)
    }

    fun searchTask(query: String)= db.getTaskDao().searchTask(query)

    fun searchTasksByCategory(query: String)= db.getTaskDao().searchTasksByCategory(query)

    suspend fun addTask(task: Task) : Long = db.getTaskDao().addTask(task)

    suspend fun updateTask(task: Task) = db.getTaskDao().updateTask(task)

    suspend fun deleteTask(task: Task) = db.getTaskDao().deleteTask(task)

    suspend fun deleteSubItem(subItem : String) = db.getTaskDao().deleteSubItem(subItem)

    suspend fun updateTaskDone(taskId: Long, done: Boolean) {
        db.getTaskDao().updateTaskDone(taskId, done)
    }

}