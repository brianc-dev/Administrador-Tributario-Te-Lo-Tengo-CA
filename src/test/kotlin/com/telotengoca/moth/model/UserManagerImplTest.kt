package com.telotengoca.moth.model

import com.telotengoca.moth.config.Config
import com.telotengoca.moth.manager.ProfileManagerImpl
import com.telotengoca.moth.manager.UserManager
import com.telotengoca.moth.manager.UserManagerImpl
import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class UserManagerImplTest {

    companion object {

        private lateinit var testUserManager: UserManager

        @BeforeAll
        @JvmStatic
        fun setupTestDatabase() {
            val model: String
            val policy: String
            kotlin.run {
                model = this::class.java.getResource("/com/telotengoca/moth/config/rbac_model.conf")?.toURI()!!.path
                println(model)
                policy = this::class.java.getResource("/com/telotengoca/moth/config/rbac_policy.csv")?.toURI()!!.path
                println(policy)
            }

            val profileManager = ProfileManagerImpl()
            testUserManager = UserManagerImpl(Enforcer(model, policy), profileManager)
        }
    }

    @Test
    fun `test that we cannot create users if not logged in`() {
        val username = "username1"
        val password = "Username1="
        val role = "user"
        assertThrowsExactly(UserManagerImpl.NoUserLoggedInException::class.java) {
            testUserManager.createUser(username, password, role)
        }
    }
}