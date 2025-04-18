package com.grigroviska.nopedot.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.databinding.ActivityHomeScreenBinding
import com.grigroviska.nopedot.db.CategoryDatabase
import com.grigroviska.nopedot.db.NoteDatabase
import com.grigroviska.nopedot.db.TaskDatabase
import com.grigroviska.nopedot.fragments.CreateTaskFragment
import com.grigroviska.nopedot.fragments.NoteFeedFragment
import com.grigroviska.nopedot.fragments.SettingsFragment
import com.grigroviska.nopedot.fragments.TaskFeedFragment
import com.grigroviska.nopedot.repository.CategoryRepository
import com.grigroviska.nopedot.repository.NoteRepository
import com.grigroviska.nopedot.repository.TaskRepository
import com.grigroviska.nopedot.viewModel.CategoryActivityViewModel
import com.grigroviska.nopedot.viewModel.CategoryActivityViewModelFactory
import com.grigroviska.nopedot.viewModel.NoteActivityViewModel
import com.grigroviska.nopedot.viewModel.NoteActivityViewModelFactory
import com.grigroviska.nopedot.viewModel.TaskActivityViewModel
import com.grigroviska.nopedot.viewModel.TaskActivityViewModelFactory

class HomeScreen : AppCompatActivity(){

    private lateinit var noteActivityViewModel: NoteActivityViewModel
    private lateinit var taskActivityViewModel: TaskActivityViewModel
    private lateinit var categoryActivityViewModel: CategoryActivityViewModel
    private lateinit var binding: ActivityHomeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val noteRepository = NoteRepository(NoteDatabase(this))
        val taskRepository = TaskRepository(TaskDatabase(this))
        val categoryRepository = CategoryRepository(CategoryDatabase(this))
        val noteActivityViewModelFactory = NoteActivityViewModelFactory(noteRepository)
        val taskActivityViewModelFactory = TaskActivityViewModelFactory(taskRepository)
        val categoryActivityViewModelFactory = CategoryActivityViewModelFactory(categoryRepository)

        noteActivityViewModel = ViewModelProvider(this,
            noteActivityViewModelFactory)[NoteActivityViewModel::class.java]
        taskActivityViewModel = ViewModelProvider(this,
            taskActivityViewModelFactory)[TaskActivityViewModel::class.java]
        categoryActivityViewModel = ViewModelProvider(this,
            categoryActivityViewModelFactory)[CategoryActivityViewModel::class.java]

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomAppBar.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.note -> {
                    navController.navigate(R.id.noteFeedFragment)
                    true
                }
                R.id.task -> {
                    navController.navigate(R.id.taskFeedFragment)
                    true
                }
                R.id.settings -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }

        val openFragment = intent.getStringExtra("OPEN_FRAGMENT")

        val name= intent.getStringExtra("TASK_NAME")
        val data= intent.getLongExtra("NOTIFICATION_ID",0L)
        if (openFragment == "Create_Task_Fragment") {
            if (name != null && data!=null) {

                openCreateTaskFragmentWithData(data, name)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment,fragment)
        fragmentTransaction.commit()

    }

    fun openCreateTaskFragmentWithData(taskId: Long, name: String) {
        val fragment = CreateTaskFragment()
        val bundle = Bundle()
        bundle.putLong("NOTIFICATION_ID", taskId)
        bundle.putString("TASK_NAME", name)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, fragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bottom_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.note -> {
                replaceFragment(NoteFeedFragment())
                true
            }
            R.id.task -> {
                replaceFragment(TaskFeedFragment())
                true
            }
            R.id.settings -> {
                replaceFragment(SettingsFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment)
        if (currentFragment is NoteFeedFragment || currentFragment is TaskFeedFragment) {
            super.onBackPressed()
        } else {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
    }

}
