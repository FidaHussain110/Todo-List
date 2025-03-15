package uk.ac.tees.mad.E4294395

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Insert
    suspend fun insertApiTask(apiTask: ApiTask)

    @Query("SELECT * FROM api_tasks")
    fun getAllApiTasks(): Flow<List<ApiTask>>

    @Query("SELECT DISTINCT location FROM tasks WHERE location IS NOT NULL")
    fun getUniqueLocations(): Flow<List<String>>

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert
    suspend fun insertLocationHistory(location: LocationHistory)

    @Query("SELECT * FROM location_history")
    fun getAllLocationHistory(): Flow<List<LocationHistory>>
}