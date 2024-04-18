package com.grigroviska.nopedot.activities

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialContainerTransform
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.databinding.ActivityCreateNoteBinding
import com.grigroviska.nopedot.databinding.ActivityHomeScreenBinding
import com.grigroviska.nopedot.fragments.CreateNoteFragmentArgs
import com.grigroviska.nopedot.model.Note
import com.grigroviska.nopedot.viewModel.NoteActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date

class CreateNote : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNoteBinding
    private lateinit var navController: NavController
    private lateinit var contentBinding: ActivityCreateNoteBinding
    private var color = Color.parseColor("#2a2a2a")
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViews()
    }

    private fun setUpViews() {
        binding.apply {
            contentBinding = ActivityCreateNoteBinding.bind(noteContentFragmentParent)

            backButton.setOnClickListener {
                hideKeyboard()
                finish()
            }

            saveNote.setOnClickListener {
                saveNote()
            }

            chooseColorMode.setOnClickListener {
                showColorPickerDialog()
            }
        }

        setUpNote()
    }

    private fun setUpNote() {
        // Eski setUpNote() kodu buraya taşınabilir
    }

    private fun saveNote() {
        // Eski saveNote() kodu buraya taşınabilir
    }

    private fun showColorPickerDialog() {
        // Eski showColorPickerDialog() kodu buraya taşınabilir
    }

    // Diğer yardımcı fonksiyonlar buraya eklenebilir

    private fun hideKeyboard() {
        // Klavye gizleme kodu buraya eklenebilir
    }
}