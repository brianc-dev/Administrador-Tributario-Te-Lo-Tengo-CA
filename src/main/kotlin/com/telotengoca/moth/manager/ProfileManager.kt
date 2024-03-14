package com.telotengoca.moth.manager

import com.telotengoca.moth.logger.MothLoggerFactory

/**
 * Represents a user profile.
 */
data class Profile(
    val userId: Long,
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
class ProfileManagerImpl: ProfileManager {

    companion object {
        private val logger = MothLoggerFactory.getLogger(ProfileManagerImpl::class.java)
    }

    override fun createProfile(profile: Profile) {
        TODO()
    }

    override fun getProfile(userId: String): Profile? {
        TODO()
    }
}