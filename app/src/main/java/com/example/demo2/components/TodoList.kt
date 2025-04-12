package com.example.demo2.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.demo2.TodoItem
import com.example.demo2.TodoPriority
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun TodoList(
    todos: List<TodoItem>,
    onToggleCompleted: (String) -> Unit,
    onDelete: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    if (todos.isEmpty()) {
        // 显示空列表提示
        Text(
            text = "没有待办事项",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            contentPadding = contentPadding
        ) {
            itemsIndexed(todos) { index, todoItem ->
                TodoItemView(
                    todoItem = todoItem,
                    onToggleCompleted = onToggleCompleted,
                    onDelete = onDelete,
                    backgroundColor = Color.Gray,
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
