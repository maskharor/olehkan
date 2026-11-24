package com.olehkan.olehkan

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olehkan.olehkan.data.repository.SupabaseCategoryRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

/** Transport tiruan, tidak membuat data pada Supabase nyata. */
class SupabaseCategoryRepositoryTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferenceName = "remote_category_test_${UUID.randomUUID()}"
    private val preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
    private val rows = mutableListOf<JSONObject>()
    private val writes = mutableListOf<String>()
    private var relationFails = false

    private fun row(id: String, name: String, count: Int = 0, locked: Boolean = false) =
        JSONObject().put("id", id).put("name", name).put("catalog_locked", locked)
            .put("products", JSONArray().put(JSONObject().put("count", count)))

    private fun repository() = SupabaseCategoryRepository(preferences) { method, path, body, _ ->
        if (method == "GET") {
            if (relationFails && path.contains("products(count)")) throw IOException("Relation unavailable")
            JSONArray().apply { rows.forEach {
                val copy = JSONObject(it.toString())
                if (relationFails) copy.remove("products")
                put(copy)
            } }.toString()
        } else {
            writes += method
            val id = if (method == "POST") "new-id" else path.substringAfter("id=eq.").substringBefore("&")
            when (method) {
                "POST" -> rows += row(id, JSONObject(body!!).getString("name"))
                "PATCH" -> rows.first { it.getString("id") == id }.put("name", JSONObject(body!!).getString("name"))
                "DELETE" -> rows.removeAll { it.getString("id") == id }
            }
            """[{"id":"$id"}]"""
        }
    }

    @After fun cleanup() { context.deleteSharedPreferences(preferenceName) }

    @Test fun countsComeFromProductRelationInsteadOfDummyCatalog() = runBlocking {
        rows += row("a", "Makanan", count = 7)
        val repository = repository()
        repository.refresh()
        assertEquals(7, repository.categories.value.single().productCount)
        assertTrue(repository.categories.value.single().usageKnown)
        assertFalse(repository.categories.value.single().canManage)
    }

    @Test fun unavailableRelationKeepsListButBlocksDestructiveActions() = runBlocking {
        rows += row("a", "Kategori")
        relationFails = true
        val repository = repository()
        repository.refresh()
        assertEquals("Kategori", repository.categories.value.single().name)
        assertFalse(repository.categories.value.single().usageKnown)
        expectRejected { repository.delete("a") }
        assertTrue(writes.isEmpty())
    }

    @Test fun usedAndLockedCategoriesCannotBeRenamedOrDeleted() = runBlocking {
        rows += row("used", "Dipakai", 1)
        rows += row("locked", "Dilindungi", locked = true)
        val repository = repository()
        expectRejected { repository.save("used", "Baru") }
        expectRejected { repository.delete("used") }
        expectRejected { repository.save("locked", "Baru") }
        expectRejected { repository.delete("locked") }
        assertTrue(writes.isEmpty())
    }

    @Test fun productUsageIsRecheckedImmediatelyBeforeDelete() = runBlocking {
        rows += row("a", "Minuman")
        val repository = repository()
        repository.refresh()
        assertTrue(repository.categories.value.single().canManage)
        rows[0].put("products", JSONArray().put(JSONObject().put("count", 1)))
        expectRejected { repository.delete("a") }
        assertTrue(writes.isEmpty())
    }

    @Test fun crudUsesServerIdsAndUpdatesSharedCache() = runBlocking {
        val repository = repository()
        val id = repository.save(null, "  Kue   Kering  ")
        assertEquals("new-id", id)
        assertEquals("Kue Kering", repository.categories.value.single().name)
        assertFalse(repository.categories.value.single().usageKnown)
        repository.refresh()
        repository.save(id, "Kue Tradisional")
        assertEquals("Kue Tradisional", repository.categories.value.single().name)
        repository.delete(id)
        assertTrue(repository.categories.value.isEmpty())
        assertEquals(listOf("POST", "PATCH", "DELETE"), writes)
    }

    private suspend fun expectRejected(action: suspend () -> Unit) {
        try { action(); fail("Operation must be rejected") } catch (_: IllegalArgumentException) { }
    }
}
