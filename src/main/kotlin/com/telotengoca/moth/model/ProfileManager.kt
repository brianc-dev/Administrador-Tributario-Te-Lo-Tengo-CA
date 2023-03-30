package com.telotengoca.moth.model

import com.telotengoca.moth.logger.MothLoggerFactory
import java.sql.SQLIntegrityConstraintViolationException

/**
 * Represents a user profile.
 */
data class Profile(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val address: String?,
    val telephone: String?
)

interface ProfileManager {
    fun createProfile(profile: Profile)
    fun getProfile(userId: String): Profile?
}

/**
 * Class to manage user profiles. This must be connected to same database as UserManager.
 */
class ProfileManagerImpl(private val database: MothDatabase): ProfileManager {

    companion object {
        private val logger = MothLoggerFactory.getLogger(ProfileManager::class.java)
    }

    init {
        createProfileTable()
    }

    override fun createProfile(profile: Profile) {
        // todo: validate data here
        database.connectDatabase().use {
            it.prepareStatement("INSERT INTO `profile` VALUES(?, ?, ?, ?, ?, ?)").use { stm ->

                stm.setString(1, profile.userId)
                stm.setString(2, profile.firstName)
                stm.setString(3, profile.lastName)
                stm.setString(4, profile.email)
                stm.setString(5, profile.address)
                stm.setString(6, profile.telephone)

                val result = stm.executeUpdate()
                if (result == 0) throw SQLIntegrityConstraintViolationException("Couldn't create new profile")
            }
        }
    }

    override fun getProfile(userId: String): Profile? {
        database.connectDatabase().use {
            // check if at least 1 profile exists
            it.prepareStatement("SELECT COUNT(*) FROM `profile` WHERE `user_id` = ?").use {
                it.setString(1, userId)
                it.executeQuery().use {
                    it.next()
                    val count = it.getInt(1)
                    if (count == 0) return null
                    check(count < 2)
                }
            }

            // retrieve profile
            it.prepareStatement("SELECT * FROM `profile` WHERE `user_id` = ?").use {
                it.setString(1, userId)
                it.executeQuery().use {
                    it.next()
                    return it.run {
                        Profile(
                            getString(1),
                            getString(2),
                            getString(3),
                            getString(4),
                            getString(5),
                            getString(6)
                        )
                    }
                }
            }
        }
    }

    /**
     * creates a table in database with columns:
     * user_id, first_name, last_name, email, address, telephone
     * user_id references `id` column in `user` table
     */
    private fun createProfileTable() {
        val tableName = "profile"
        logger.info("Checking for table '{}' existence...", tableName)
        if (MothDatabaseImpl.tableExists(tableName, database.connectDatabase())) {
            logger.info("Table '{}' found", tableName)
            return
        }

        logger.info("Creating table '{}'...", tableName)
        database.connectDatabase().use {
            it.createStatement().use { stm ->
                stm.execute("CREATE TABLE IF NOT EXISTS `profile`(`user_id` VARCHAR(7) PRIMARY KEY NOT NULL, `firstName` VARCHAR(30) NOT NULL, `lastName` VARCHAR(30), `email` TEXT, `address` VARCHAR(150), `telephone` VARCHAR(15), FOREIGN KEY (user_id) REFERENCES user(id))")
            }
        }
        logger.info("Table '{}' created", tableName)
    }
}