package com.telotengoca.moth.model

import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

private const val DATABASE_TEST_DIR = "testDatabase/"

private const val TEST_DATABASE_FILE = "moth.sqlite"

class UserManagerImplTest {

    companion object {

        private const val CONNECTION_STRING = "jdbc:sqlite:$DATABASE_TEST_DIR$TEST_DATABASE_FILE"

        private lateinit var testDatabase: MothDatabase
        private lateinit var testUserManager: UserManager

        @BeforeAll
        @JvmStatic
        fun setupTestDatabase() {
            testDatabase = object : MothDatabase {

                init {
                    createDatabase()
                }

                private fun createDatabase() {
                    val testDirName = DATABASE_TEST_DIR
                    val testRootDir = File(testDirName)
                    if (!testRootDir.exists()) check(testRootDir.mkdir())

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
                model = this::class.java.getResource("/com/telotengoca/moth/config/rbac_model.conf")?.toURI()!!.path
                println(model)
                policy = this::class.java.getResource("/com/telotengoca/moth/config/rbac_policy.csv")?.toURI()!!.path
                println(policy)

            }

            val profileManager = ProfileManagerImpl(testDatabase)
            testUserManager = UserManagerImpl(Enforcer(model, policy), profileManager)
        }

        @AfterAll
        @JvmStatic
        fun cleanTestDatabase() {
            testDatabase.connectDatabase().use {
                it.createStatement().use {
                    it.execute("DROP TABLE `user`")
                    it.execute("DROP TABLE `profile`")
                    it.execute("DROP TABLE `role`")
                }
            }
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


    @Test
    fun `test that we can create users as root`() {

        val props = Properties().apply { load(this@UserManagerImplTest::class.java.getResourceAsStream("/config.properties")) }
        val rootPassword = props.getProperty("ROOT_PASSWORD")

        val username = "username1"
        val password = "Username1="
        val role = "user"
        val logged = testUserManager.login("root", rootPassword)

        assertTrue(logged)

        testUserManager.createUser(username, password, role)
        testDatabase.connectDatabase().use {
            it.createStatement().use {
                it.executeQuery("SELECT * FROM `user` WHERE `username` = '$username'").use {
                    val result = it.next()
                    assert(result)
                    assert(it.getRow() == 1)
                    val savedUsername = it.getString("username")
                    assert(savedUsername.equals(username))
                }
            }
        }

        testUserManager.logout()
    }

    @Test
    fun `test that we can create root user` () {

        val rootId = "0"
        val rootUsername = "root"
        val rootRole = "admin"


        testDatabase.connectDatabase().use {
            it.prepareStatement("SELECT COUNT(*) FROM `user` WHERE `id` = ?").use {
                it.setString(1, rootId)

                it.executeQuery().use {
                    it.next()

                    val count = it.getInt(1)
                    assertEquals(1, count)
                }
            }

            it.prepareStatement("SELECT * FROM `user` WHERE `id` = ?").use {
                it.setString(1, rootId)

                it.executeQuery().use {
                    it.next()

                    assertEquals(rootId, it.getString(1))
                    assertEquals(rootUsername, it.getString(2))
                    assertEquals(rootRole, it.getString(4))
                }
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

    private fun createRootUser() {
        val prop = Properties()
        prop.load(this::class.java.getResourceAsStream("/config.properties"))

        val rootId = "0"
        val rootUsername = "root"
        val rootPassword = prop.getProperty("ROOT_PASSWORD")
        val rootRole = "admin"

        testDatabase.connectDatabase().use {
            it.prepareStatement("SELECT COUNT(*) FROM `user` WHERE `id` = ? AND `username` = ?").use {
                it.setString(1, rootId)
                it.setString(2, rootUsername)

                it.executeQuery().use {
                    it.next()
                    val count = it.getInt(1)
                    if (count != 0) return
                }
            }

            it.prepareStatement("INSERT INTO `user`(id, username, password, role) VALUES(?, ?, ?, ?)").use {
                it.setString(1, rootId)
                it.setString(2, rootUsername)
                it.setString(3, rootPassword)
                it.setString(4, rootRole)

                it.executeUpdate()
            }
        }
    }
}