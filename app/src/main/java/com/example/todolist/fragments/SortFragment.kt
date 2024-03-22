package com.example.todolist.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.todolist.activity.MainActivity
import com.example.todolist.R
import com.example.todolist.viewmodel.ToDoViewModel
import com.example.todolist.databinding.FragmentSortBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortFragment : BottomSheetDialogFragment()
{
    private lateinit var vm: ToDoViewModel
    private lateinit var binding: FragmentSortBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sort, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        vm = (activity as MainActivity).vm
        binding.viewModel = vm
        binding.dialogFragment = this
    }

    enum class SortOption
    {
        NONE, PRIORITY, DATE, COMPLETED
    }
}