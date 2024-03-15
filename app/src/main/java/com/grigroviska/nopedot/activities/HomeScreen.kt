package com.grigroviska.nopedot.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.nopedot.databinding.ActivityHomeScreenBinding
import com.grigroviska.nopedot.db.NoteDatabase
import com.grigroviska.nopedot.repository.NoteRepository
import com.grigroviska.nopedot.viewModel.NoteActivityViewModel
import com.grigroviska.nopedot.viewModel.NoteActivityViewModelFactory

class HomeScreen : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var binding: ActivityHomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        val view = binding.root

        try {
            setContentView(view)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(this,
                noteActivityViewModelFactory)[NoteActivityViewModel::class.java]
        }catch (e: Exception){

            Log.d("TAG","ERROR")

        }
    }
}