package com.grigroviska.nopedot.repository

import com.grigroviska.nopedot.db.CategoryDatabase
import com.grigroviska.nopedot.model.Category

class CategoryRepository(private val db: CategoryDatabase) {

    fun getCategory()= db.getCategoryDao().getAllCategory()

    fun searchCategory(query: String)= db.getCategoryDao().searchCategory(query)

    suspend fun addCategory(category: Category) = db.getCategoryDao().addCategory(category)

    suspend fun updateCategory(category: Category) = db.getCategoryDao().updateCategory(category)

    suspend fun deleteCategory(category: Category) = db.getCategoryDao().deleteCategory(category)

}