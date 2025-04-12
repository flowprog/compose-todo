package com.example.demo2.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.demo2.TodoItem
import com.example.demo2.TodoPriority
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItemView(
    todoItem: TodoItem,
    onToggleCompleted: (String) -> Unit,
    onDelete: (String) -> Unit,
    backgroundColor: Color
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    var showSnackbar by remember { mutableStateOf(false) }
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            // 显示 Snackbar 并在 3 秒后自动隐藏
            delay(3000L)
            showSnackbar = false
        }
    }
    // 根据 showSnackbar 状态显示或隐藏 Snackbar
    if (showSnackbar) {
        Snackbar(
            modifier = Modifier
                .padding(16.dp),

            content = {
                Text("已复制到剪贴板")
            }
        )
    }

    Row(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .padding(10.dp)
            .combinedClickable(
                onClick = {},
                onDoubleClick = {
                    // 复制文本到剪贴板
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("label", todoItem.title))
                    showSnackbar = true
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // 待办事项完成状态圆圈
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable { onToggleCompleted(todoItem.id) },
                contentAlignment = Alignment.Center
            ) {
                if (todoItem.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "已完成",
                        tint = getPriorityColor(todoItem.priority)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "未完成",
                        tint = getPriorityColor(todoItem.priority)
                    )
                }
            }
            
            // 待办事项标题
            Text(
                text = todoItem.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp),
                textDecoration = if (todoItem.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = Color.Black
            )
        }
        
        // 删除按钮
//        IconButton(onClick = { onDelete(todoItem.id) }) {
//            Icon(
//                imageVector = Icons.Default.Close,
//                contentDescription = "删除",
//                tint = Color.Gray
//            )
//        }
    }
}

@Composable
fun getPriorityColor(priority: TodoPriority): Color {
    return when (priority) {
        TodoPriority.HIGH -> Color.Red
        TodoPriority.MEDIUM -> Color(0xFFFFA500) // 橙色
        TodoPriority.LOW -> Color.Green
        TodoPriority.COMPLETED -> Color.Gray
    }
}
