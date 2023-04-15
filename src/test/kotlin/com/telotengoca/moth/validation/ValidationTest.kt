package com.telotengoca.moth.validation

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.constraints.Email
import org.junit.jupiter.api.Test


class ValidationTest {

    data class User(
        @get:Email(message = "Email must be a valid") val email: String?
    )

    @Test
    fun `test that we can validate a email`() {
        // given
        val email = "username1example.com"
        val user = User(email)

        val factory = Validation.buildDefaultValidatorFactory()
        val validator: Validator = factory.validator

        // when
        val violations = validator.validate(user)

        // then
        for (violation in violations) {
            print(violation.message)
        }

        assert(violations.isNotEmpty())
    }
}