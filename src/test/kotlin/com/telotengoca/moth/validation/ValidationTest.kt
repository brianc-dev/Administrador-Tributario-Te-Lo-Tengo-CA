package com.telotengoca.moth.validation

import com.google.common.truth.Truth
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test


class ValidationTest {
    
    companion object {

        private const val SIZE_MESSAGE = "The length must be between 2 and 50 characters"
        private const val EMAIL_MESSAGE = "Email must be a valid"
        private const val NOTNULL_MESSAGE = "Cannot be null"

        private lateinit var validator: Validator
        
        @BeforeAll
        @JvmStatic
        fun `get validator`() {
            val factory = Validation.buildDefaultValidatorFactory()
            validator = factory.validator
        }
    }

    @Test
    fun `test that we can validate a email`() {
        // given
        data class User(
            @get:Email(message = EMAIL_MESSAGE) val email: String,
        )
        val email = "username1example.com"
        val user = User(email)

        // when
        val violations = validator.validate(user)

        // then
        for (violation in violations) {
            print(violation.message)
            Truth.assertThat(violation.message).matches(EMAIL_MESSAGE)
        }

        assert(violations.isNotEmpty())
        assert(violations.size < 2)
    }

    @Test
    fun `test that we can reject when name is min`() {
        // given
        data class User(
            @get:jakarta.validation.constraints.Size(min = 2, max = 50, message = SIZE_MESSAGE) val name: String,
        )
        val name = "a"
        val user = User(name)
        // when
        val violations = validator.validate(user)
        // then
        for (violation in violations) {
            print(violation.message)
            Truth.assertThat(violation.constraintDescriptor.annotation).isInstanceOf(jakarta.validation.constraints.Size::class.java)
            Truth.assertThat(violation.message).matches(SIZE_MESSAGE)
        }

        assert(violations.isNotEmpty())
        assert(violations.size < 2)
    }

    @Test
    fun `test that we can reject null values`() {
        // given
        data class User(
            @get:NotNull(message = NOTNULL_MESSAGE) val address: String?
        )
        val address = null
        val user = User(address)
        // when
        val violations = validator.validate(user)
        // then
        Truth.assertThat(violations.isNotEmpty())
        Truth.assertThat(violations.size).isEqualTo(1)
        val violation = violations.first()
        print(violation.message)
        Truth.assertThat(violation.constraintDescriptor.annotation).isInstanceOf(NotNull::class.java)
    }
}