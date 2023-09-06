package com.telotengoca.moth.model

import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.utils.IDUtils
import org.casbin.jcasbin.main.Enforcer
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import kotlin.system.exitProcess

/**
 * Describes a user manager object
 */
interface UserManager {
    val currentUser: User?
    fun createUser(username: String, password: String, role: String)
    fun updateRole(username: String, newRole: String)
    fun login(username: String, password: String): Boolean
    fun logout()
    fun checkPermission(id: String, resource: String, permission: String): Boolean
}

private const val SALT_LENGTH = 32
private const val HASH_LENGTH = 128
private const val PARALLELISM = 1
private const val MEMORY = 15 * 1024
private const val ITERATIONS = 10

/**
 * This user manager uses a sqlite database to save users and role.
 */
class UserManagerImpl(
    private val database: MothDatabase,
    private val policyEnforcer: Enforcer,
    private val profileManager: ProfileManager
) : UserManager, ProfileManager by profileManager {

    /**
     * Argon2 password encoder
     */
    private val hasher
        get() = Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY, ITERATIONS)


    init {
        // create tables if they don't exist
        createRoleTable(database.connectDatabase())
        createUserTable(database.connectDatabase())
        // create root user
        createRootUser()
    }

    override var currentUser: User? = null
        private set

    companion object {
        private val logger = MothLoggerFactory.getLogger(UserManager::class.java)

        /**
         * Length for any user ID. Only root has 1-character id.
         */
        private const val ID_LENGTH = 7
        private const val DEFAULT_ROLE = "user"
        /**
         * Defines the name of the object/resource to check against the permission enforcer
         */
        const val RESOURCE: String = "user"
        private const val ROOT_USERNAME = "root"
        private const val ROOT_ID = "0"
        private const val ROOT_ROLE = "admin"

    }

    /**
     * Describes permissions for this resource
     */
    enum class Permission {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        CHANGE_ROLE;

        val value: String
            get() = name.lowercase()
    }

    override fun createUser(username: String, password: String, role: String) {

        try {
            // check user is logged in
            if (currentUser == null) {
                throw NoUserLoggedInException("No user is logged in")
            }

            // check user permission
            if (!checkPermission(currentUser!!.id!!, RESOURCE, Permission.CREATE.value)) {
                throw SecurityPolicyViolation("User has no permission to create new user")
            }

            // check role is valid
            if (role !in Role.values().map { it.value }) throw IllegalArgumentException("Role is not valid")

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

            // hash password
            val hashedPassword = hasher.encode(password)

            database.connectDatabase().use {
                it.prepareStatement("INSERT INTO `user`(id, username, password, role) VALUES(?, ?, ?, ?)").use {
                    it.setString(1, id)
                    it.setString(2, username)
                    it.setString(3, hashedPassword)
                    it.setString(4, DEFAULT_ROLE)

                    it.executeUpdate().also {
                        check(it == 1)
                    }
                }
            }

            val result = policyEnforcer.addRoleForUser(id, role)
            check(result)
        } catch (e: SQLException) {
            logger.error("SQL exception occurred", e)
            logger.info("Terminating program due to error...")
            exitProcess(255)
        }

    }

    override fun updateRole(username: String, newRole: String) {
        logger.info("User [{}] tries to update role of target user [{}] to role '{}'", currentUser!!.id!!, username, newRole)
        if (!checkPermission(currentUser!!.id!!, RESOURCE, Permission.CHANGE_ROLE.value)) {
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
//                            logger.info("An attempt to login occurred but not user was found")
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
//                        user = User(userId, userUsername, userPassword, userRole, createdAt)
                    }
                }
            }

            if (hasher.matches(password, hashedPassword)) {
                //login
//                currentUser = user
//                logger.info("Login successful: User [{}]", user.id)
                return true
            }
//            logger.warn("An attempt to log in occurred but password mismatched.  User [{}]", user.id)
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

    /**
     * Logs out current user
     */
    override fun logout() {
        logger.info("User {} logged out", currentUser?.id ?: "NULL")
        currentUser = null
    }

    /**
     * Checks for permission using permission enforcer.
     * Id is used to check for permission since it won't change unlike username.
     * @param id the id of the user
     * @param resource the name of the resource to act upon. E.g: vendor, user, invoice...
     * @param permission the type of permission. E.g: read, create, update, delete...
     * @return true if the subject is allowed. false otherwise.
     */
    override fun checkPermission(id: String, resource: String, permission: String): Boolean {
        return policyEnforcer.enforce(id, resource, permission)
    }

    fun createProfile(firstName: String, lastName: String, email: String?, telephone: String?, address: String?) {
        if (currentUser == null) throw IllegalStateException("A user must be logged in to create profile")
        val profile = Profile(currentUser!!.id!!, firstName, lastName, email, address, telephone)
        profileManager.createProfile(profile)
    }

    /**
     * Creates role table for roles if not created.
     */
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

                // iterate enum and insert if not found
                Role.values().forEach { role ->
                    it.executeQuery("SELECT COUNT(*) FROM `role` WHERE `role`.`role` = '${role.value}'").use { rs ->
                        rs.next()
                        if (rs.getInt(1) == 0) {
                            val result = it.executeUpdate("INSERT INTO `role`(`role`) VALUES('${role.value}')")
                            check(result == 1)
                        }
                    }
                }
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
                it.execute("CREATE TABLE IF NOT EXISTS `user`(id VARCHAR(7) PRIMARY KEY NOT NULL, username VARCHAR(10) UNIQUE NOT NULL, password VARCHAR(128) NOT NULL, role VARCHAR(20) NOT NULL, created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), FOREIGN KEY (role) REFERENCES role(role))")
            }
        }
        logger.info("Table '{}' created", tableName)
    }

    /**
     * Creates root user. This run every time the app is started but only one root is created.
     */
    private fun createRootUser() {
        val props = Properties()
        props.load(this::class.java.getResourceAsStream("/config.properties"))

        // get root password from config file
        // we could ask the user to create this password the first time the app is booted
        // instead of relaying in a config file
        val rootPassword = props.getProperty("ROOT_PASSWORD")
        val hashedRootPassword = hasher.encode(rootPassword)

        database.connectDatabase().use {
            it.prepareStatement("SELECT COUNT(*) FROM `user` WHERE `id` = ?").use {
                it.setString(1, ROOT_ID)

                it.executeQuery().use {
                    it.next()
                    val count = it.getInt(1)
                    if (count != 0) return
                    logger.info("root has not yet been created")
                }
            }

            logger.info("Creating root user...")
            it.prepareStatement("INSERT INTO `user`(id, username, password, role) VALUES(?, ?, ?, ?)").use {
                it.setString(1, ROOT_ID)
                it.setString(2, ROOT_USERNAME)
                it.setString(3, hashedRootPassword)
                it.setString(4, ROOT_ROLE)

                check(it.executeUpdate() == 1)
            }
        }

        check(policyEnforcer.addRoleForUser(ROOT_ID, ROOT_ROLE))
        logger.info("root created")
    }
}


data class Employee constructor(
    val name: String
)