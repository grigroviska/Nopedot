package com.grigroviska.nopedot.adapters

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.databinding.TaskItemLayoutBinding
import com.grigroviska.nopedot.listener.TaskItemClickListener
import com.grigroviska.nopedot.model.Task
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import java.text.DateFormatSymbols

class RvTasksAdapter(
    private val taskActivityViewModel: TaskActivityViewModel,
    private val itemClickListener: TaskItemClickListener
) : ListAdapter<Task, RvTasksAdapter.TaskViewHolder>(TaskDiffUtilCallback()) {

    inner class TaskViewHolder(private val binding: TaskItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            with(binding) {
                // Önceki dinleyiciyi kaldır
                taskCheckBox.setOnCheckedChangeListener(null)

                // CheckBox güncellemesi
                taskCheckBox.text = task.title
                taskCheckBox.isChecked = task.done
                taskCheckBox.paintFlags = if (task.done) {
                    taskCheckBox.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    taskCheckBox.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // Veritabanından gelen renk ile başlık rengini güncelle
                val titleColor = if (task.color != -1) { // -1 varsayılan olarak "renk yok" anlamında
                    task.color
                } else {
                    ContextCompat.getColor(root.context, R.color.white) // Varsayılan renk
                }
                taskCheckBox.setTextColor(titleColor)

                // Tarih bilgisi
                day.text = task.day
                month.text = getMonthName(task.month.toInt())
                year.text = task.year

                // Alt öğeler
                subItems.text = task.subItems.joinToString("\n") { "\u2022 $it" }

                // **COLOR LAYOUT RENK GÜNCELLEME**
                val colorRes = if (task.done) R.color.doneColor else R.color.thirdColor
                val color = ContextCompat.getColor(root.context, colorRes)
                colorLayout.setBackgroundColor(color) // ✅ Renk sıfırlanıyor ve güncelleniyor

                // Hatırlatma ikonunu güncelle
                reminder.visibility = if (task.timeReminder.isNotEmpty()) View.VISIBLE else View.GONE

                // **CheckBox tıklanınca güncelle**
                taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
                    task.done = isChecked
                    taskActivityViewModel.updateTaskDone(task.id, isChecked)
                    notifyItemChanged(adapterPosition) // 🎯 ColorLayout rengi güncelleniyor
                }

                // Kart tıklama işlemi
                taskItemLayout.setOnClickListener {
                    itemClickListener.onTaskItemClicked(task)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: List<Task>?) {
        super.submitList(list?.sortedBy { it.done })
    }

    private fun getMonthName(monthNumber: Int): String {
        return DateFormatSymbols().months[monthNumber - 1]
    }
}