package com.grigroviska.nopedot.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grigroviska.nopedot.model.Category

@Database(
    entities = [Category::class],
    version = 1,
    exportSchema = false
)
abstract class CategoryDatabase : RoomDatabase() {

    abstract fun getCategoryDao(): CategoryDAO

    companion object{

        @Volatile
        private var instance : CategoryDatabase?= null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){

            instance ?: createDatabase(context).also {
                instance=it
            }

        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            CategoryDatabase::class.java,
            "category_database"
        ).build()

    }
}