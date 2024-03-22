package com.example.todolist.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.todolist.viewmodel.Event
import com.example.todolist.activity.MainActivity
import com.example.todolist.R
import com.example.todolist.repository.Resource
import com.example.todolist.viewmodel.ToDoViewModel
import com.example.todolist.databinding.FragmentAddBinding
import kotlinx.coroutines.launch

class AddFragment : Fragment()
{
    private lateinit var vm: ToDoViewModel
    private lateinit var binding: FragmentAddBinding

    private val onBackPressedCallback = object : OnBackPressedCallback(true)
    {
        override fun handleOnBackPressed()
        {
            vm.apply {

                /*
                     Call to get the updated, filtered and sorted tasks
                 */

                onEvent(Event.QueryTodoList())

                onEvent(Event.UpdateTodoId(0))
                onEvent(Event.UpdateTodoTitle(""))
                onEvent(Event.UpdateTodoCategory(""))
                onEvent(Event.UpdateTodoTodo(""))
                onEvent(Event.UpdateTodoCompleted(false))
                onEvent(Event.UpdateTodoUserId(0))
                onEvent(Event.UpdateTodoDate(""))
                onEvent(Event.UpdateTodoPriority(""))
            }
            remove()
            activity?.onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        vm = (activity as MainActivity).vm
        binding.viewModel = vm
        binding.fragment = this
        binding.isUpdateFlow = arguments?.getBoolean("isUpdateFlow") ?: false
        binding.lifecycleOwner = viewLifecycleOwner

        val categoryAdapter = ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_item,
            vm.categoryList.value.map { it.name }
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categoryAdapter = categoryAdapter

        val priorityAdapter = ArrayAdapter(
            view.context,
            android.R.layout.simple_spinner_item,
            listOf("High", "Medium", "Low")
        )
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.priorityAdapter = priorityAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState2.collect { it ->
                    when (it)
                    {
                        is Resource.Error ->
                        {
                            binding.loader = false

                            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                        }

                        is Resource.Loading ->
                        {
                            binding.loader = true

                        }

                        is Resource.Success ->
                        {
                            binding.loader = false

                            /*
                                Move to previous screen
                             */

                            onBackPressedCallback.handleOnBackPressed()
                        }
                    }
                }
            }
        }
    }

    override fun onResume()
    {
        super.onResume()

        /*
            Custom back press callback
         */

        activity?.onBackPressedDispatcher?.addCallback(onBackPressedCallback)
    }

    override fun onStop()
    {
        super.onStop()
        onBackPressedCallback.remove()
    }
}