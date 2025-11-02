package com.fitnessapp.data.repositories

import com.fitnessapp.data.dao.LocationDao
import com.fitnessapp.data.entities.Location

// Handles database access for map data
class LocationRepository(private val dao: LocationDao) {

    suspend fun getAllLocations(): List<Location> = dao.getAllLocations()

    suspend fun insertAll(locations: List<Location>) {
        for (location in locations) dao.insertLocation(location)
    }
}
