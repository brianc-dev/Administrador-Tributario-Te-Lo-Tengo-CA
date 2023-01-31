package com.telotengoca.moth.model

import com.telotengoca.moth.utils.HexUtils
import com.telotengoca.moth.utils.IDUtils
import org.casbin.jcasbin.main.Enforcer
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.lang.IllegalStateException
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.sql.Connection
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class User(
    val id: String,
    val username: String,
    private val password: String,
    val role: String,
    val createAt: String
)

/**
 * Describes a user manager object
 */
interface MothUserManager {
    fun createUser(username: String, password: String)
    fun login(username: String, password: String): Boolean
    fun logout()
}

/**
 * This user manager uses a sqlite database to save users and role.
 */
class MothUserManagerImpl(
    private val database: MothDatabase,
    private val policyEnforcer: Enforcer,
    private val profileManager: MothProfileManager
) : MothUserManager {

    init {
        // create tables if they don't exist
        createUserTable(database.connectDatabase())
    }

    var currentUser: User? = null
        private set

    companion object {
        private const val ID_LENGTH = 7
        private const val DEFAULT_ROLE = "user"
    }

    class UsernameExistsException(username: String): Exception("$username already exists")


    override fun createUser(username: String, password: String) {

        // check that username doesn't exist
        database.connectDatabase().use {
            it.prepareStatement("SELECT COUNT(*) FROM `user` WHERE `username` = ?").use {
                it.setString(1, username)
                it.executeQuery().use {
                    it.last()
                    val count = it.getInt(1)
                    if (count > 0) throw UsernameExistsException(username)
                }
            }
        }

        // make sure id generated is truly unique
        var id: String
        val connection = database.connectDatabase()
        connection.prepareStatement("SELECT COUNT(*) FROM `user` WHERE `id` = ?").use {
            while (true) {
                id = IDUtils.generateRandomId(ID_LENGTH)
                it.setString(1, id)
                val result: Int
                it.executeQuery().use {
                    it.last()
                    result = it.getInt(1)
                }
                if (result == 0) break
            }
        }

        database.connectDatabase().use {
            it.prepareStatement("INSERT INTO `user` VALUES(?, ?, ?, ?)").use {
                it.setString(1, id)
                it.setString(2, username)
                it.setString(3, password)
                it.setString(4, DEFAULT_ROLE)

                it.executeUpdate().also {
                    check(it == 1)
                }


            }
        }

    }

    /**
     * Logs in a new user.
     * @param [username] the username
     * @param [password] the password
     * @return true if login was successful. false if user or password is wrong
     * @throws SecurityException if more than one user exists with the same username
     */
    override fun login(username: String, password: String): Boolean {
        val user: User
        val hashedPassword: String
        database.connectDatabase().use {
            val preparedStatement = it.prepareStatement("SELECT * FROM `user` WHERE `user`.`username` = ?")
            preparedStatement.setString(1, username)
            val resultSet = preparedStatement.executeQuery()!!

            if (resultSet.fetchSize == 0) return false

            // hope we never use this line, just for defensive purpose
            if (resultSet.fetchSize > 1) throw SecurityException("There are more than one user with same username")

            resultSet.last()
            val userId = resultSet.getString(1)
            val userUsername = resultSet.getString(2)
            val userPassword = resultSet.getString(3)
            val userRole = resultSet.getString(4)
            val createdAt = resultSet.getString(5)

            hashedPassword = userPassword
            user = User(userId, userUsername, userPassword, userRole, createdAt)
        }
        val hasher = Argon2PasswordEncoder(32, 128, 1, 15 * 1024, 2)

        if (hasher.matches(password, hashedPassword)) {
            //login
            currentUser = user
            return true
        }
        return false
    }

    override fun logout() {
        currentUser = null
    }

    fun createProfile(firstName: String, lastName: String, email: String?, telephone: String?, address: String?) {
        if (currentUser == null) throw IllegalStateException("User must be logged in to create profile")
        val profile = Profile(currentUser!!.id, firstName, lastName, email, address, telephone)
        profileManager.createProfile(profile)
    }

    private fun createRoleTable(connection: Connection) {
        connection.use {
            it.createStatement().use {
                val result = it.execute("CREATE TABLE IF NOT EXISTS `role`(role VARCHAR(20) PRIMARY KEY NOT NULL)")
//                it.execute("INSERT INTO `role` VALUES(")")
            }
        }
    }

    /**
     * Creates user table if not created.
     */
    private fun createUserTable(connection: Connection) {
        connection.use {
            it.createStatement().use {
                it.execute("CREATE TABLE IF NOT EXISTS `user`(id VARCHAR(7) PRIMARY KEY NOT NULL, username VARCHAR(10) UNIQUE NOT NULL, password VARCHAR(128) NOT NULL, role VARCHAR(32) NOT NULL, created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), FOREIGN KEY (role) REFERENCES role(role))")
            }
        }
    }

    /**
     * Hash a string using PBKDF2 algorithm.
     * @return a string of 32 characters of length
     */
    private fun hashPassword(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val spec: KeySpec = PBEKeySpec(IDUtils.generateRandomId(8).toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        return HexUtils.bytesToHex(hash)
    }
}