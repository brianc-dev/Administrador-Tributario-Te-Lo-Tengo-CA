package com.telotengoca.moth.model

import java.sql.Connection

/**
 * Represents a user profile.
 */
data class Profile(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val address: String?,
    val telephone: String?
)

interface MothProfileManager {
    fun createProfile(user: User)
}

/**
 * Class to manage user profiles. This must be connected to same database as UserManager.
 */
class MothProfileManagerImpl(private val database: MothDatabase): MothProfileManager {

    init {
        createProfileTable(database.connectDatabase())
    }
    override fun createProfile(user: User) {
        TODO("Not yet implemented")
    }

    private fun createProfileTable(connection: Connection) {
        connection.use {
            val stm = it.createStatement()
            stm.execute("CREATE TABLE IF NOT EXISTS profile(user_id VARCHAR(7) PRIMARY KEY NOT NULL, firstName VARCHAR(30), lastName VARCHAR(30) NOT NULL, email TEXT, address VARCHAR(150), telephone VARCHAR(15), FOREIGN KEY (user_id) REFERENCES user(id))")
        }
    }
}