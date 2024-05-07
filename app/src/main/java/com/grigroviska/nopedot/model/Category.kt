package com.grigroviska.nopedot.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Category", indices = [Index(value = ["categoryName"], unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryName: String
) : Serializable
