package com.grigroviska.nopedot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity
data class Task(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    val title: String,
    var done: Boolean = false,
    val subItems: MutableList<String>,
    val category: String,
    var dueDate: String,
    var timeReminder: String,
    var repeatTask: String = "No",
    val color: Int = -1
) : Serializable {

    companion object {
        private const val DATE_FORMAT = "dd/MM/yyyy"
        private const val TIME_FORMAT = "HH:mm"

        fun parseDate(dateString: String): Date? {
            return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateString)
        }

        fun formatDate(date: Date): String {
            return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)
        }

        fun parseTime(timeString: String): Date? {
            return SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).parse(timeString)
        }

        fun formatTime(time: Date): String {
            return SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(time)
        }
    }

    val day: String
        get() = dueDate.split("/")[0]

    val month: String
        get() = dueDate.split("/")[1]

    val year: String
        get() = dueDate.split("/")[2]
}
