package io.github.stbtpersonal.wkschedule

import android.content.Context
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*
import kotlin.collections.HashMap

class WaniKaniInterface(private val context: Context) {
    private val baseUrl = "https://api.wanikani.com/v2"
    private val requestQueue = Volley.newRequestQueue(this.context)

    private fun buildRequest(
        apiKey: String,
        endpoint: String,
        params: Map<String, String>?,
        failureListener: (VolleyError) -> Unit,
        successListener: (String) -> Unit
    ): StringRequest {
        val paramStrings = params?.map { entry -> "${entry.key}=${entry.value}" }
        val paramsString = paramStrings?.joinToString(separator = "&", prefix = "", postfix = "")
        val url = "$baseUrl/$endpoint${if (paramsString == null) "" else "?$paramsString"}"

        return object : StringRequest(
            Method.GET,
            url,
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
        failureListener: (VolleyError) -> Unit,
        successListener: (String) -> Unit
    ) {
        val request = this.buildRequest(apiKey, "user", null, failureListener, successListener)
        this.requestQueue.add(request)
    }

    fun getAssignments(
        apiKey: String,
        availableAfter: Date,
        availableBefore: Date,
        failureListener: (VolleyError) -> Unit,
        successListener: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["burned"] = "false"
        params["available_after"] = DateUtils.toIso8601(availableAfter)
        params["available_before"] = DateUtils.toIso8601(availableBefore)

        val request = this.buildRequest(apiKey, "assignments", params, failureListener, successListener)
        this.requestQueue.add(request)
    }

    fun getLevelAssignments(
        apiKey: String,
        level: String,
        failureListener: (VolleyError) -> Unit,
        successListener: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["subject_types"] = "radical,kanji"
        params["levels"] = level

        val request = this.buildRequest(apiKey, "assignments", params, failureListener, successListener)
        this.requestQueue.add(request)
    }

    fun getSubjects(
        apiKey: String,
        level: String,
        failureListener: (VolleyError) -> Unit,
        successListener: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["types"] = "radical,kanji"
        params["levels"] = level

        val request = this.buildRequest(apiKey, "subjects", params, failureListener, successListener)
        this.requestQueue.add(request)
    }
}