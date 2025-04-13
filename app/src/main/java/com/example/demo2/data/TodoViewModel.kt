package com.example.demo2.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.demo2.TodoItem
import com.example.demo2.TodoPriority
import com.example.demo2.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = TodoRepository.getInstance(database.todoDao(), database.pendingOperationDao())
        viewModelScope.launch {
            //repository.syncWithRemote()
        }
    }
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    private val _selectedPriority = MutableStateFlow(TodoPriority.HIGH)
    val selectedPriority = _selectedPriority.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()
    
    private val _newTodoTitle = MutableStateFlow("")
    val newTodoTitle = _newTodoTitle.asStateFlow()
    
    private val _newTodoPriority = MutableStateFlow(TodoPriority.HIGH)
    val newTodoPriority = _newTodoPriority.asStateFlow()

    val filteredTodos: StateFlow<List<TodoItem>> = combine(
        repository.todos,
        _searchQuery,
        _selectedPriority
    ) { todos, query, priority ->
         todos.filter { todo ->
            todo.title.contains(query, ignoreCase = true) &&
                    ((todo.priority == priority && !todo.isCompleted) || (todo.isCompleted && priority== TodoPriority.COMPLETED))
            }.sortedByDescending { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSelectedPriority(priority: TodoPriority) {
        _selectedPriority.value = priority
    }
    
    fun toggleAddDialog(show: Boolean) {
        _showAddDialog.value = show
        if (!show) {
            resetNewTodoFields()
        }
    }
    
    fun setNewTodoTitle(title: String) {
        _newTodoTitle.value = title
    }
    
    fun setNewTodoPriority(priority: TodoPriority) {
        _newTodoPriority.value = priority
    }
    
    fun addNewTodo() {
        val title = _newTodoTitle.value.trim()
        if (title.isNotEmpty()) {
            viewModelScope.launch {
                repository.addTodo(title, _newTodoPriority.value)
                resetNewTodoFields()
                toggleAddDialog(false)
            }
        }
    }
    
    private fun resetNewTodoFields() {
        _newTodoTitle.value = ""
        _newTodoPriority.value = TodoPriority.MEDIUM
    }
    
    fun toggleTodoCompleted(id: String) {
        viewModelScope.launch {
            repository.toggleTodoCompleted(id)
        }
    }
    
    fun deleteTodo(id: String) {
        viewModelScope.launch {
            repository.deleteTodo(id)
        }
    }
    
    fun syncData() {
        viewModelScope.launch {
            repository.sendPending()
        }
    }
} 