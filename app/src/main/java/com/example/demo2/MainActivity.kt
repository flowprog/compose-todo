package com.example.demo2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo2.components.AddTodoDialog
import com.example.demo2.components.TodoList
import com.example.demo2.data.TodoViewModel
import com.example.demo2.data.local.AppDatabase
import com.example.demo2.ui.theme.Demo2Theme
import com.example.demo2.data.remote.LocalDateTimeAdapter
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonToken
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


enum class TodoPriority {
    HIGH, MEDIUM, LOW, COMPLETED
}


data class TodoItem(
    val id: String,
    val title: String,
    val priority: TodoPriority,
    @SerializedName("completed")
    var isCompleted: Boolean = false,
    val createdAt: LocalDateTime
)

class MainActivity : ComponentActivity() {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // 应用获取了焦点，同步数据
//            val viewModel: TodoViewModel by viewModels()
//            viewModel.syncData()
        } else {
            // 应用失去了焦点
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化数据库
        AppDatabase.getDatabase(applicationContext)
        
        setContent {
            // Add your UI content here
            Demo2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp(viewModel: TodoViewModel = viewModel()) {
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val newTodoTitle by viewModel.newTodoTitle.collectAsState()
    val newTodoPriority by viewModel.newTodoPriority.collectAsState()
    val todos by viewModel.filteredTodos.collectAsState()

    Scaffold(
        topBar = {
            TodoAppBar(
                isSearchVisible = searchQuery.isNotEmpty(),
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                selectedTab = selectedPriority,
                onTabSelected = viewModel::setSelectedPriority,
                onSearchToggle = { 
                    if (searchQuery.isEmpty()) {
                        viewModel.setSearchQuery(" ")
                    } else {
                        viewModel.setSearchQuery("")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddDialog(true) },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加待办事项"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            TodoBottomNavigation(selectedTab = selectedPriority, onTabSelected=viewModel::setSelectedPriority)
        },
    ) { innerPadding ->
        TodoList(
            todos = todos,
            onToggleCompleted = viewModel::toggleTodoCompleted,
            onDelete = viewModel::deleteTodo,
            contentPadding = innerPadding
        )
    }

    AddTodoDialog(
        isVisible = showAddDialog,
        title = newTodoTitle,
        onTitleChange = viewModel::setNewTodoTitle,
        selectedPriority = newTodoPriority,
        onPrioritySelected = viewModel::setNewTodoPriority,
        onDismiss = { viewModel.toggleAddDialog(false) },
        onConfirm = viewModel::addNewTodo
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoAppBar(
    isSearchVisible: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTab: TodoPriority,
    onTabSelected: (TodoPriority) -> Unit,
    onSearchToggle: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = {
        3
    })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Tab 1", "Tab 2", "Tab 3")
    var selectedIndex by remember { mutableStateOf(0) }

    Column {
        TopAppBar(
            title = {
                if (isSearchVisible) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("搜索待办事项") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    Text("待办", fontSize = 20.sp)
                }
            },
            actions = {
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
fun TodoBottomNavigation(
    selectedTab: TodoPriority,
    onTabSelected: (TodoPriority) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == TodoPriority.HIGH,
            onClick = { onTabSelected(TodoPriority.HIGH) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "高优先级"
                )
            },
            label = { Text("高") }
        )
        
        NavigationBarItem(
            selected = selectedTab == TodoPriority.MEDIUM,
            onClick = { onTabSelected(TodoPriority.MEDIUM) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "中优先级"
                )
            },
            label = { Text("中") }
        )
        
        Spacer(modifier = Modifier.width(10.dp))
        
        NavigationBarItem(
            selected = selectedTab == TodoPriority.LOW,
            onClick = { onTabSelected(TodoPriority.LOW) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "低优先级"
                )
            },
            label = { Text("低") }
        )
        
        NavigationBarItem(
            selected = selectedTab == TodoPriority.COMPLETED,
            onClick = { onTabSelected(TodoPriority.COMPLETED) },
            icon = { 
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = "已完成"
                )
            },
            label = { Text("已完成") }
        )
    }
}