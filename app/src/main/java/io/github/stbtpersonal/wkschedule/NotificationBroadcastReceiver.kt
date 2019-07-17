package io.github.stbtpersonal.wkschedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val bootCompleteAction = "android.intent.action.BOOT_COMPLETED"

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            this.bootCompleteAction -> {
                NotificationScheduler.scheduleNotifications(context)
                NotificationScheduler.notifyIfRequired(context)
            }
            NotificationScheduler.hourPassedAction -> {
                NotificationScheduler.notifyIfRequired(context)
            }
        }
    }
}