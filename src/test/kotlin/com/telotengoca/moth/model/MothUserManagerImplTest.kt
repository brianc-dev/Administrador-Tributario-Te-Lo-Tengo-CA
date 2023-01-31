package com.telotengoca.moth.model

import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

private const val DATABASE_TEST_DIR = "testDatabase/"

private const val TEST_DATABASE_FILE = "moth.sqlite"

class MothUserManagerImplTest {

    companion object {

        private const val CONNECTION_STRING = "jdbc:sqlite:$DATABASE_TEST_DIR$TEST_DATABASE_FILE"

        private lateinit var testDatabase: MothDatabase
        private lateinit var testUserManager: MothUserManager

        @BeforeAll
        @JvmStatic
        fun setupTestDatabase() {
            testDatabase = object : MothDatabase {
                override fun createDatabase() {
                    val testDirName = DATABASE_TEST_DIR
                    val testRootDir = File(testDirName)

                    val databaseFile = File(testRootDir, TEST_DATABASE_FILE)
                    if (!databaseFile.exists()) databaseFile.createNewFile()
                }

                override fun connectDatabase(): Connection {
                    return DriverManager.getConnection(CONNECTION_STRING)
                }
            }

            val model: String
            val policy: String
            kotlin.run {
                model = this::class.java.getResource("/com/telotengoca/moth/config/rbac_model.conf")?.path.toString()

                policy = this::class.java.getResource("/com/telotengoca/moth/config/rbac_policy.csv")?.path.toString()

            }

            val profileManager = MothProfileManagerImpl(testDatabase)
            testUserManager = MothUserManagerImpl(testDatabase, Enforcer(model, policy), profileManager)
        }
    }

    @Test
    fun `test that we can create users`() {
        testUserManager.createUser("username1", "Username1=")
        testDatabase.connectDatabase().use {
            it.createStatement().use {
                it.executeQuery("SELECT ")
            }
        }
    }

    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    fun `test that password hasher works`() {
        val myPassword = "my\$ecret"
        val hashedPassword: String
        val hasher: Argon2PasswordEncoder

        val timeToHash = measureTimeMillis {
            hasher = Argon2PasswordEncoder(32, 128, 1, 15 * 1024, 2)
            hashedPassword = hasher.encode(myPassword)
        }

        val result: Boolean
        val timeToCompare = measureTimeMillis {
            result = hasher.matches(myPassword, hashedPassword)
        }

        println("Time for hash: " + timeToHash)
        println("Time for compare: " + timeToCompare)

        assert(result)
    }
}