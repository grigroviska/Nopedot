package com.grigroviska.nopedot.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grigroviska.nopedot.model.Category
import com.grigroviska.nopedot.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryActivityViewModel(private val repository: CategoryRepository) : ViewModel() {

    fun saveCategory(category: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.addCategory(category)
    }

    fun updateCategory(existingCategory : Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCategory(existingCategory)
    }

    fun deleteCategory(existingCategory: Category) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteCategory(existingCategory)
    }

    fun searchCategory(query: String): LiveData<List<Category>> {
        return repository.searchCategory(query)
    }

    fun getAllCategories(): LiveData<List<Category>> = repository.getCategory()

}