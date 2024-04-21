package com.grigroviska.nopedot.repository

import com.grigroviska.nopedot.db.TaskDatabase
import com.grigroviska.nopedot.model.Task

class TaskRepository(private val db: TaskDatabase) {

    fun getTask()= db.getTaskDao().getAllTask()

    fun searchTask(query: String)= db.getTaskDao().searchTask(query)

    suspend fun addTask(task: Task) = db.getTaskDao().addTask(task)

    suspend fun updateTask(task: Task) = db.getTaskDao().updateTask(task)

    suspend fun deleteTask(task: Task) = db.getTaskDao().deleteTask(task)

}