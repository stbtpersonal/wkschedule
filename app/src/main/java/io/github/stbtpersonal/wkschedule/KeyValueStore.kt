package io.github.stbtpersonal.wkschedule

import android.content.Context

class KeyValueStore(private val context: Context) {
    private val sharedPreferences = this.context.getSharedPreferences("key-value-store", Context.MODE_PRIVATE)

    var apiKey: String?
        get() = this.sharedPreferences.getString("api-key", null)
        set(value) = with(this.sharedPreferences.edit()) {
            putString("api-key", value)
            apply()
        }
}