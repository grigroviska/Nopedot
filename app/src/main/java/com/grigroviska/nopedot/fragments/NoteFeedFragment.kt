package com.grigroviska.nopedot.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.activities.HomeScreen
import com.grigroviska.nopedot.adapters.RvNotesAdapter
import com.grigroviska.nopedot.databinding.FragmentNoteFeedBinding
import com.grigroviska.nopedot.utils.SwipeToDelete
import com.grigroviska.nopedot.utils.hideKeyboard
import com.grigroviska.nopedot.viewModel.NoteActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class NoteFeedFragment : Fragment(R.layout.fragment_note_feed) {

    private lateinit var binding: FragmentNoteFeedBinding
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private lateinit var rvAdapter: RvNotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition= MaterialElevationScale(false).apply {
            duration=350
        }
        enterTransition=MaterialElevationScale(true).apply {
            duration=350
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNoteFeedBinding.bind(view)
        val activity= activity as HomeScreen
        val navController= Navigation.findNavController(view)
        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            activity?.window?.statusBarColor = Color.parseColor("#1a110e")
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

        binding.floatingActionButton.setOnClickListener {
            navController.navigate(R.id.action_noteFeedFragment_to_createNoteFragment)
        }

        recyclerViewDisplay()

        swipeToDelete(binding.rvNote)

        binding.searchView.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.noData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty()){
                    val text = s.toString()
                    val query="%$text"
                    if (query.isNotEmpty()){
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner){
                            rvAdapter.submitList(it)
                        }
                    }else{
                        observerDataChanges()
                    }
                }else{
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.searchView.setOnEditorActionListener{v, actionId, _ ->
            if (actionId== EditorInfo.IME_ACTION_SEARCH){
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }

    }

    private fun swipeToDelete(rvNote: RecyclerView) {

        val swipeToDeleteCallback = object : SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note= rvAdapter.currentList[position]
                var actionBtnTapped= false
                noteActivityViewModel.deleteNote(note)
                binding.searchView.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if (binding.searchView.text.toString().isEmpty()){
                    observerDataChanges()

                }
                val snackBar= Snackbar.make(
                    requireView(), "Note Deleted!", Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {

                        transientBottomBar?.setAction("UNDO"){
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped=true
                            binding.noData.isVisible =false
                        }

                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode= Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.floatingActionButton)
                }
                snackBar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackBar.show()
            }
        }
        val itemTouchHelper= ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)
    }

    private fun recyclerViewDisplay() {
        when(resources.configuration.orientation){
            Configuration.ORIENTATION_PORTRAIT-> setUpRecyclerView(2)
            Configuration.ORIENTATION_LANDSCAPE-> setUpRecyclerView(3)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {

        binding.rvNote.apply {
            layoutManager= StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rvAdapter= RvNotesAdapter()
            rvAdapter.stateRestorationPolicy=
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter=rvAdapter
            postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()

    }

    private fun observerDataChanges() {
        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner){list->
            binding.noData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)
        }
    }

}