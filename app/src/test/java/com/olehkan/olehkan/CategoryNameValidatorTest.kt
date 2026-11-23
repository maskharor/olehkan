package com.olehkan.olehkan

import com.olehkan.olehkan.data.model.CategoryNameValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryNameValidatorTest {
    @Test fun normalizesWhitespace() {
        assertEquals("Kue Kering", CategoryNameValidator.normalize("  Kue   Kering\n"))
    }

    @Test fun rejectsBlankName() {
        assertNotNull(CategoryNameValidator.error("   "))
    }

    @Test fun rejectsDuplicateIgnoringCaseAndWhitespace() {
        assertNotNull(CategoryNameValidator.error(" kue  kering ", listOf("Kue Kering")))
    }

    @Test fun reservesAllFilterName() {
        assertNotNull(CategoryNameValidator.error(" semua "))
    }

    @Test fun enforcesNameLength() {
        assertNull(CategoryNameValidator.error("a".repeat(40)))
        assertNotNull(CategoryNameValidator.error("a".repeat(41)))
    }

    @Test fun acceptsNewName() {
        assertNull(CategoryNameValidator.error("Minuman", listOf("Makanan")))
    }
}
