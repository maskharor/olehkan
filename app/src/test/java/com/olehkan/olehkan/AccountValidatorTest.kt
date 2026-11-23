package com.olehkan.olehkan

import com.olehkan.olehkan.data.model.AccountValidator
import com.olehkan.olehkan.data.model.UserRole
import org.junit.Assert.*
import org.junit.Test

class AccountValidatorTest {
    @Test fun rejectsEmptyAndLongNames() {
        assertNotNull(AccountValidator.nameError("   "))
        assertNotNull(AccountValidator.nameError("a".repeat(101)))
        assertNull(AccountValidator.nameError("  Nadia Putri  "))
    }

    @Test fun validatesEmailWithoutWhitespace() {
        listOf("", "buyer", "buyer@", "a b@example.com", "buyer@example").forEach {
            assertNotNull(AccountValidator.emailError(it))
        }
        assertNull(AccountValidator.emailError(" buyer@example.com "))
    }

    @Test fun enforcesMinimumRegistrationPasswordLength() {
        assertNotNull(AccountValidator.passwordError("1234567"))
        assertNull(AccountValidator.passwordError("Password8!"))
    }

    @Test fun onlyTwoSupportedRolesExist() {
        assertEquals(setOf("ADMIN", "BUYER"), UserRole.entries.map { it.name }.toSet())
    }
}
