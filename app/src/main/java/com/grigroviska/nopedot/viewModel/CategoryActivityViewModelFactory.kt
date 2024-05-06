package com.grigroviska.nopedot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.nopedot.repository.CategoryRepository

class CategoryActivityViewModelFactory(private val repository: CategoryRepository) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoryActivityViewModel(repository) as T
    }

}