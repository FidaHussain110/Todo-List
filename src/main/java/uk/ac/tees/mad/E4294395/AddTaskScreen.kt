package uk.ac.tees.mad.E4294395

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(context.applicationContext as Application)
    )
    val datePickerState = rememberDatePickerState()
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var priority by remember { mutableStateOf("Medium") }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    val priorityOptions = listOf("High", "Medium", "Low")
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    val categories by viewModel.categories.collectAsState(initial = emptyList())
    var repeatInterval by remember { mutableStateOf("None") }
    var showRepeatDropdown by remember { mutableStateOf(false) }
    val repeatOptions = listOf("None", "Daily", "Weekly", "Monthly")
    val uniqueLocations by viewModel.uniqueLocations.collectAsState(initial = emptyList())
    var showLocationDropdown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showLocationDialog by remember { mutableStateOf(false) }
    var shareViaEmail by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var shareStatus by remember { mutableStateOf<String?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val surfaceColor = if (isDarkTheme) Color.DarkGray else Color.White

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).firstOrNull()
    val shakeDetected = remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchCurrentLocation(context, cameraPositionState, showLocationDialog = { showLocationDialog = true }) { latLng ->
                location = "${latLng.latitude}, ${latLng.longitude}"
                selectedLocation = latLng
            }
        } else {
            showLocationDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation(context, cameraPositionState, showLocationDialog = { showLocationDialog = true }) { latLng ->
                location = "${latLng.latitude}, ${latLng.longitude}"
                selectedLocation = latLng
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                if (acceleration > 20) {
                    title = ""
                    subject = ""
                    description = ""
                    location = null
                    selectedLocation = null
                    priority = "Medium"
                    dueDate = null
                    categoryId = null
                    repeatInterval = "None"
                    shareViaEmail = false
                    email = ""
                    shakeDetected.value = true
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    DisposableEffect(Unit) {
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Location Services Required", color = textColor) },
            text = { Text("Location services are disabled or permission denied. Please enable them to tag your current location.", color = textColor) },
            confirmButton = {
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        showLocationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color.DarkGray else MaterialTheme.colorScheme.primary,
                        contentColor = textColor
                    )
                ) {
                    Text("Enable", color = textColor)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLocationDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color.DarkGray else MaterialTheme.colorScheme.secondary,
                        contentColor = textColor
                    )
                ) {
                    Text("Cancel", color = textColor)
                }
            },
            containerColor = surfaceColor
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Add Task",
                color = textColor,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title", color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = textColor,
                    cursorColor = textColor,
                    containerColor = surfaceColor
                )
            )
        }
        item {
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject/Topic", color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = textColor,
                    cursorColor = textColor,
                    containerColor = surfaceColor
                )
            )
        }
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = textColor) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                maxLines = 4,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                    focusedLabelColor = textColor,
                    unfocusedLabelColor = textColor,
                    cursorColor = textColor,
                    containerColor = surfaceColor
                )
            )
        }
        item {
            ElevatedButton(
                onClick = { showPriorityDropdown = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Priority: $priority",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            DropdownMenu(
                expanded = showPriorityDropdown,
                onDismissRequest = { showPriorityDropdown = false },
                modifier = Modifier.background(surfaceColor)
            ) {
                priorityOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        },
                        onClick = {
                            priority = option
                            showPriorityDropdown = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.background(surfaceColor)
                    )
                }
            }
        }
        item {
            ElevatedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = dueDate?.let { SimpleDateFormat("MMM dd, yyyy").format(Date(it)) }
                        ?: "Set Due Date",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            dueDate = datePickerState.selectedDateMillis
                            showDatePicker = false
                        }) {
                            Text("Confirm", color = textColor)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = textColor)
                        }
                    },
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    colors = DatePickerDefaults.colors(
                        containerColor = surfaceColor,
                        titleContentColor = textColor,
                        headlineContentColor = textColor,
                        weekdayContentColor = textColor,
                        subheadContentColor = textColor,
                        yearContentColor = textColor,
                        currentYearContentColor = textColor,
                        selectedYearContainerColor = if (isDarkTheme) Color.DarkGray else MaterialTheme.colorScheme.primary,
                        selectedYearContentColor = textColor,
                        dayContentColor = textColor,
                        selectedDayContainerColor = if (isDarkTheme) Color.DarkGray else MaterialTheme.colorScheme.primary,
                        selectedDayContentColor = textColor
                    )
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
        item {
            ElevatedButton(
                onClick = { showCategoryDropdown = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = categories.find { it.id == categoryId }?.name ?: "Select Category",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            DropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false },
                modifier = Modifier.background(surfaceColor)
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        },
                        onClick = {
                            categoryId = category.id
                            showCategoryDropdown = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.background(surfaceColor)
                    )
                }
            }
        }
        item {
            ElevatedButton(
                onClick = { showRepeatDropdown = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Repeat: $repeatInterval",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            DropdownMenu(
                expanded = showRepeatDropdown,
                onDismissRequest = { showRepeatDropdown = false },
                modifier = Modifier.background(surfaceColor)
            ) {
                repeatOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        },
                        onClick = {
                            repeatInterval = option
                            showRepeatDropdown = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.background(surfaceColor)
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = shareViaEmail,
                    onCheckedChange = { shareViaEmail = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = textColor,
                        checkmarkColor = textColor
                    )
                )
                Text(
                    text = "Share via Email",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        item {
            if (shareViaEmail) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = textColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                        focusedLabelColor = textColor,
                        unfocusedLabelColor = textColor,
                        cursorColor = textColor,
                        containerColor = surfaceColor
                    )
                )
            }
        }
        item {
            Text(
                text = "Select Location",
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    location = "${latLng.latitude}, ${latLng.longitude}"
                }
            ) {
                selectedLocation?.let {
                    Marker(
                        state = com.google.maps.android.compose.MarkerState(position = it),
                        title = "Selected Location"
                    )
                }
            }
        }
        item {
            ElevatedButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fetchCurrentLocation(context, cameraPositionState, showLocationDialog = { showLocationDialog = true }) { latLng ->
                            location = "${latLng.latitude}, ${latLng.longitude}"
                            selectedLocation = latLng
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Tag Current Location",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        item {
            ElevatedButton(
                onClick = { showLocationDropdown = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Select from Location History",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            DropdownMenu(
                expanded = showLocationDropdown,
                onDismissRequest = { showLocationDropdown = false },
                modifier = Modifier.background(surfaceColor)
            ) {
                uniqueLocations.forEach { loc ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = loc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        },
                        onClick = {
                            location = loc
                            val coords = loc.split(",").map { it.trim().toDouble() }
                            selectedLocation = LatLng(coords[0], coords[1])
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLocation!!, 15f)
                            showLocationDropdown = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.background(surfaceColor)
                    )
                }
            }
        }
        item {
            if (shakeDetected.value) {
                Text(
                    text = "Shake detected! Input cleared.",
                    color = if (isDarkTheme) Color.Red else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            shareStatus?.let { status ->
                Text(
                    text = status,
                    color = if (status.startsWith("Task shared")) Color.Green else if (isDarkTheme) Color.Red else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        item {
            ElevatedButton(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            val task = Task(
                                title = title,
                                subject = subject.takeIf { it.isNotBlank() },
                                description = description.takeIf { it.isNotBlank() },
                                location = location,
                                priority = priority,
                                dueDate = dueDate,
                                categoryId = categoryId,
                                repeatInterval = repeatInterval.takeIf { it != "None" }
                            )
                            viewModel.addTask(task, shareViaEmail, email.takeIf { it.isNotBlank() })
                            Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show()
                            if (shareViaEmail && email.isNotBlank()) {
                                val client = OkHttpClient.Builder()
                                    .connectTimeout(30, TimeUnit.SECONDS)
                                    .writeTimeout(30, TimeUnit.SECONDS)
                                    .readTimeout(30, TimeUnit.SECONDS)
                                    .build()
                                val message = """
                                    Title: ${task.title}
                                    Subject: ${task.subject ?: "N/A"}
                                    Description: ${task.description ?: "N/A"}
                                    Priority: ${task.priority}
                                    Due Date: ${task.dueDate?.let { SimpleDateFormat("MMM dd, yyyy").format(Date(it)) } ?: "N/A"}
                                    Location: ${task.location ?: "N/A"}
                                    Category: ${categories.find { it.id == task.categoryId }?.name ?: "N/A"}
                                    Repeat Interval: ${task.repeatInterval ?: "N/A"}
                                """.trimIndent()
                                val formBody = FormBody.Builder()
                                    .add("email", email)
                                    .add("message", message)
                                    .add("_subject", "New Task Shared from App")
                                    .build()
                                val request = Request.Builder()
                                    .url("https://formspree.io/f/movdjozg")
                                    .header("Accept", "application/json")
                                    .post(formBody)
                                    .build()
                                try {
                                    val response = withContext(Dispatchers.IO) {
                                        client.newCall(request).execute()
                                    }
                                    shareStatus = if (response.isSuccessful) {
                                        Toast.makeText(context, "Task shared successfully!", Toast.LENGTH_SHORT).show()
                                        "Task shared successfully!"
                                    } else {
                                        Toast.makeText(context, "Failed to share task", Toast.LENGTH_SHORT).show()
                                        "Failed to share task: ${response.message}"
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error sharing task: Check network", Toast.LENGTH_SHORT).show()
                                    shareStatus = "Error sharing task: Check network connection"
                                }
                            }
                            location?.let { loc ->
                                val coords = loc.split(",").map { it.trim().toDouble() }
                                val latLng = LatLng(coords[0], coords[1])
                                addGeofence(context, task.id, latLng, task.title)
                            }
                            if (!shareViaEmail || shareStatus?.startsWith("Task shared") == true) {
                                navController.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
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
                    contentColor = textColor
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "Save Task",
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

fun fetchCurrentLocation(
    context: Context,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    showLocationDialog: () -> Unit,
    onLocationFetched: (LatLng) -> Unit
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        showLocationDialog()
        return
    }

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val latLng = LatLng(loc.latitude, loc.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                onLocationFetched(latLng)
            }
        }.addOnFailureListener {
            showLocationDialog()
        }
    } else {
        showLocationDialog()
    }
}

fun addGeofence(context: Context, taskId: Int, latLng: LatLng, taskTitle: String) {
    val geofencingClient = LocationServices.getGeofencingClient(context)
    val geofence = Geofence.Builder()
        .setRequestId("task_$taskId")
        .setCircularRegion(latLng.latitude, latLng.longitude, 100f)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()

    val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
        putExtra("task_title", taskTitle)
    }
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        taskId,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
    )

    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                // Geofence added
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
}