package com.telotengoca.moth.manager

import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.model.User
import com.telotengoca.moth.model.UserManager

interface Manager {
}

class ManagerImpl(val userManager: UserManager) : Manager {
    companion object {
        private val logger = MothLoggerFactory.getLogger(ManagerImpl::class.java)
    }

    val currentUser: User?
        get() = userManager.currentUser
}