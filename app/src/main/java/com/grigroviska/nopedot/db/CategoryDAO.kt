package com.grigroviska.nopedot.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grigroviska.nopedot.model.Category

@Dao
interface CategoryDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("SELECT * FROM Category ORDER BY id DESC")
    fun getAllCategory(): LiveData<List<Category>>

    @Query("SELECT * FROM Category WHERE categoryName LIKE '%' || :categoryName || '%' ORDER BY id DESC")
    fun searchCategory(categoryName: String): LiveData<List<Category>>

    @Query("SELECT * FROM Category WHERE categoryName = :name")
    fun getCategoryByName(name: String): LiveData<List<Category>>

    @Delete
    suspend fun deleteCategory(category: Category)

}