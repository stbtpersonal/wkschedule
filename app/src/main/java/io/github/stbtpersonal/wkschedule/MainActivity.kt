package io.github.stbtpersonal.wkschedule

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap


class MainActivity : Activity() {
    private lateinit var waniKaniInterface: WaniKaniInterface
    private lateinit var keyValueStore: KeyValueStore

    private lateinit var popups: List<View>

    private val removeTimeoutScript = "var timeoutElement = document.getElementById('timeout');" +
            "if (timeoutElement) { timeoutElement.parentNode.removeChild(timeoutElement); }"

    private val scheduleRecyclerViewAdapter = ScheduleRecyclerViewAdapter()
    private val studyRecyclerViewAdapter = StudyRecyclerViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.waniKaniInterface = WaniKaniInterface(this)
        this.keyValueStore = KeyValueStore(this)

        this.setContentView(R.layout.activity_main)

        this.popups = listOf<View>(this.menuContainer, this.subjectsContainer, this.wkWebView, this.wkStatsWebView)

        this.loginButton.setOnClickListener { submitApiKey() }

        this.scheduleRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        this.scheduleRecyclerView.adapter = this.scheduleRecyclerViewAdapter

        this.studyRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        this.studyRecyclerView.adapter = this.studyRecyclerViewAdapter

        this.menuButton.setOnClickListener { this.showMenu() }
        this.menuRefreshButton.setOnClickListener { this.initialize() }
        this.menuScheduleButton.setOnClickListener { this.showSchedule() }
        this.menuStudyButton.setOnClickListener { this.showStudy() }
        this.menuWaniKaniButton.setOnClickListener { this.browseWaniKani() }
        this.menuWkStatsButton.setOnClickListener { this.browseWkStats() }

        CookieManager.getInstance().setAcceptCookie(true)

        this.initWebView(this.wkWebView)
        this.wkWebView.loadUrl("http://www.wanikani.com")

        this.initWebView(this.wkStatsWebView)
        this.wkStatsWebView.loadUrl("http://www.wkstats.com")

