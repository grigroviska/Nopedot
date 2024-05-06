package com.grigroviska.nopedot.repository

import com.grigroviska.nopedot.db.TaskDatabase
import com.grigroviska.nopedot.model.Task

class TaskRepository(private val db: TaskDatabase) {

    fun getTask()= db.getTaskDao().getAllTask()

    fun searchTask(query: String)= db.getTaskDao().searchTask(query)

    fun searchTasksByCategory(query: String)= db.getTaskDao().searchTasksByCategory(query)

    suspend fun addTask(task: Task) = db.getTaskDao().addTask(task)

    suspend fun updateTask(task: Task) = db.getTaskDao().updateTask(task)

    suspend fun deleteTask(task: Task) = db.getTaskDao().deleteTask(task)

    suspend fun deleteSubItem(subItem : String) = db.getTaskDao().deleteSubItem(subItem)

    suspend fun updateTaskDone(taskId: Int, done: Boolean) {
        db.getTaskDao().updateTaskDone(taskId, done)
    }

}