package com.telotengoca.moth.model


import com.telotengoca.moth.model.Model.Companion.all
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class UserTest {

    @Test
    fun weCanSaveUser() {
        val user = User.create("Username1", "asdfgh", Role.ADMIN)

        val userList = user.all()
        assert(userList.any {  it.username == user.username })
    }

    @Test
    fun weCanGetUserById() {
        val user = User.create("Username1", "asdfgh", Role.ADMIN)

        val result = User.get(user.id!!)

        assertNotNull(result)
        checkNotNull(result)
        assert(result.username == user.username)
    }
}
