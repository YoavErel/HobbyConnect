package com.example.hobbyconnect.data


import androidx.room.*


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user WHERE id = :userId")
    fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM user")
    fun getAllUsers(): List<UserEntity>

}
