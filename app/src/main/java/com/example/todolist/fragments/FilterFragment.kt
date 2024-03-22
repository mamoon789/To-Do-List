package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.activity.MainActivity
import com.example.todolist.R
import com.example.todolist.viewmodel.ToDoViewModel
import com.example.todolist.databinding.FilterRowBinding
import com.example.todolist.databinding.FragmentFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch


class FilterFragment : BottomSheetDialogFragment()
{
    private lateinit var vm: ToDoViewModel
    private lateinit var binding: FragmentFilterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        isCancelable = false
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        vm = (activity as MainActivity).vm
        binding.adapter = Adapter()
        binding.viewModel = vm
        binding.dialogFragment = this

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.categoryList.collect {
                    binding.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    inner class Adapter() : RecyclerView.Adapter<Adapter.ViewHolder>()
    {
        inner class ViewHolder(val binding: FilterRowBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val binding: FilterRowBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.filter_row,
                parent,
                false
            )
            binding.lifecycleOwner = viewLifecycleOwner
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.binding.filter = vm.categoryList.value[position]
            holder.binding.viewModel = vm
        }

        override fun getItemCount(): Int
        {
            return vm.categoryList.value.size
        }
    }
}