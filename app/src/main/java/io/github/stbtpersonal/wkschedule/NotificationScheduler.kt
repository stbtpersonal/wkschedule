package io.github.stbtpersonal.wkschedule

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONObject
import java.util.*

object NotificationScheduler {
    val hourPassedAction = "io.github.stbtpersonal.wkschedule.intent.action.HOUR_PASSED"
    val channelId = "wkschedule"

    fun scheduleNotifications(context: Context) {
        val applicationContext = context.applicationContext

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        calendar.set(Calendar.HOUR_OF_DAY, currentHour + 1)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 30)

        val intent = Intent(applicationContext, this.javaClass)
        intent.action = this.hourPassedAction
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0)

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            1000 * 60 * 60,
            pendingIntent
        )
    }

    fun notifyIfRequired(context: Context) {
        val keyValueStore = KeyValueStore(context)
        val apiKey = keyValueStore.apiKey ?: return

        val waniKaniInterface = WaniKaniInterface(context)
        waniKaniInterface.getAssignments(
            apiKey,
            { },
            { response ->
                val responseJson = JSONObject(response)
                val dataJson = responseJson.getJSONArray("data")

                val now = Date()
                var reviewsCount = 0
                for (i in 0 until dataJson.length()) {
                    val assignmentJson = dataJson.getJSONObject(i)
                    val assignmentDataJson = assignmentJson.getJSONObject("data")

                    val availableAtJson = assignmentDataJson.getString("available_at")
                    val availableAt = DateUtils.fromIso8601(availableAtJson)
                    if (availableAt.before(now)) {
                        reviewsCount++
                    }
                }

                if (reviewsCount > 0) {
                    this.notify(context, reviewsCount)
                }
            })
    }

    private fun notify(context: Context, reviewsCount: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(this.channelId, this.channelId, importance)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val applicationContext = context.applicationContext

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val notification = NotificationCompat.Builder(applicationContext, this.channelId)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setContentTitle("WaniKani")
            .setContentText("$reviewsCount reviews are available")
            .setNumber(reviewsCount)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(0, notification)
    }
}