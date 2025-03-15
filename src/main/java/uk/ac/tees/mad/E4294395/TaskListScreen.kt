package uk.ac.tees.mad.E4294395

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun TaskListScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(context.applicationContext as Application)
    )
    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White

    // Pre-populate categories on first load
    LaunchedEffect(Unit) {
        if (categories.isEmpty()) {
            viewModel.addCategory(Category(name = "Work"))
            viewModel.addCategory(Category(name = "Personal"))
            viewModel.addCategory(Category(name = "Other"))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Theme-based background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Task List",
                color = textColor, // White in dark theme, black in light theme
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally(),
                        exit = slideOutHorizontally()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color.DarkGray else Color.White
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = task.title,
                                    color = textColor,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                task.subject?.let {
                                    Text(
                                        text = "Subject: $it",
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                task.description?.let {
                                    Text(
                                        text = "Description: $it",
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                task.priority?.let {
                                    Text(
                                        text = "Priority: $it",
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                task.dueDate?.let {
                                    Text(
                                        text = "Due: ${
                                            SimpleDateFormat("MMM dd, yyyy").format(
                                                Date(it)
                                            )
                                        }",
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                task.categoryId?.let { id ->
                                    val category = categories.find { it.id == id }
                                    Text(
                                        text = "Category: ${category?.name ?: "None"}",
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                task.repeatInterval?.let {
                                    Text(
                                        text = "Repeat: $it",
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                task.location?.let {
                                    Text(
                                        text = "Location: $it",
                                        color = textColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                ElevatedButton(
                                    onClick = {
                                        scope.launch {
                                            viewModel.deleteTask(task)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(4.dp, RoundedCornerShape(16.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.error,
                                                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onError
                                    ),
                                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
                                ) {
                                    Text(
                                        text = "Delete",
                                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onError,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Sticky Add Task button
            ElevatedButton(
                onClick = { navController.navigate("add_task") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Add Task",
                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}