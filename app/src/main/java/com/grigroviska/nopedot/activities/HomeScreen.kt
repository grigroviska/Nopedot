package com.grigroviska.nopedot.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.databinding.ActivityHomeScreenBinding
import com.grigroviska.nopedot.db.NoteDatabase
import com.grigroviska.nopedot.db.TaskDatabase
import com.grigroviska.nopedot.repository.NoteRepository
import com.grigroviska.nopedot.repository.TaskRepository
import com.grigroviska.nopedot.viewModel.NoteActivityViewModel
import com.grigroviska.nopedot.viewModel.NoteActivityViewModelFactory
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import com.grigroviska.nopedot.viewModel.TaskActivityViewModelFactory

class HomeScreen : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    lateinit var taskActivityViewModel: TaskActivityViewModel
    private lateinit var binding: ActivityHomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        val view = binding.root

        try {
            setContentView(view)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val taskRepository = TaskRepository(TaskDatabase(this))
            val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
            val taskActivityViewModelFactory = TaskActivityViewModelFactory(taskRepository)
            noteActivityViewModel = ViewModelProvider(this,
                noteActivityViewModelFactory)[NoteActivityViewModel::class.java]
            taskActivityViewModel = ViewModelProvider(this,
                taskActivityViewModelFactory)[TaskActivityViewModel::class.java]

            try {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
                val navController = navHostFragment.navController

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    if (destination.id == R.id.createNoteFragment) {
                        binding.topBar.visibility = View.GONE
                    } else {
                        binding.topBar.visibility = View.VISIBLE
                    }
                }

                        binding.noteList.setOnClickListener {
                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination != R.id.noteFeedFragment) {
                        navController.navigate(R.id.noteFeedFragment)
                    }else{

                    }
                }

                binding.taskList.setOnClickListener {
                    val currentDestination = navController.currentDestination?.id
                    if (currentDestination != R.id.taskFeedFragment) {
                        navController.navigate(R.id.taskFeedFragment)
                    }else{

                    }
                }
            }catch (e: Exception){

                Toast.makeText(this,e.localizedMessage, Toast.LENGTH_SHORT).show()

            }


        }catch (e: Exception){

            Toast.makeText(this,e.localizedMessage, Toast.LENGTH_SHORT).show()

        }


    }
}