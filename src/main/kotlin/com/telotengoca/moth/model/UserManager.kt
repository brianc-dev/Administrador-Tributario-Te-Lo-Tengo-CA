package com.telotengoca.moth.model

import com.telotengoca.moth.logger.MothLoggerFactory
import org.casbin.jcasbin.main.Enforcer
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
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
    fun changePassword(oldPassword: String, newPassword: String)
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
    private val policyEnforcer: Enforcer,
    private val profileManager: ProfileManager
) : UserManager, ProfileManager by profileManager {

    /**
     * Argon2 password encoder
     */
    private val hasher
        get() = Argon2PasswordEncoder(SALT_LENGTH, HASH_LENGTH, PARALLELISM, MEMORY, ITERATIONS)

    init {
        // create root user
        createRootUser()
    }

    override var currentUser: User? = null
        private set

    companion object {
        private val logger = MothLoggerFactory.getLogger(UserManagerImpl::class.java)

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

        override fun toString(): String {
            return name.lowercase()
        }
    }

    override fun createUser(username: String, password: String, role: String) {

        try {
            // check user is logged in
            checkUserIsLoggedIn()

            // check user permission
            if (!checkPermission(currentUser!!.id!!, RESOURCE, Permission.CREATE.toString())) {
                throw SecurityPolicyViolation("User has no permission to create new user")
            }

            // check role is valid
            val mappedRole = runCatching {
                Role.valueOf(role)
            }.fold(
                onSuccess = { return@fold it },
                onFailure = { throw IllegalArgumentException("Role is not valid") }
            )

            // check that username doesn't exist
            if (User.usernameExists(username)) throw UsernameExistsException(username)

            // hash password
            val hashedPassword = hasher.encode(password)

            val newUser = User.create(username, hashedPassword, mappedRole)

            val result = policyEnforcer.addRoleForUser(newUser.id, mappedRole.value)
            check(result)
        } catch (e: SQLException) {
            logger.error("SQL exception occurred", e)
            logger.error("Terminating program due to error...")
            exitProcess(255)
        }

    }

    override fun updateRole(username: String, newRole: String) {
        checkUserIsLoggedIn()

        val targetUser = User.getUserByUsername(username) ?: throw UserNotFound("Username does not belong to any user")

        logger.info(
            "User [{}] tries to update role of target user [{}] to role '{}'",
            currentUser!!.id!!,
            targetUser.id!!,
            newRole
        )
        if (!checkPermission(currentUser!!.id!!, RESOURCE, Permission.CHANGE_ROLE.toString())) {
            throw SecurityPolicyViolation("User has no permission to change roles")
        }

        targetUser.role = Role.valueOf(newRole)
        targetUser.save()

        logger.info(
            "User [{}] successfully updated role of target user [{}] to role '{}'",
            currentUser!!.id!!,
            targetUser.id,
            newRole
        )
    }

    override fun changePassword(oldPassword: String, newPassword: String) {
        checkUserIsLoggedIn()

        if (!checkPermission(currentUser!!.id!!, RESOURCE, Permission.UPDATE.toString())) {
            throw SecurityPolicyViolation("User has no permission to update")
        }

        if (!hasher.matches(oldPassword, currentUser!!.password)) {
            throw WrongPassword("The password does not match the current user's password")
        }

        val newHashedPassword = hasher.encode(newPassword)

        val user = requireNotNull(currentUser)
        user.password = newHashedPassword

        user.save()
    }

    /**
     * Logs in a new user.
     * @param [username] the username
     * @param [password] the password
     * @return true if login was successful. false if user or password is wrong
     * @throws SecurityException if more than one user exists with the same username
     */
    override fun login(username: String, password: String): Boolean {
        try {
            val user = User.getUserByUsername(username) ?: run {
                logger.info("An attempt to login occurred but not user was found")
                return false
            }

            requireNotNull(user.id)
            if (hasher.matches(password, user.password)) {
                //login
                currentUser = user
                logger.info("Login successful: User [{}]", user.id)
                return true
            }
            logger.info("An attempt to log in occurred but password mismatched.  User [{}]", user.id)
            return false
        } catch (e: IllegalStateException) {
            logger.error("An illegal state has been detected.", e)
            logger.error("Terminating program due to illegal state...")
            exitProcess(255)
        } catch (e: SQLException) {
            logger.error("A database error occurred", e)
            logger.error("Terminating program due to error...")
            exitProcess(255)
        }
    }

    /**
     * Logs out current user
     */
    override fun logout() {
        if (currentUser != null) {
            logger.info("User {} logged out", currentUser?.id!!)
            currentUser = null
        }
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
     * Creates root user. This run every time the app is started but only one root is created.
     */
    private fun createRootUser() {

        if (User.idExists(ROOT_ID)) {
            return
        } else {
            logger.info("root has not yet been created")
        }

        logger.info("Creating root user...")

        val props = Properties()
        props.load(this::class.java.getResourceAsStream("/config.properties"))

        // get root password from config file
        // we could ask the user to create this password the first time the app is booted
        // instead of relaying in a config file
        val rootPassword = props.getProperty("ROOT_PASSWORD")
        val hashedRootPassword = hasher.encode(rootPassword)

        User.createRoot(ROOT_ID, ROOT_USERNAME, hashedRootPassword, Role.ADMIN)

        val result = policyEnforcer.addRoleForUser(ROOT_ID, ROOT_ROLE)
        check(result)
        logger.info("root created")
    }

    private fun checkUserIsLoggedIn() {
        if (currentUser == null) {
            throw NoUserLoggedInException("No user is logged in")
        }
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
     * Exception used to indicate that a user requested was not found.
     * @param message message for exception
     */
    class UserNotFound(message: String) : RuntimeException(message)

    /**
     * Exception used to indicate that password does not match.
     * @param message message for exception
     */
    class WrongPassword(message: String) : RuntimeException(message)
}