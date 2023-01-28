package com.telotengoca.moth.printer

import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class EnforcerTest {
    @Test
    fun `test That A Non-Existing User Cannot Perform Actions`() {
        val s = "user3"
        val o = "vendor"
        val a = "delete"
        assert(!enforcer.enforce(s, o, a))
    }
    @Test
    fun testThatUserCannotWriteToVendor() {
        val s = "user2"
        val o = "vendor"
        val a = "write"
        val result = enforcer.enforce(s, o, a)

        assert(!result)
    }

    @Test
    fun testThatAdminCanWriteToVendor() {
        // given
//        val rbacModel = this::class.java.getResource("/com/telotengoca/moth/config/rbac_model.conf") ?: throw Exception("Couldn't find resource")
//        val rbacModelPath = rbacModel.toURI()
//
//        val rbacPolicy = this::class.java.getResource("/com/telotengoca/moth/config/rbac_policy.csv") ?: throw Exception("Couldn't find resource")
//        val rbacPolicyPath = rbacPolicy.toURI()
//
//        val enforcer = Enforcer(rbacModelPath.path, rbacPolicyPath.path)

        // when
        val s = "user1"
        val o = "vendor"
        val a = "read"

        val checkPermission = enforcer.enforce(s, o, a)
        // then
        assert(checkPermission)
    }

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
}