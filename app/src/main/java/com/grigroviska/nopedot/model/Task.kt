package com.grigroviska.nopedot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Task(

    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    val title: String,
    var done: Boolean=false,
    val subItems: MutableList<String>,
    val category: String,
    var dueDate: String,
    var timeReminder: String,
    var repeatTask: String = "No",
    val color: Int = -1,

    ): Serializable{

    val day: String
        get() = dueDate.split(".")[0] // Günü almak için "." karakterinden böl

    val month: String
        get() = dueDate.split(".")[1] // Ayı almak için "." karakterinden böl

    val year: String
        get() = dueDate.split(".")[2] // Yılı almak için "." karakterinden böl

    }
