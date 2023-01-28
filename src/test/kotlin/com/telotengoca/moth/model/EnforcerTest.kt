package com.telotengoca.moth.model

import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class EnforcerTest {

    companion object {

        private lateinit var enforcer: Enforcer
        @JvmStatic
        @BeforeAll
        fun setupEnforce(): Unit {
            val rbacModel =
                EnforcerTest::class.java.getResource("/com/telotengoca/moth/config/rbac_model.conf") ?: throw Exception(
                    "Couldn't find resource"
                )
            val rbacModelUri = rbacModel.toURI()

            val rbacPolicy =
                EnforcerTest::class.java.getResource("/com/telotengoca/moth/config/rbac_policy.csv") ?: throw Exception(
                    "Couldn't find resource"
                )
            val rbacPolicyUri = rbacPolicy.toURI()
            enforcer = Enforcer(rbacModelUri.path, rbacPolicyUri.path)
        }
    }

    @Test
    fun `test That A Non-Existing User Cannot Perform Actions`() {
        val s = "user3"
        val o = "vendor"
        var a = "delete"
        assert(!enforcer.enforce(s, o, a))
        a = "read"
        assert(!enforcer.enforce(s, o, a))
        a = "write"
        assert(!enforcer.enforce(s, o, a))
        a = "update"
        assert(!enforcer.enforce(s, o, a))
    }

    @Test
    fun testThatUserCannotWriteToVendor() {
        // user2 is user
        // when
        val s = "user2"
        val o = "vendor"
        val a = "write"
        val result = enforcer.enforce(s, o, a)
        // then
        assert(!result)
    }

    @Test
    fun testThatAdminCanWriteToVendor() {
        // user1 is admin
        // when
        val s = "user1"
        val o = "vendor"
        val a = "write"
        val checkPermission = enforcer.enforce(s, o, a)
        // then
        assert(checkPermission)
    }
}