package uk.ac.tees.mad.E4294395

import androidx.room.Entity
import androidx.room.PrimaryKey
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.Call

// Retrofit interface for JSONPlaceholder API
interface ApiService {
    @GET("todos")
    fun getTodos(): Call<List<Todo>>

    @POST("todos")
    fun postTodo(@Body todo: Todo): Call<Todo>
}

// Data class for JSONPlaceholder API response
data class Todo(
    val id: Int,
    val title: String,
    val completed: Boolean
)

// Data class for caching API responses in Room
@Entity(tableName = "api_tasks")
data class ApiTask(
    @PrimaryKey val id: Int,
    val title: String,
    val completed: Boolean
)