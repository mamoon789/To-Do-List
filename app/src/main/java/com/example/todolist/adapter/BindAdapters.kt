package com.example.todolist.adapter

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import com.example.todolist.R
import com.example.todolist.fragments.FilterFragment
import com.example.todolist.fragments.SortFragment
import com.example.todolist.models.Category
import com.example.todolist.models.ToDo
import com.example.todolist.viewmodel.Event
import com.example.todolist.viewmodel.ToDoViewModel
import java.util.Calendar

@BindingAdapter("viewModel", "todo", requireAll = true)
fun ConstraintLayout.updateTodoOnClickListener(viewModel: ToDoViewModel?, todo: ToDo?)
{
    setOnClickListener {
        viewModel?.apply {
            onEvent(Event.UpdateTodoId(todo!!.id))
            onEvent(Event.UpdateTodoTitle(todo.Title))
            onEvent(Event.UpdateTodoCategory(todo.Category))
            onEvent(Event.UpdateTodoTodo(todo.todo))
            onEvent(Event.UpdateTodoCompleted(todo.completed))
            onEvent(Event.UpdateTodoUserId(todo.userId))
            onEvent(Event.UpdateTodoDate(todo.date))
            onEvent(Event.UpdateTodoPriority(todo.priority))

            val bundle = Bundle()
            bundle.putBoolean("isUpdateFlow", true)
            findNavController().navigate(R.id.action_listFragment_to_addFragment, bundle)
        }
    }
}

@BindingAdapter("viewModel", "sortOption", "dialogFragment", requireAll = true)
fun AppCompatRadioButton.sortOnClickListener(
    viewModel: ToDoViewModel, sortOption: SortFragment.SortOption, dialogFragment: SortFragment
)
{
    setOnClickListener {
        viewModel.onEvent(Event.UpdateSortOption(sortOption))
        viewModel.onEvent(Event.QueryTodoList())
        dialogFragment.dismiss()
    }
}

@BindingAdapter("viewModel", "isUpdateFlow", requireAll = true)
fun AppCompatButton.saveDeleteOnClickListener(
    viewModel: ToDoViewModel, isUpdateFlow: Boolean
)
{
    setOnClickListener {
        when (id)
        {
            R.id.btSave -> if (isUpdateFlow) viewModel.onEvent(Event.UpdateTodo()) else viewModel.onEvent(
                Event.AddTodo()
            )

            R.id.btDelete -> viewModel.onEvent(Event.DeleteTodo())
        }
    }
}

@BindingAdapter("viewModel", "dialogFragment", requireAll = true)
fun AppCompatButton.filterOnClickListener(
    viewModel: ToDoViewModel, dialogFragment: FilterFragment
)
{
    setOnClickListener {
        viewModel.onEvent(Event.QueryTodoList())
        dialogFragment.dismiss()
    }
}

@BindingAdapter("viewModel", "todo", requireAll = true)
fun AppCompatCheckBox.completeOnCheckedChangedListener(
    viewModel: ToDoViewModel?, todo: ToDo?
)
{
    setOnClickListener {
        if (viewModel != null && todo != null)
        {
            todo.completed = !todo.completed
            viewModel.onEvent(Event.UpdateTodo(todo))
        }
    }
}

@BindingAdapter("viewModel", "filter", requireAll = true)
fun AppCompatCheckBox.filterOnCheckedChangedListener(
    viewModel: ToDoViewModel?, filter: Category?
)
{
    setOnCheckedChangeListener { _, isChecked ->
        if (viewModel != null && filter != null)
        {
            val categoryList = viewModel.categoryList.value.map {
                if (it == filter) Category(filter.name, isChecked) else it
            }
            viewModel.onEvent(Event.UpdateCategoryList(categoryList))
        }
    }
}

@BindingAdapter("viewModel", requireAll = true)
fun AppCompatTextView.dialogOnClickListener(viewModel: ToDoViewModel)
{
    setOnClickListener {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(
            it.context, { view, year, monthOfYear, dayOfMonth ->
                viewModel.onEvent(Event.UpdateTodoDate("$dayOfMonth - ${monthOfYear + 1} - $year"))
            }, year, month, day
        ).show()
    }
}

@BindingAdapter("priority", requireAll = true)
fun AppCompatTextView.setPriorityTextColor(priority: String?)
{
    if (priority != null)
    {
        setTextColor(
            when (priority)
            {
                "High" -> ContextCompat.getColor(context, R.color.red)
                "Low" -> ContextCompat.getColor(context, R.color.green)
                else -> ContextCompat.getColor(context, R.color.yellow)
            }
        )
    }
}

@BindingAdapter("viewModel", requireAll = true)
fun AppCompatEditText.customAddTextChangedListener(viewModel: ToDoViewModel)
{
    addTextChangedListener(object : TextWatcher
    {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
        {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
        {
            when (id)
            {
                R.id.etTitle -> viewModel.onEvent(Event.UpdateTodoTitle(p0.toString()))
                R.id.etTodo -> viewModel.onEvent(Event.UpdateTodoTodo(p0.toString()))
            }
        }

        override fun afterTextChanged(p0: Editable?)
        {
        }
    })
}

@BindingAdapter("viewModel", "selection", requireAll = true)
fun AppCompatSpinner.customSetSelection(viewModel: ToDoViewModel, selection: String)
{
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener
    {
        override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, p3: Long)
        {
            when (id)
            {
                R.id.spCategory -> viewModel.onEvent(Event.UpdateTodoCategory(viewModel.categoryList.value[position].name))
                R.id.spPriority -> viewModel.onEvent(
                    Event.UpdateTodoPriority(
                        listOf(
                            "High", "Medium", "Low"
                        )[position]
                    )
                )
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?)
        {
        }
    }

    val index = when (id)
    {
        R.id.spCategory -> viewModel.categoryList.value.map { it.name }.indexOf(selection)
        R.id.spPriority -> listOf("High", "Medium", "Low").indexOf(selection)
        else ->
        {
            -1
        }
    }
    if (index > 0) setSelection(index)
}
