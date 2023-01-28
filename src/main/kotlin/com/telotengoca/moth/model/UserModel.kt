package com.telotengoca.moth.model

/**
 * Defines user model behavior
 *
 */
interface MothUserModel {
    fun createUser(username: String, password: String)
    fun updateUserPassword(oldPassword: String, newPassword: String)
    fun getUsers(): Array<User>
    fun deleteUser()
}

class UserModel(private val mothDatabase: MothDatabase): MothUserModel {
    override fun createUser(username: String, password: String) {
        TODO("Not yet implemented")
    }

    override fun updateUserPassword(oldPassword: String, newPassword: String) {
        TODO("Not yet implemented")
    }

    override fun getUsers(): Array<User> {
        TODO("Not yet implemented")
    }

    override fun deleteUser() {
        TODO("Not yet implemented")
    }
}

