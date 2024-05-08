package com.grigroviska.nopedot.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.grigroviska.nopedot.databinding.CategoryItemLayoutBinding
import com.grigroviska.nopedot.model.Category
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel

class RvTaskCategoryAdapter (private val taskActivityViewModel: TaskActivityViewModel,
                             private val viewLifecycleOwner: LifecycleOwner,
                             private val rvAdapter: RvTasksAdapter
) :
    ListAdapter<Category, RvTaskCategoryAdapter.CategoryViewHolder>(CategoryDiffUtilCallback()) {

    inner class CategoryViewHolder(private val binding: CategoryItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var category: Category
        val categoryTitle : MaterialButton = binding.categoryName
        val categoryParent : MaterialCardView = binding.categoryParent

        fun bind(category: Category) {
            this.category = category
            categoryTitle.text = category.categoryName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CategoryItemLayoutBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)

        getItem(position).let {category ->

            holder.apply {
                val categoryName = category.categoryName

                categoryParent.setOnClickListener {
                    taskActivityViewModel.searchTasksByCategory(categoryName)
                        .observe(viewLifecycleOwner) { filteredTasks ->
                            rvAdapter.submitList(filteredTasks)
                        }
                }

                categoryTitle.setOnClickListener {
                    taskActivityViewModel.searchTasksByCategory(categoryName)
                        .observe(viewLifecycleOwner) { filteredTasks ->
                            rvAdapter.submitList(filteredTasks)
                        }
                }
            }

        }

    }

    fun setCategories(categories: List<Category>) {
        submitList(categories)
    }

    }
