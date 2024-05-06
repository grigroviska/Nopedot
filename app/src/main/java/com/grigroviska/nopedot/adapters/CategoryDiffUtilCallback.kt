package com.grigroviska.nopedot.adapters

import androidx.recyclerview.widget.DiffUtil
import com.grigroviska.nopedot.model.Category

class CategoryDiffUtilCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}