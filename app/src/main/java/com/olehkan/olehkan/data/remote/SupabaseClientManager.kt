package com.olehkan.olehkan.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClientManager {
    const val SUPABASE_URL = "https://vocuavzynbsiqwtbcehx.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZvY3Vhdnp5bmJzaXF3dGJjZWh4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTUzNjU5NjksImV4cCI6MjExMDk0MTk2OX0.q8GlacLk4a2agtrp_7wwAhCah_-CzVni0Z7SnqWxEnM"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY,
        ) {
            install(Postgrest)
            install(Storage)
            install(Auth)
        }
    }
}
