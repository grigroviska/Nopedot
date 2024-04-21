package com.grigroviska.nopedot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Task(

    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    val title: String,
    val subItems: MutableList<String>,
    val category: MutableList<String>,
    var reminderDate: String,

    ): Serializable
