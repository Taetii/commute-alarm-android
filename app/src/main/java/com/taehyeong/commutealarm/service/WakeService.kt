package com.taehyeong.commutealarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.taehyeong.commutealarm.WakeActivity

class WakeService : Service() {
    
    companion object {
        private const val TAG = "WakeService"
        private const val CHANNEL_ID = "wake_channel"
        private const val NOTIFICATION_ID = 9999
        const val EXTRA_ACTION = "extra_action"
        
        fun start(context: Context, action: String) {
            val intent = Intent(context, WakeService::class.java)
            intent.putExtra(EXTRA_ACTION, action)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
    
    private var wakeLock: PowerManager.WakeLock? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "★★★ WakeService created ★★★")
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "★★★ WakeService started ★★★")
        
        val action = intent?.getStringExtra(EXTRA_ACTION) ?: ""
        
        // Start foreground with full-screen intent
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Acquire wake lock
        acquireWakeLock()
        
        // Launch WakeActivity with full-screen intent capability
        launchWakeActivity()
        
        // Wait and trigger the commute action
        Thread {
            Thread.sleep(2000) // Wait for screen to wake
            
            when (action) {
                AlarmReceiver.ACTION_CHECK_IN -> {
                    Log.d(TAG, "★ Triggering check-in from service ★")
                    CommuteAccessibilityService.triggerCheckIn(this)
                }
                AlarmReceiver.ACTION_CHECK_OUT -> {
                    Log.d(TAG, "★ Triggering check-out from service ★")
                    CommuteAccessibilityService.triggerCheckOut(this)
                }
            }
            
            // Stop service after a delay
            Thread.sleep(30000) // 30 seconds to complete automation
            stopSelf()
        }.start()
        
        return START_NOT_STICKY
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "CommuteAlarm:WakeServiceLock"
        )
        wakeLock?.acquire(60 * 1000L)
        Log.d(TAG, "Wake lock acquired")
    }
    
    private fun launchWakeActivity() {
        try {
            val wakeIntent = Intent(this, WakeActivity::class.java)
            wakeIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            startActivity(wakeIntent)
            Log.d(TAG, "WakeActivity launched")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WakeActivity", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "출퇴근 알람",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "출퇴근 자동 체크 알람"
                setSound(null, null)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val fullScreenIntent = Intent(this, WakeActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("출퇴근 체크 중...")
            .setContentText("Hiworks 자동화 진행 중")
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        Log.d(TAG, "WakeService destroyed")
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
