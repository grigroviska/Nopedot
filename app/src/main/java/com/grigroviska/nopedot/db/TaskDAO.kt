package com.grigroviska.nopedot.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grigroviska.nopedot.model.Task

@Dao
interface TaskDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM Task ORDER BY CASE WHEN done = 0 THEN 0 ELSE 1 END, id DESC")
    fun getAllTask(): LiveData<List<Task>>

    @Query("SELECT * FROM Task WHERE title LIKE '%' || :query || '%' OR subItems LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchTask(query: String): LiveData<List<Task>>

    @Query("SELECT * FROM Task Where category LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchTasksByCategory(query: String): LiveData<List<Task>>

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM Task WHERE subItems = :subItemText")
    suspend fun deleteSubItem(subItemText: String)

    @Query("UPDATE Task SET done = :done WHERE id = :taskId")
    suspend fun updateTaskDone(taskId: Int, done: Boolean)

}