package uk.ac.tees.mad.E4294395

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()
    val tasks: Flow<List<Task>> = taskDao.getAllTasks()
    val uniqueLocations: Flow<List<String>> = taskDao.getUniqueLocations()
    val categories: Flow<List<Category>> = taskDao.getAllCategories()
    val locationHistory: Flow<List<LocationHistory>> = taskDao.getAllLocationHistory()

    fun addTask(task: Task, shareViaEmail: Boolean = false, email: String? = null) {
        viewModelScope.launch {
            taskDao.insertTask(task)
            task.location?.let { loc ->
                taskDao.insertLocationHistory(
                    LocationHistory(
                        location = loc,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            task.dueDate?.let { dueDate ->
                NotificationHelper.scheduleTaskReminder(
                    getApplication(),
                    task,
                    dueDate
                )
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            taskDao.insertCategory(category)
        }
    }
}