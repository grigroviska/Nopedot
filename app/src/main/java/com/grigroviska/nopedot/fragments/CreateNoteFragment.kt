package com.grigroviska.nopedot.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.grigroviska.nopedot.databinding.BottomSheetLayoutBinding
import com.google.android.material.transition.MaterialContainerTransform
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen
import com.grigroviska.nopedot.databinding.FragmentCreateNoteBinding
import com.grigroviska.nopedot.model.Note
import com.grigroviska.nopedot.utils.hideKeyboard
import com.grigroviska.nopedot.viewModel.NoteActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date


class CreateNoteFragment : Fragment(R.layout.fragment_create_note) {

    private lateinit var navController : NavController
    private lateinit var contentBinding: FragmentCreateNoteBinding
    private var note: Note?=null
    private var color= -1
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private val currentDate = SimpleDateFormat.getInstance().format(Date())
    private val job = CoroutineScope(Dispatchers.Main)
    private val args: CreateNoteFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            scrimColor= Color.TRANSPARENT
            duration=300L
        }
        sharedElementEnterTransition=animation
        sharedElementReturnTransition=animation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding= FragmentCreateNoteBinding.bind(view)

        navController = Navigation.findNavController(view)
        val activity = activity as HomeScreen

        contentBinding.backButton.setOnClickListener {
            requireView().hideKeyboard()
            navController.popBackStack()
        }

        contentBinding.saveNote.setOnClickListener {
            saveNote()

            try {
                contentBinding.etNoteContent.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus){

                        contentBinding.bottomBar.visibility = View.VISIBLE
                        contentBinding.etNoteContent.setStylesBar(contentBinding.styleBar)

                    }else contentBinding.bottomBar.visibility = View.GONE
                }
            }catch (e : Throwable){

                Log.d("TAG", e.stackTraceToString())

            }

            contentBinding.chooseColorMode.setOnClickListener{

                val bottomSheetDialog = BottomSheetDialog(
                    requireContext(),
                    R.style.BottomSheetDialogTheme
                )
                val bottomSheetView: View =layoutInflater.inflate(
                    R.layout.bottom_sheet_layout,
                    null
                )

                with(bottomSheetDialog){

                    setContentView(bottomSheetView)
                    show()

                }

                val bottomSheetBinding = BottomSheetLayoutBinding.bind(bottomSheetView)
                bottomSheetBinding.apply {
                    colorPicker.apply {
                        setSelectedColor(color)
                        setOnColorSelectedListener {
                            value ->
                            color = value
                            contentBinding.apply {
                                noteContentFragmentParent.setBackgroundColor(color)
                                toolbarFragmentNoteContent.setBackgroundColor(color)
                                bottomBar.setBackgroundColor(color)
                                activity.window.statusBarColor= color
                            }
                            bottomSheetBinding.bottomSheetParent.setCardBackgroundColor(color)
                        }
                    }
                    bottomSheetParent.setCardBackgroundColor(color)
                }
                bottomSheetView.post {
                    bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    private fun saveNote() {
        if (contentBinding.etNoteContent.text.toString().isEmpty() || contentBinding.etTitle.text.toString().isEmpty()) {
            Toast.makeText(activity, "Something is Empty", Toast.LENGTH_SHORT).show()
        } else {
            note = args.note
            when (note) {
                null -> {
                    noteActivityViewModel.saveNote(
                        Note(
                            0,
                            contentBinding.etTitle.text.toString(),
                            contentBinding.etNoteContent.text.toString(),
                            currentDate,
                            color
                        )
                    )
                    navController.navigate(R.id.action_createNoteFragment_to_noteFeedFragment)
                }
                else -> {
                    // update note
                }
            }
        }
    }


}