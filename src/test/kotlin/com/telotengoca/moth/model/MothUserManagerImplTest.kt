package com.telotengoca.moth.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class MothUserManagerImplTest {
    @Test
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    fun `test that password hasher works`() {
        val myPassword = "my\$ecret"
        val hashedPassword: String
        val hasher: Argon2PasswordEncoder

        val timeToHash = measureTimeMillis {
            hasher = Argon2PasswordEncoder(32, 128, 1, 15 * 1024, 2)
            hashedPassword = hasher.encode(myPassword)
        }

        val result: Boolean
        val timeToCompare = measureTimeMillis {
            result = hasher.matches(myPassword, hashedPassword)
        }

        println("Time for hash: " + timeToHash)
        println("Time for compare: " + timeToCompare)

        assert(result)
    }
}