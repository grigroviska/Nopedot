package com.grigroviska.nopedot.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.grigroviska.nopedot.R
import com.grigroviska.nopedot.databinding.FragmentNoteFeedBinding
import com.grigroviska.nopedot.databinding.FragmentTaskFeedBinding


class TaskFeedFragment : Fragment() {

    private lateinit var binding: FragmentTaskFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        return inflater.inflate(R.layout.fragment_task_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskFeedBinding.bind(view)
    }

}