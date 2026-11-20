package com.yuskil.subscriptionmanager

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private const val CHANNEL_ID = "subscription_reminders"
private const val EXTRA_DAY = "event_day"

internal fun scheduleReminder(context: Context, id: Long, fields: SubscriptionFields) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    val intent = Intent(context, SubscriptionReminderReceiver::class.java).apply {
        data = Uri.parse("subscription-reminder://entry/$id")
    }
    val flags = PendingIntent.FLAG_IMMUTABLE
    if (!fields.active || !fields.reminder) {
        PendingIntent.getBroadcast(context, 0, intent, flags or PendingIntent.FLAG_NO_CREATE)
            ?.let(alarmManager::cancel)
        return
    }

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val now = System.currentTimeMillis()
    val event = fields.eventDate(today)
    val eventTime = event?.atTime(9, 0)?.atZone(zone)?.toInstant()?.toEpochMilli()
    val date = if (eventTime != null && eventTime > now) event else fields.eventDate(today.plusDays(1))
    if (date == null) {
        PendingIntent.getBroadcast(context, 0, intent, flags or PendingIntent.FLAG_NO_CREATE)
            ?.let(alarmManager::cancel)
        return
    }
    intent.putExtra(EXTRA_DAY, date.toEpochDay())
    val pending = PendingIntent.getBroadcast(context, 0, intent, flags or PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        date.atTime(9, 0).atZone(zone).toInstant().toEpochMilli(),
        pending
    )
}

class SubscriptionReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = SubscriptionDatabase.get(context).subscriptions()
                if (intent.action in systemReminderActions) {
                    dao.observeAll().first().forEach { entry ->
                        scheduleReminder(context, entry.id, entry.fields)
                    }
                } else {
                    val id = intent.data?.lastPathSegment?.toLongOrNull() ?: return@launch
                    val day = intent.getLongExtra(EXTRA_DAY, Long.MIN_VALUE)
                    val fields = dao.find(id)?.fields ?: return@launch
                    val today = LocalDate.now()
                    if (fields.active && fields.reminder && day == today.toEpochDay() &&
                        fields.eventDate(today) == today
                    ) {
                        showReminder(context, id, fields)
                    }
                    scheduleReminder(context, id, fields)
                }
            } catch (error: Exception) {
                Log.e("SubscriptionReminder", "Unable to process reminder", error)
            } finally {
                result.finish()
            }
        }
    }
}

private val systemReminderActions = setOf(
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_MY_PACKAGE_REPLACED,
    Intent.ACTION_TIMEZONE_CHANGED,
    Intent.ACTION_TIME_CHANGED
)

private fun showReminder(context: Context, id: Long, fields: SubscriptionFields) {
    if (Build.VERSION.SDK_INT >= 33 &&
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(
        NotificationChannel(CHANNEL_ID, "구독 결제 및 만료 알림", NotificationManager.IMPORTANCE_DEFAULT)
    )
    val openApp = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
    )
    val message = when (fields.renewal) {
        Renewal.Auto -> "예정된 결제 금액 · ${formattedPrice(fields.price, fields.currency)}"
        Renewal.Prepaid -> "이용 혜택이 오늘 만료됩니다. 계속 이용하려면 재구매를 확인하세요."
        Renewal.Unknown -> "결제 또는 이용권 상태를 확인하세요."
    }
    val notification = Notification.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(fields.displayName)
        .setContentText(message)
        .setContentIntent(openApp)
        .setAutoCancel(true)
        .build()
    manager.notify(id.toString(), 1, notification)
}
