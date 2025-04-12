package com.example.demo2.data

import com.example.demo2.TodoItem
import com.example.demo2.TodoPriority
import com.example.demo2.data.local.TodoDao
import com.example.demo2.data.local.TodoEntity
import com.example.demo2.data.remote.NetworkModule
import com.example.demo2.data.local.PendingOperation
import com.example.demo2.data.local.PendingOperationDao
import com.example.demo2.data.local.OperationType
import com.example.demo2.data.remote.LocalDateTimeAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.sql.Date
import java.time.LocalDateTime

class TodoRepository(
    private val todoDao: TodoDao,
    private val pendingOperationDao: PendingOperationDao
) {
    private val todoApi = NetworkModule.todoApi
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val todos: Flow<List<TodoItem>> = todoDao.getAllTodos().map { entities ->
        entities.map { it.toTodoItem() }
    }

    suspend fun addTodo(title: String, priority: TodoPriority) {
        val todoItem = TodoItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            priority = priority,
            isCompleted = false,
            createdAt = LocalDateTime.now()
        )
        // 立即保存到本地
        todoDao.insertTodo(TodoEntity.fromTodoItem(todoItem))
        pendingOperationDao.insertOperation(
            PendingOperation(
                operationType = OperationType.CREATE,
                todoData = gson.toJson(todoItem),
                todoId = todoItem.id

            )
        )
        
        // 异步执行远程操作
        coroutineScope.launch {
            _isSyncing.value = true
            try {
                val remoteTodo = todoApi.createTodo(todoItem)
                todoDao.updateTodo(TodoEntity.fromTodoItem(remoteTodo))
                pendingOperationDao.deleteByTodo(remoteTodo.id)
            } catch (e: Exception) {
                println(e.toString())
            } finally {
                _isSyncing.value = false
            }
        }
    }

    suspend fun updateTodo(todoItem: TodoItem) {
        // 立即更新本地
        todoDao.updateTodo(TodoEntity.fromTodoItem(todoItem))
        pendingOperationDao.insertOperation(
            PendingOperation(
                operationType = OperationType.UPDATE,
                todoId = todoItem.id,
                todoData = gson.toJson(todoItem)
            )
        )
        // 异步执行远程操作
        coroutineScope.launch {
            _isSyncing.value = true
            try {
                todoApi.updateTodo(todoItem.id, todoItem)
                pendingOperationDao.deleteByTodo(todoItem.id)
            } catch (e: Exception) {
                println(e.toString())
            } finally {
                _isSyncing.value = false
            }
        }
    }

    suspend fun deleteTodo(id: String) {
        // 立即删除本地
        todoDao.deleteTodoById(id)
        pendingOperationDao.insertOperation(
            PendingOperation(
                operationType = OperationType.DELETE,
                todoId = id,
                todoData = ""
            )
        )
        // 异步执行远程操作
        coroutineScope.launch {
            _isSyncing.value = true
            try {
                todoApi.deleteTodo(id)
                pendingOperationDao.deleteByTodo(id)
            } catch (e: Exception) {
                println(e.toString())
            } finally {
                _isSyncing.value = false
            }
        }
    }

    suspend fun toggleTodoCompleted(id: String) {
        val todo = todoDao.getTodoById(id)
        todo?.let {
            val updatedTodo = it.toTodoItem().copy(isCompleted = !it.isCompleted)
            updateTodo(updatedTodo)
        }
    }

    fun getTodosByPriority(priority: TodoPriority, searchQuery: String = ""): Flow<List<TodoItem>> {
        return if (priority == TodoPriority.COMPLETED) {
            todoDao.getTodosByCompletion(true)
        } else {
            todoDao.getTodosByPriority(priority.name, false)
        }.map { entities ->
            entities.map { it.toTodoItem() }
                .filter { 
                    if (searchQuery.isBlank()) true
                    else it.title.contains(searchQuery, ignoreCase = true)
                }
        }
    }

    // 同步本地待处理的操作到远程
    suspend fun syncPendingOperations() {
        val pendingOperations = pendingOperationDao.getAllPendingOperations()
        for (operation in pendingOperations) {
            try {
                when (operation.operationType) {
                    OperationType.CREATE -> {
                        val todoItem = gson.fromJson(operation.todoData, TodoItem::class.java)
                        val remoteTodo = todoApi.createTodo(todoItem)
                        todoDao.updateTodo(TodoEntity.fromTodoItem(remoteTodo))
                    }
                    OperationType.UPDATE -> {
                        val todoItem = gson.fromJson(operation.todoData, TodoItem::class.java)
                        todoApi.updateTodo(todoItem.id, todoItem)
                    }
                    OperationType.DELETE -> {
                        todoApi.deleteTodo(operation.todoId)
                    }
                }
                // 操作成功后删除待处理记录
                pendingOperationDao.deleteOperation(operation.id)
            } catch (e: Exception) {
                // 如果同步失败，保留待处理记录
                println(e.toString())
                break
            }
        }
    }

    // 同步远程数据到本地
    suspend fun sendPending() {
        try {
            // 先同步待处理的操作
            syncPendingOperations()
        } catch (e: Exception) {
            // 如果同步失败，保持使用本地数据
            e.printStackTrace()
        }
    }

    // 同步远程数据到本地
    suspend fun syncWithRemote() {
        try {
            // 先同步待处理的操作
            syncPendingOperations()
            
            // 然后同步远程数据
            val remoteTodos = todoApi.getAllTodos()
            todoDao.deleteAllTodos()
            todoDao.insertTodos(remoteTodos.map { TodoEntity.fromTodoItem(it) })
        } catch (e: Exception) {
            // 如果同步失败，保持使用本地数据
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TodoRepository? = null

        fun getInstance(todoDao: TodoDao, pendingOperationDao: PendingOperationDao): TodoRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TodoRepository(todoDao, pendingOperationDao)
                INSTANCE = instance
                instance
            }
        }
    }
}