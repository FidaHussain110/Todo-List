package uk.ac.tees.mad.E4294395

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        setContent { ToDoApp() }
    }
}

@Composable
fun ToDoApp() {
    MaterialTheme {
        val context = LocalContext.current

        val navController = rememberNavController()
        val showPrivacyNotice = remember { mutableStateOf(true) }

        if (showPrivacyNotice.value) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismiss */ },
                title = { Text("Privacy Notice") },
                text = { Text("This app uses your location to tag tasks. Location data is stored locally and not shared. Do you agree?") },
                confirmButton = {
                    Button(onClick = { showPrivacyNotice.value = false }) {
                        Text("Agree")
                    }
                },
                dismissButton = {
                    Button(onClick = { /* Exit app or handle refusal */
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Text("Disagree")
                    }
                }
            )
        } else {
            NavHost(navController = navController, startDestination = "splash") {
                composable("splash") {
                    SplashScreen {
                        navController.navigate("task_list") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
                composable("task_list") {
                    TaskListScreen(navController)
                }
                composable("add_task") {
                    AddTaskScreen(navController)
                }
            }
        }
    }
}