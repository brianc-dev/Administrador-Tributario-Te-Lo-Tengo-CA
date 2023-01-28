package com.telotengoca.moth.model

import com.telotengoca.moth.utils.HexUtils
import com.telotengoca.moth.utils.IDUtils
import org.casbin.jcasbin.main.Enforcer
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.sql.Connection
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class User(val id: String, val username: String, val password: String, val role: String)

/**
 * Describes a user manager object
 */
interface MothUserManager {
    fun createUser(username: String, password: String)
}

enum class DefaultRoles(val role: String) {
    ADMIN("admin"),
    USER("user")
}

/**
 * This user manager uses a sqlite database to save users and role.
 */
class MothUserManagerImpl (private val database: MothDatabase, private val policyEnforcer: Enforcer, private val profileManager: MothProfileManager): MothUserManager {

    init {
        // create tables if they don't exist
        createUserTable(database.connectDatabase())
    }

    companion object {
        private const val ID_LENGTH = 7
        private const val DEFAULT_ROLE = "user"
    }
    override fun createUser(username: String, password: String) {
        val id = IDUtils.generateRandomId(ID_LENGTH)
        val user = User(id, username, password, DEFAULT_ROLE)
    }

    /**
     * Creates user table.
     */
    private fun createUserTable(connection: Connection) {
        connection.use {
            val stm = it.createStatement()
            stm.execute("CREATE TABLE `user`(id VARCHAR(7) PRIMARY KEY NOT NULL, username VARCHAR(10) NOT NULL, password VARCHAR(128) NOT NULL, role VARCHAR(32), created_at TEXT DEFAULT (datetime('now', 'localtime')), FOREIGN KEY (role) REFERENCES role(role))")
        }
    }

    private fun createProfileTable(connection: Connection) {
        connection.use {
            val stm = it.createStatement()
            stm.execute("")
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