        NotificationScheduler.scheduleNotifications(this)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setAppCacheEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = false
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                view.loadUrl("javascript:$removeTimeoutScript")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        this.initialize()
    }

    private fun hideAll() {
        this.spinner.visibility = View.GONE
        this.loginContainer.visibility = View.GONE
        this.scheduleContainer.visibility = View.GONE
        this.subjectsContainer.visibility = View.GONE
        this.menuButton.visibility = View.GONE
        this.menuContainer.visibility = View.GONE
        this.wkWebView.visibility = View.GONE
        this.wkStatsWebView.visibility = View.GONE
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
        this.waniKaniInterface.getAssignments(
            apiKey,
            { this.fail(it) },
            { response ->
                val schedule = this.buildSchedule(response)
                this.scheduleRecyclerViewAdapter.setScheduleItems(schedule)

                this.getStudySubjects(apiKey, level)
            })
    }

    private fun buildSchedule(getAssignmentsResponse: String): Map<Date, ScheduleItem> {
        val responseJson = JSONObject(getAssignmentsResponse)
        val dataJson = responseJson.getJSONArray("data")

        val now = Date()
        val scheduleItems = ConcurrentSkipListMap<Date, ScheduleItem>()
        for (i in 0 until dataJson.length()) {
            val assignmentJson = dataJson.getJSONObject(i)
            val assignmentDataJson = assignmentJson.getJSONObject("data")

            val availableAtJson = assignmentDataJson.getString("available_at")
            var availableAt = DateUtils.fromIso8601(availableAtJson)
            if (availableAt.before(now)) {
                availableAt = DateUtils.epoch
            }
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

    private fun getStudySubjects(apiKey: String, level: String) {
        this.waniKaniInterface.getLevelAssignments(
            apiKey,
            level,
            { this.fail(it) },
            { getLevelAssignmentsResponse ->
                this.waniKaniInterface.getSubjects(
                    apiKey,
                    level,
                    { this.fail(it) },
                    { getSubjectsResponse ->
                        val assignedSubjectIds = this.extractAssignedSubjectIds(getLevelAssignmentsResponse)
                        val studySubjects = this.buildStudySubjects(getSubjectsResponse, assignedSubjectIds)
                        this.studyRecyclerViewAdapter.setStudySubjects(studySubjects)

                        this.showSchedule()
                    })
            })
    }

    private fun extractAssignedSubjectIds(getLevelAssignmentsResponse: String): Set<Int> {
        val responseJson = JSONObject(getLevelAssignmentsResponse)
        val dataJson = responseJson.getJSONArray("data")

        val assignedSubjectIds = mutableSetOf<Int>()
        for (i in 0 until dataJson.length()) {
            val assignmentJson = dataJson.getJSONObject(i)
            val assignmentDataJson = assignmentJson.getJSONObject("data")

            val isPassed = assignmentDataJson.getBoolean("passed")
            if (!isPassed) {
                val subjectId = assignmentDataJson.getInt("subject_id")
                assignedSubjectIds.add(subjectId)
            }
        }

        return assignedSubjectIds
    }

    private fun buildStudySubjects(getSubjectsResponse: String, assignedSubjectIds: Set<Int>): List<StudySubject> {
        val responseJson = JSONObject(getSubjectsResponse)
        val dataJson = responseJson.getJSONArray("data")

        val lockedRadicals = mutableListOf<StudySubject>()
        val unlockedRadicals = mutableListOf<StudySubject>()
        val lockedKanji = mutableListOf<StudySubject>()
        val unlockedKanji = mutableListOf<StudySubject>()
        for (i in 0 until dataJson.length()) {
            val assignmentJson = dataJson.getJSONObject(i)
            val assignmentDataJson = assignmentJson.getJSONObject("data")

            val type = assignmentJson.getString("object")
            val subjectId = assignmentJson.getInt("id")
            val isUnlocked = assignedSubjectIds.contains(subjectId)

            val character =
                if (!assignmentDataJson.isNull("characters")) {
                    assignmentDataJson.getString("characters")
                } else {
                    null
                }

            val characterImageUrl = if (type == "radical" && character == null) {
                var originalCharacterImageUrl: String? = null
                val characterImagesJson = assignmentDataJson.getJSONArray("character_images")
                for (j in 0 until characterImagesJson.length()) {
                    val characterImageJson = characterImagesJson.getJSONObject(j)
                    val metadataJson = characterImageJson.getJSONObject("metadata")
                    if (!metadataJson.has("style_name")) {
                        continue
                    }
                    val styleName = metadataJson.getString("style_name")
                    if (styleName == "original") {
                        originalCharacterImageUrl = characterImageJson.getString("url")
                        break
                    }
                }
                originalCharacterImageUrl
            } else {
                null
            }

            val meanings = mutableListOf<String>()
            val meaningsJson = assignmentDataJson.getJSONArray("meanings")
            for (j in 0 until meaningsJson.length()) {
                val meaningJson = meaningsJson.getJSONObject(j)
                val meaning = meaningJson.getString("meaning")
                meanings.add(meaning)
            }

            val readings = mutableListOf<String>()
            if (type == "kanji") {
                val readingsJson = assignmentDataJson.getJSONArray("readings")
                for (j in 0 until readingsJson.length()) {
                    val readingJson = readingsJson.getJSONObject(j)
                    val isPrimary = readingJson.getBoolean("primary")
                    if (isPrimary) {
                        val reading = readingJson.getString("reading")
                        readings.add(reading)
                    }
                }
            }

            val subject = StudySubject(type, isUnlocked, character, characterImageUrl, meanings, readings)

            when {
                type == "radical" && !isUnlocked -> lockedRadicals.add(subject)
                type == "radical" && isUnlocked -> unlockedRadicals.add(subject)
                type == "kanji" && !isUnlocked -> lockedKanji.add(subject)
                type == "kanji" && isUnlocked -> unlockedKanji.add(subject)
            }
        }

        val studySubjects = mutableListOf<StudySubject>()
        studySubjects.addAll(unlockedRadicals)
        studySubjects.addAll(unlockedKanji)
        studySubjects.addAll(lockedRadicals)
        studySubjects.addAll(lockedKanji)
        return studySubjects
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

    private fun showMenu() {
        this.hideAll()
        this.menuContainer.visibility = View.VISIBLE
    }

    private fun showSchedule() {
        this.hideAll()
        this.scheduleContainer.visibility = View.VISIBLE
        this.menuButton.visibility = View.VISIBLE
    }

    private fun showStudy() {
        this.hideAll()
        this.subjectsContainer.visibility = View.VISIBLE
        this.menuButton.visibility = View.VISIBLE
    }

    private fun browseWaniKani() {
        this.hideAll()
        this.wkWebView.visibility = View.VISIBLE
    }

    private fun browseWkStats() {
        this.hideAll()
        this.wkStatsWebView.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        val visiblePopup = this.popups.firstOrNull { it.visibility == View.VISIBLE }
        if (visiblePopup != null) {
            this.showSchedule()
            return
        }

        super.onBackPressed()
    }
}
