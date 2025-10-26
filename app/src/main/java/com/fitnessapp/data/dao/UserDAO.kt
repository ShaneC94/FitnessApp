package com.fitnessapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fitnessapp.data.entities.User

// Data Access Object for interacting with the users table
// Defines database ops related to user registration, auth, and user retrieval using Room persistence library
@Dao
interface UserDao {

    // Inserts new user. If a user with the same primary key exists, insertion fails
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User)
    // Retrieves a user record by their username - null if no matching user
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?
    // Auths a user by checking if username/password hash match a record
    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash LIMIT 1")
    suspend fun authenticate(username: String, passwordHash: String): User?
    // Retrieves user by their unique ID
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?
}
