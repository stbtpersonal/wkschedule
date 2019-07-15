package io.github.stbtpersonal.wkschedule

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.VolleyError
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

class MainController(private val mainActivity: MainActivity) {
    private val waniKaniInterface = WaniKaniInterface(this.mainActivity)
    private val keyValueStore = KeyValueStore(this.mainActivity)

    private val spinner = this.mainActivity.findViewById<ProgressBar>(R.id.spinner)

    private val loginContainer = this.mainActivity.findViewById<ViewGroup>(R.id.loginContainer)
    private val loginEditText = this.mainActivity.findViewById<EditText>(R.id.loginEditText)
    private val loginButton = this.mainActivity.findViewById<Button>(R.id.loginButton)

    init {
        this.loginButton.setOnClickListener { submitApiKey() }

        this.initialize()
    }

    private fun hideAll() {
        this.spinner.visibility = View.GONE
        this.loginContainer.visibility = View.GONE
    }

    private fun initialize() {
        this.hideAll()
        this.spinner.visibility = View.VISIBLE

        val apiKey = this.keyValueStore.apiKey
        if (apiKey == null) {
            this.showLogin()
            return
        }

        this.getUser(apiKey)
    }

    private fun getUser(apiKey: String) {
        this.waniKaniInterface.getUser(
            apiKey,
            { this.fail(it) },
            { response ->
                val responseJson = JSONObject(response)
                val dataJson = responseJson.getJSONObject("data")
                val level = dataJson.getString("level")

                this.getSchedule(apiKey, level)
            })
    }

    private fun getSchedule(apiKey: String, level: String) {
        val now = Date()

        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.add(Calendar.DATE, 1)
        val tomorrow = calendar.time

        this.waniKaniInterface.getAssignments(
            apiKey,
            false,
            now,
            tomorrow,
            { this.fail(it) },
            { response ->
                val schedule = this.buildSchedule(response)
                this.updateScheduleView(schedule)

                this.showSchedule()
            })
    }

    private fun buildSchedule(getAssignmentsResponse: String): Map<String, AssignmentsHour> {
        val responseJson = JSONObject(getAssignmentsResponse)
        val dataJson = responseJson.getJSONArray("data")

        val assignmentHours = ConcurrentSkipListMap<String, AssignmentsHour>()
        for (i in 0 until dataJson.length()) {
            val assignmentJson = dataJson.getJSONObject(i)
            val assignmentDataJson = assignmentJson.getJSONObject("data")

            val availableAt = assignmentDataJson.getString("available_at")
            if (!assignmentHours.containsKey(availableAt)) {
                assignmentHours[availableAt] = AssignmentsHour()
            }
            val assignmentsHour = assignmentHours[availableAt]!!

            val subjectType = assignmentDataJson.getString("subject_type")
            val passed = assignmentDataJson.getBoolean("passed")
            when (subjectType) {
                "radical" -> {
                    assignmentsHour.radicals++
                    if (!passed) {
                        assignmentsHour.newRadicals++
                    }
                }
                "kanji" -> {
                    assignmentsHour.kanji++
                    if (!passed) {
                        assignmentsHour.newKanji++
                    }
                }
                "vocabulary" -> {
                    assignmentsHour.vocabulary++
                    if (!passed) {
                        assignmentsHour.newVocabulary++
                    }
                }
            }
        }

        return assignmentHours
    }

    private fun fail(error: VolleyError) {
        val statusCode = error.networkResponse.statusCode
        Toast.makeText(this.mainActivity, "Failed! $statusCode", Toast.LENGTH_SHORT).show()
        this.keyValueStore.apiKey = null
        this.showLogin()
    }

    private fun showLogin() {
        this.hideAll()
        this.loginContainer.visibility = View.VISIBLE
        this.loginEditText.text.clear()
    }

    private fun submitApiKey() {
        val apiKey = this.loginEditText.text.toString()
        if (apiKey.isEmpty()) {
            return
        }

        this.keyValueStore.apiKey = apiKey
        this.initialize()
    }

    private fun updateScheduleView(schedule: Map<String, AssignmentsHour>) {

    }

    private fun showSchedule() {
    }
}