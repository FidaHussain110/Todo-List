package uk.ac.tees.mad.E4294395

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_history")
data class LocationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val location: String,
    val timestamp: Long
)