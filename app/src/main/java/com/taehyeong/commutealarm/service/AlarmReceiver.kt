package com.taehyeong.commutealarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.taehyeong.commutealarm.WakeActivity
import com.taehyeong.commutealarm.data.HolidayRepository
import com.taehyeong.commutealarm.data.LeaveRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_CHECK_IN = "com.taehyeong.commutealarm.CHECK_IN"
        const val ACTION_CHECK_OUT = "com.taehyeong.commutealarm.CHECK_OUT"
        private const val CHANNEL_ID = "commute_alarm_channel"
        private const val NOTIFICATION_ID = 8888
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "★★★ Alarm received: ${intent.action} ★★★")
        
        // Acquire wake lock immediately
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "CommuteAlarm:AlarmWakeLock"
        )
        wakeLock.acquire(60 * 1000L)
        
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now()
                
                // Check if today is a day off
                if (HolidayRepository.isDayOff(today)) {
                    Log.d(TAG, "Today is a day off, skipping")
                    return@launch
                }
                
                // Check if today is a leave day
                if (LeaveRepository.isLeaveDay(context, today)) {
                    Log.d(TAG, "Today is a leave day, skipping")
                    return@launch
                }
                
                val action = intent.action ?: return@launch
                
                // Show full-screen notification to wake device
                showFullScreenNotification(context, action)
                
                // Wait for screen to wake up
                kotlinx.coroutines.delay(3000)
                
                // Trigger the accessibility service
                when (action) {
                    ACTION_CHECK_IN -> {
                        Log.d(TAG, "★ Triggering check-in ★")
                        CommuteAccessibilityService.triggerCheckIn(context)
                    }
                    ACTION_CHECK_OUT -> {
                        Log.d(TAG, "★ Triggering check-out ★")
                        CommuteAccessibilityService.triggerCheckOut(context)
                    }
                }
            } finally {
                wakeLock.release()
                pendingResult.finish()
            }
        }
    }
    
    private fun showFullScreenNotification(context: Context, action: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel with high importance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "출퇴근 알람",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "출퇴근 자동 체크 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create full-screen intent
        val fullScreenIntent = Intent(context, WakeActivity::class.java).apply {
            putExtra("action", action)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (action == ACTION_CHECK_IN) "출근 체크" else "퇴근 체크"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(title)
            .setContentText("Hiworks 자동 체크 진행 중...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        Log.d(TAG, "Showing full-screen notification")
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
