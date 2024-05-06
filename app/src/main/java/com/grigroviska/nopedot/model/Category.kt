package com.grigroviska.nopedot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Category(

    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    val categoryName: String,

    ): Serializable
