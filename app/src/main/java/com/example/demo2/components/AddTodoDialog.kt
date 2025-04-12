package com.example.demo2.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.demo2.TodoPriority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    isVisible: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    selectedPriority: TodoPriority,
    onPrioritySelected: (TodoPriority) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("添加新待办事项") },

            text = {
                Column {
                    val focusRequester = remember { FocusRequester() }
                    val focusManager = LocalFocusManager.current
                    // 使用 LaunchedEffect 在窗口加载时自动请求焦点
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                    // 标题输入框
                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("待办事项") },
                        modifier = Modifier.fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 优先级选择
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 高优先级
                        PriorityRadioButton(
                            title = "高",
                            priority = TodoPriority.HIGH,
                            selectedPriority = selectedPriority,
                            onPrioritySelected = onPrioritySelected
                        )

                        // 中优先级
                        PriorityRadioButton(
                            title = "中",
                            priority = TodoPriority.MEDIUM,
                            selectedPriority = selectedPriority,
                            onPrioritySelected = onPrioritySelected
                        )

                        // 低优先级
                        PriorityRadioButton(
                            title = "低",
                            priority = TodoPriority.LOW,
                            selectedPriority = selectedPriority,
                            onPrioritySelected = onPrioritySelected
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = title.isNotBlank()
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun PriorityRadioButton(
    title: String,
    priority: TodoPriority,
    selectedPriority: TodoPriority,
    onPrioritySelected: (TodoPriority) -> Unit
) {
    Row(
        modifier = Modifier
            .selectable(
                selected = (selectedPriority == priority),
                onClick = { onPrioritySelected(priority) }
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = (selectedPriority == priority),
            onClick = { onPrioritySelected(priority) }
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
} 