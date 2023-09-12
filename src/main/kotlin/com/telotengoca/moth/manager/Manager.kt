package com.telotengoca.moth.manager

import com.telotengoca.moth.logger.MothLoggerFactory
import com.telotengoca.moth.model.ProfileManagerImpl
import com.telotengoca.moth.model.User
import com.telotengoca.moth.model.UserManager
import com.telotengoca.moth.model.UserManagerImpl
import com.telotengoca.moth.utils.PolicyEnforcerUtil

interface Manager {
}

class ManagerImpl private constructor(val userManager: UserManager): Manager {
    companion object {
        private val logger = MothLoggerFactory.getLogger(ManagerImpl::class.java)

        private lateinit var instance: ManagerImpl

        fun getInstance(): ManagerImpl {
            if (!this::instance.isInitialized) {
                val enforcer = PolicyEnforcerUtil.getEnforcer()
                val pm = ProfileManagerImpl()
                val um = UserManagerImpl(enforcer, pm)
                instance = ManagerImpl(um)
            }
            return instance
        }
    }

    val currentUser: User?
        get() = userManager.currentUser
}