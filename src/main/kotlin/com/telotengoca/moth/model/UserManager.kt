package com.telotengoca.moth.model

import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.utils.HexUtils
import com.telotengoca.moth.utils.IDUtils
import org.casbin.jcasbin.main.Enforcer
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.sql.Connection
import java.sql.SQLException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.system.exitProcess

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
interface UserManager {
    val currentUser: User?
    fun createUser(username: String, password: String)
    fun updateRole(username: String, newRole: String)
    fun login(username: String, password: String): Boolean
    fun logout()
    fun checkPermission(user: String, resource: String, permission: String): Boolean
}

/**
 * This user manager uses a sqlite database to save users and role.
 */
class UserManagerImpl(
    private val database: MothDatabase,
    private val policyEnforcer: Enforcer,
    private val profileManager: MothProfileManager
) : UserManager {

    init {
        // create tables if they don't exist
        createUserTable(database.connectDatabase())
        createRoleTable(database.connectDatabase())
    }

    override var currentUser: User? = null
        private set

    companion object {
        private val logger = MothLoggerFactory.getLogger(UserManager::class.java)
        private const val ID_LENGTH = 7
        private const val DEFAULT_ROLE = "user"
        /**
         * Defines the name of the object/resource to check against the permission enforcer
         */
        const val RESOURCE: String = "user"

    }

    enum class Permissions {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        CHANGE_ROLE;

        val value: String
            get() = name.lowercase()
    }

    override fun createUser(username: String, password: String) {

        try {
            // check user is logged in
            if (currentUser == null) {
                throw NoUserLoggedInException("No user is logged in")
            }

            // check user permission
            if (!checkPermission(currentUser!!.username, RESOURCE, Permissions.CREATE.value)) {
                throw SecurityPolicyViolation("User has no permission to create new user")
            }

            // check that username doesn't exist
            database.connectDatabase().use {
                it.prepareStatement("SELECT COUNT(*) FROM `user` WHERE `username` = ?").use {
                    it.setString(1, username)
                    it.executeQuery().use {
                        it.next()
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
                        it.next()
                        result = it.getInt(1)
                    }
                    if (result == 0) break
                }
            }

            database.connectDatabase().use {
                it.prepareStatement("INSERT INTO `user`(id, username, password, role) VALUES(?, ?, ?, ?)").use {
                    it.setString(1, id)
                    it.setString(2, username)
                    it.setString(3, password)
                    it.setString(4, DEFAULT_ROLE)

                    it.executeUpdate().also {
                        check(it == 1)
                    }
                }
            }
        } catch (e: SQLException) {
            logger.error("SQL exception occurred", e)
            logger.info("Terminating program due to error...")
            exitProcess(255)
        }

    }

    override fun updateRole(username: String, newRole: String) {
        logger.info("User [{}] tries to update role of target user [{}] to role '{}'", currentUser!!.id, username, newRole)
        if (!checkPermission(currentUser!!.username, RESOURCE, Permissions.CHANGE_ROLE.value)) {
            throw SecurityPolicyViolation("User has no permission to change roles")
        }
        TODO("update role for user")


    }

    /**
     * Exception used to indicate that a given username already exists
     * @param username the username already existing
     */
    class UsernameExistsException(username: String) : Exception("$username already exists")

    /**
     * Exception used to indicate no user is logged in
     * @param message message for exception
     */
    class NoUserLoggedInException(message: String) : RuntimeException(message)

    /**
     * Exception used to indicate that a subject tried to access a resource, but the security policy rejected it.
     * @param message message for exception
     */
    class SecurityPolicyViolation(message: String) : RuntimeException(message)

    /**
     * Logs in a new user.
     * @param [username] the username
     * @param [password] the password
     * @return true if login was successful. false if user or password is wrong
     * @throws SecurityException if more than one user exists with the same username
     */
    override fun login(username: String, password: String): Boolean {
        try {
            val user: User
            val hashedPassword: String
            database.connectDatabase().use {

                it.prepareStatement("SELECT COUNT(*) FROM `user` WHERE `user`.`username` = ?").use {
                    it.setString(1, username)
                    it.executeQuery().use {
                        it.next()
                        val count = it.getInt(1)

                        if (count == 0){
                            logger.info("An attempt to login occurred but not user was found")
                            return false
                        }

                        // hope we never use this line, just for defensive purpose
                        if (count > 1) throw IllegalStateException("There are more than one user with same username")
                    }
                }

                it.prepareStatement("SELECT * FROM `user` WHERE `user`.`username` = ?").use {

                    it.setString(1, username)
                    it.executeQuery().use {
                        it.next()
                        val userId = it.getString(1)
                        val userUsername = it.getString(2)
                        val userPassword = it.getString(3)
                        val userRole = it.getString(4)
                        val createdAt = it.getString(5)
                        hashedPassword = userPassword
                        user = User(userId, userUsername, userPassword, userRole, createdAt)
                    }
                }
            }
            val hasher = Argon2PasswordEncoder(32, 128, 1, 15 * 1024, 2)

            if (hasher.matches(password, hashedPassword)) {
                //login
                currentUser = user
                logger.info("Login successful: User [{}]", user.id)
                return true
            }
            logger.warn("An attempt to log in occurred but password mismatched.  User [{}]", user.id)
            return false
        } catch (e: IllegalStateException) {
            logger.error("An illegal state has been detected.", e)
            logger.info("Terminating program due to illegal state...")
            exitProcess(255)
        } catch (e: SQLException) {
            logger.error("A database error occurred", e)
            logger.info("Terminating program due to error...")
            exitProcess(255)
        }
    }

    override fun logout() {
        logger.info("User {} logged out", currentUser!!.id)
        currentUser = null
    }

    override fun checkPermission(user: String, resource: String, permission: String): Boolean {
        return policyEnforcer.enforce(user, resource, permission)
    }

    fun createProfile(firstName: String, lastName: String, email: String?, telephone: String?, address: String?) {
        if (currentUser == null) throw IllegalStateException("A user must be logged in to create profile")
        val profile = Profile(currentUser!!.id, firstName, lastName, email, address, telephone)
        profileManager.createProfile(profile)
    }

    private fun createRoleTable(connection: Connection) {
        val tableName = "role"
        logger.info("Checking for table '{}' existence...", tableName)
        if (MothDatabaseImpl.tableExists("role", connection)) {
            connection.close()
            logger.info("Table '{}' found", tableName)
            return
        }

        logger.info("Creating table '{}'...", tableName)
        connection.use {
            it.createStatement().use {
                it.execute("CREATE TABLE IF NOT EXISTS `role`(role VARCHAR(20) PRIMARY KEY NOT NULL)")
                it.execute("INSERT INTO `role` VALUES('user')")
                it.execute("INSERT INTO `role` VALUES('admin')")
            }
        }
        logger.info("Table '{}' created", tableName)
    }

    /**
     * Creates user table if not created.
     */
    private fun createUserTable(connection: Connection) {
        val tableName = "user"
        logger.info("Checking for table '{}' existence...", tableName)
        if (MothDatabaseImpl.tableExists("user", connection)) {
            logger.info("Table '{}' found", tableName)
            connection.close()
            return
        }

        logger.info("Creating table '{}'...", tableName)
        connection.use {
            it.createStatement().use {
                it.execute("CREATE TABLE IF NOT EXISTS `user`(id VARCHAR(7) PRIMARY KEY NOT NULL, username VARCHAR(10) UNIQUE NOT NULL, password VARCHAR(128) NOT NULL, role VARCHAR(32) NOT NULL, created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), FOREIGN KEY (role) REFERENCES role(role))")
            }
        }
        logger.info("Table '{}' created", tableName)
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