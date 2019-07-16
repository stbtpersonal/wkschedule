package io.github.stbtpersonal.wkschedule

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

class MainActivity : Activity() {
    private lateinit var waniKaniInterface: WaniKaniInterface
    private lateinit var keyValueStore: KeyValueStore

    private lateinit var spinner: ProgressBar

    private lateinit var loginContainer: ViewGroup
    private lateinit var loginEditText: EditText
    private lateinit var loginButton: Button

    private lateinit var scheduleContainer: ViewGroup
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var scheduleRecyclerViewAdapter: ScheduleRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.waniKaniInterface = WaniKaniInterface(this)
        this.keyValueStore = KeyValueStore(this)

        this.setContentView(R.layout.activity_main)

        this.spinner = this.findViewById(R.id.spinner)

        this.loginContainer = this.findViewById(R.id.loginContainer)
        this.loginEditText = this.findViewById(R.id.loginEditText)
        this.loginButton = this.findViewById(R.id.loginButton)
        this.loginButton.setOnClickListener { submitApiKey() }

        this.scheduleContainer = this.findViewById(R.id.scheduleContainer)
        this.scheduleRecyclerView = this.findViewById(R.id.scheduleRecyclerView)
        this.scheduleRecyclerViewAdapter = ScheduleRecyclerViewAdapter()
        this.scheduleRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        this.scheduleRecyclerView.adapter = this.scheduleRecyclerViewAdapter

        this.initialize()
    }

    private fun hideAll() {
        this.spinner.visibility = View.GONE
        this.loginContainer.visibility = View.GONE
        this.scheduleContainer.visibility = View.GONE
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
                this.scheduleRecyclerViewAdapter.setScheduleItems(schedule)

                this.showSchedule()
            })
    }

    private fun buildSchedule(getAssignmentsResponse: String): Map<String, ScheduleItem> {
        val responseJson = JSONObject(getAssignmentsResponse)
        val dataJson = responseJson.getJSONArray("data")

        val scheduleItems = ConcurrentSkipListMap<String, ScheduleItem>()
        for (i in 0 until dataJson.length()) {
            val assignmentJson = dataJson.getJSONObject(i)
            val assignmentDataJson = assignmentJson.getJSONObject("data")

            val availableAt = assignmentDataJson.getString("available_at")
            if (!scheduleItems.containsKey(availableAt)) {
                scheduleItems[availableAt] = ScheduleItem()
            }
            val scheduleItem = scheduleItems[availableAt]!!

            val subjectType = assignmentDataJson.getString("subject_type")
            val passed = assignmentDataJson.getBoolean("passed")
            when (subjectType) {
                "radical" -> {
                    scheduleItem.radicals++
                    if (!passed) {
                        scheduleItem.newRadicals++
                    }
                }
                "kanji" -> {
                    scheduleItem.kanji++
                    if (!passed) {
                        scheduleItem.newKanji++
                    }
                }
                "vocabulary" -> {
                    scheduleItem.vocabulary++
                    if (!passed) {
                        scheduleItem.newVocabulary++
                    }
                }
            }
        }

        return scheduleItems
    }

    private fun fail(error: VolleyError) {
        val statusCode = error.networkResponse.statusCode
        Toast.makeText(this, "Failed! $statusCode", Toast.LENGTH_SHORT).show()
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

    private fun showSchedule() {
        this.hideAll()
        this.scheduleContainer.visibility = View.VISIBLE
    }
}
