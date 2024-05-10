package com.grigroviska.nopedot.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Subscription(

    @PrimaryKey(autoGenerate = true)
    var id: Int=0,
    val companyName: String,
    val description: String,
    val firstPayment: String,
    val color: Int = Color.parseColor("#111111"),
    val note : String,



    ) : Serializable