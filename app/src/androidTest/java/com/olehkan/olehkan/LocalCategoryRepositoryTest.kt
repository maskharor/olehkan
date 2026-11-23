package com.olehkan.olehkan

import android.content.Context
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.repository.DummyProductRepository
import com.olehkan.olehkan.data.repository.LocalCategoryRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalCategoryRepositoryTest {
    private lateinit var preferences: SharedPreferences
    private val catalog = object : ProductRepository {
        override suspend fun getProducts(): List<Product> = DummyProductRepository.dummyProducts
        override suspend fun getProductById(id: Int): Product? = getProducts().find { it.id == id }
    }

    @Before fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        preferences = context.getSharedPreferences("category_test_${UUID.randomUUID()}", Context.MODE_PRIVATE)
    }

    @After fun tearDown() {
        preferences.edit().clear().commit()
    }

    @Test fun crudPersistsAcrossRepositoryRecreation() = runBlocking {
        val first = LocalCategoryRepository(preferences, catalog)
        first.refresh()
        val id = first.save(null, "  Minuman   Segar ")
        val second = LocalCategoryRepository(preferences, catalog)
        second.refresh()
        assertEquals("Minuman Segar", second.categories.value.single { it.id == id }.name)
        second.save(id, "Minuman Tradisional")
        val third = LocalCategoryRepository(preferences, catalog)
        third.refresh()
        assertEquals("Minuman Tradisional", third.categories.value.single { it.id == id }.name)
        third.delete(id)
        val fourth = LocalCategoryRepository(preferences, catalog)
        fourth.refresh()
        assertFalse(fourth.categories.value.any { it.id == id })
    }

    @Test fun protectsUsedCategoriesFromRenameAndDelete() = runBlocking {
        val repository = LocalCategoryRepository(preferences, catalog)
        repository.refresh()
        val used = repository.categories.value.first { it.name == "Makanan" }
        assertEquals(2, used.productCount)
        expectRejected { repository.save(used.id, "Nama Baru") }
        expectRejected { repository.delete(used.id) }
        repository.refresh()
        assertEquals(used, repository.categories.value.single { it.id == used.id })
    }

    @Test fun rejectsDuplicateAndReservedNamesAtRepositoryBoundary() = runBlocking {
        val repository = LocalCategoryRepository(preferences, catalog)
        repository.refresh()
        repository.save(null, "Kue Kering")
        expectRejected { repository.save(null, " kue   kering ") }
        expectRejected { repository.save(null, "Semua") }
        assertEquals(1, repository.categories.value.count { it.name == "Kue Kering" })
    }

    @Test fun missingIdsDoNotCreateOrDeleteOtherCategories() = runBlocking {
        val repository = LocalCategoryRepository(preferences, catalog)
        repository.refresh()
        val original = repository.categories.value
        expectRejected { repository.save("missing", "Kategori Baru") }
        expectRejected { repository.delete("missing") }
        assertEquals(original, repository.categories.value)
    }

    @Test fun emptyCategoryCollectionStaysEmptyAfterReload() = runBlocking {
        val emptyCatalog = object : ProductRepository {
            override suspend fun getProducts(): List<Product> = emptyList()
            override suspend fun getProductById(id: Int): Product? = null
        }
        val first = LocalCategoryRepository(preferences, emptyCatalog)
        first.refresh()
        val id = first.save(null, "Sementara")
        first.delete(id)
        val second = LocalCategoryRepository(preferences, emptyCatalog)
        second.refresh()
        assertTrue(second.categories.value.isEmpty())
    }

    @Test fun corruptStorageIsReportedWithoutOverwritingIt() = runBlocking {
        preferences.edit().putString("categories_v1", "invalid-json").commit()
        val repository = LocalCategoryRepository(preferences, catalog)
        try {
            repository.refresh()
            fail("Data rusak seharusnya menghasilkan error")
        } catch (_: java.io.IOException) {
            assertEquals("invalid-json", preferences.getString("categories_v1", null))
        }
    }

    private suspend fun expectRejected(action: suspend () -> Any) {
        try {
            action()
            fail("Operasi seharusnya ditolak")
        } catch (_: IllegalArgumentException) {
            // Penolakan aturan data yang diharapkan.
        }
    }
}
