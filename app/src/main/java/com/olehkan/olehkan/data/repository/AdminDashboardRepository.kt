package com.olehkan.olehkan.data.repository

import org.json.JSONObject

data class AdminOverview(val categoryCount: Long, val buyerCount: Long)

/** Ringkasan tetap dihitung backend; tidak menggantikan kegagalan dengan data dummy. */
class AdminDashboardRepository(
    private val requestOverview: suspend () -> String,
) {
    constructor(auth: SupabaseAuthRepository) : this({
        auth.authorized("POST", "/rest/v1/rpc/admin_overview")
    })

    suspend fun loadOverview(): AdminOverview {
        val json = JSONObject(requestOverview())
        fun count(key: String): Long {
            val value = json.opt(key)?.toString()?.toLongOrNull()
            check(value != null && value >= 0) { "Format ringkasan toko tidak valid." }
            return value
        }
        return AdminOverview(count("category_count"), count("buyer_count"))
    }
}
