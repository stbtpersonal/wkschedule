package io.github.stbtpersonal.wkschedule

import android.content.Context
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class WaniKaniInterface(private val context: Context) {
    private val baseUrl = "https://api.wanikani.com/v2"
    private val requestQueue = Volley.newRequestQueue(this.context)

    private fun buildRequest(
        apiKey: String,
        endpoint: String,
        successListener: (String) -> Unit,
        failureListener: (VolleyError) -> Unit
    ): StringRequest {
        return object : StringRequest(
            Method.GET,
            "$baseUrl/$endpoint",
            Response.Listener<String>(successListener),
            Response.ErrorListener(failureListener)
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Wanikani-Revision"] = "20170710"
                headers["Authorization"] = "Bearer $apiKey"
                return headers
            }
        }
    }

    fun getUser(
        apiKey: String,
        successListener: (String) -> Unit,
        failureListener: (VolleyError) -> Unit
    ) {
        val request = this.buildRequest(apiKey, "user", successListener, failureListener)
        this.requestQueue.add(request)
    }
}