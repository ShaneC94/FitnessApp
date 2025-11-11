package com.fitnessapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitnessapp.data.entities.Location

// Data Access Object for interacting with the locations table
// Provides methods to query, insert, and clear gym locations in the local Room database
@Dao
interface LocationDao {

    // Retrieves all cached gym locations from the local database
    @Query("SELECT * FROM locations")
    suspend fun getAllLocations(): List<Location>

    // Inserts a new gym location into the database
    // If a duplicate (same primary key) exists, it will replace the old record
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location)

    // Clears all gym locations from the database (used before caching new data)
    @Query("DELETE FROM locations")
    suspend fun clearAll()
}
