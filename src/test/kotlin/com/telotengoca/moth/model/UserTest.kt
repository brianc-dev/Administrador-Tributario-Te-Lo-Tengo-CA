package com.telotengoca.moth.model


import com.telotengoca.moth.model.Model.Companion.all
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserTest {

    @Test
    fun weCanSaveUser() {
        val user = User.create("Username1", "asdfgh", Role.ADMIN)

        val userList = User.all()
        assert(userList.any {  it.username == user.username })
    }

    @Test
    fun weCanGetUserById() {
        val user = User.create("Username2", "asdfgh", Role.ADMIN)

        val result = User.findById(user.id!!)

        assertNotNull(result)
        checkNotNull(result)
        assert(result.username == user.username)
    }

    @Test
    fun canCheckUsernameExists() {
        val username = "Username3"
        User.create(username, "asdfgh", Role.ADMIN)

        assertTrue(User.usernameExists(username))
    }

    @Test fun insertDuplicateUsernameThrows() {
        val username = "Username1"
        val password = "asddfg234"

        assertThrows<ConstraintViolationException> {
            User.create(username, password, Role.ADMIN)
        }
    }

    @Test fun canGetAllUsers() {
        val username = "Username456"
        val username2 = "Username789"
        val password = "asddfg234"

        User.create(username, password, Role.ADMIN)
        User.create(username2, password, Role.ADMIN)

        val users = User.all()

        assert(users.isNotEmpty())
        assert(users.any { it.username == username } && users.any { it.role == Role.ADMIN })
        assert(users.any { it.username == username2 } && users.any { it.role == Role.ADMIN })
    }
}
