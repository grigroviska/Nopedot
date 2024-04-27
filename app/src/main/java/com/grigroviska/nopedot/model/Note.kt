package com.grigroviska.nopedot.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Note(

    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    val title: String,
    val content: String,
    val date: String,
    val color: Int = Color.parseColor("#111111"),


    ) : Serializable
