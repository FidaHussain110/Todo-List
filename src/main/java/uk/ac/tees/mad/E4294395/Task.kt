package uk.ac.tees.mad.E4294395

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String? = null,
    val description: String? = null,
    val location: String? = null,
    val priority: String? = null,
    val dueDate: Long? = null,
    val categoryId: Int? = null, // References Category table
    val repeatInterval: String? = null // e.g., "Daily", "Weekly", "None"
)