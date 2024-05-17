package com.grigroviska.nopedot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Task(

    @PrimaryKey(autoGenerate = true)
    var id: Long=0L,
    val title: String,
    var done: Boolean=false,
    val subItems: MutableList<String>,
    val category: String,
    var dueDate: String,
    var timeReminder: String,
    var repeatTask: String = "No",
    val color: Int = -1

    ): Serializable{

    val day: String
        get() = dueDate.split(".")[0]

    val month: String
        get() = dueDate.split(".")[1]

    val year: String
        get() = dueDate.split(".")[2]

    }
