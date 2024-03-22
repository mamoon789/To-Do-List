package com.example.todolist.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.models.Category
import com.example.todolist.viewmodel.Event
import com.example.todolist.activity.MainActivity
import com.example.todolist.R
import com.example.todolist.repository.Resource
import com.example.todolist.viewmodel.ToDoViewModel
import com.example.todolist.databinding.FragmentListBinding
import com.example.todolist.databinding.TodoRowBinding
import kotlinx.coroutines.launch

class ListFragment : Fragment(), MenuProvider
{
    private lateinit var vm: ToDoViewModel
    private lateinit var binding: FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_list, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        activity?.addMenuProvider(this, viewLifecycleOwner)
        vm = (activity as MainActivity).vm
        binding.adapter = Adapter()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { it ->
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
                                Saving categories in viewmodel on initial call only.
                             */

                            if (vm.categoryList.value.isEmpty())
                            {
                                val categoryList = it.data.distinctBy { todo -> todo.Category }
                                    .map { todo -> Category(todo.Category) }
                                vm.onEvent(Event.UpdateCategoryList(categoryList))
                            }
                            vm.onEvent(Event.UpdateTodoList(it.data))
                        }
                    }
                }
            }
        }

        /*
            Updating adapter with latest tasks
         */

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.todoList.collect {
                    binding.adapter?.notifyDataSetChanged()
                }
            }
        }
    }


    inner class Adapter() : RecyclerView.Adapter<Adapter.ViewHolder>()
    {
        inner class ViewHolder(val binding: TodoRowBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val binding: TodoRowBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context), R.layout.todo_row, parent, false
            )
            binding.lifecycleOwner = viewLifecycleOwner
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.binding.todo = vm.todoList.value[position]
            holder.binding.viewModel = vm
        }

        override fun getItemCount(): Int
        {
            return vm.todoList.value.size
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater)
    {
        menuInflater.inflate(R.menu.menu, menu)
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : OnQueryTextListener
        {
            override fun onQueryTextSubmit(query: String?): Boolean
            {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean
            {
                vm.onEvent(Event.UpdateSearchText(newText ?: ""))
                vm.onEvent(Event.QueryTodoList())
                return true
            }
        })
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean
    {
        when (menuItem.itemId)
        {
            R.id.sort ->
            {
                findNavController().navigate(R.id.action_listFragment_to_sortFragment)
            }

            R.id.filter ->
            {
                findNavController().navigate(R.id.action_listFragment_to_filterFragment)
            }

            R.id.add ->
            {
                findNavController().navigate(R.id.action_listFragment_to_addFragment)
            }
        }
        return false
    }
}