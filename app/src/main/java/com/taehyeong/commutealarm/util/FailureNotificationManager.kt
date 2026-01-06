package com.taehyeong.commutealarm.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.taehyeong.commutealarm.MainActivity
import com.taehyeong.commutealarm.R

object FailureNotificationManager {
    private const val CHANNEL_ID = "failure_notification_channel"
    private const val NOTIFICATION_ID = 1001
    private const val INTERVAL_MS = 10_000L // 10 seconds
    private const val MAX_NOTIFICATIONS = 30 // 30 times = 5 minutes total
    
    private var handler: Handler? = null
    private var notificationCount = 0
    private var isRunning = false
    
    fun startFailureNotifications(context: Context) {
        if (isRunning) return
        
        createNotificationChannel(context)
        isRunning = true
        notificationCount = 0
        handler = Handler(Looper.getMainLooper())
        
        scheduleNextNotification(context)
    }
    
    private fun scheduleNextNotification(context: Context) {
        if (!isRunning || notificationCount >= MAX_NOTIFICATIONS) {
            stopNotifications()
            return
        }
        
        handler?.postDelayed({
            sendNotification(context)
            notificationCount++
            scheduleNextNotification(context)
        }, INTERVAL_MS)
    }
    
    private fun sendNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val remaining = MAX_NOTIFICATIONS - notificationCount
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.notification_failure_title))
            .setContentText("${context.getString(R.string.notification_failure_message)} (${remaining}회 남음)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun cancelFailureNotifications(context: Context) {
        stopNotifications()
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
    
    private fun stopNotifications() {
        isRunning = false
        handler?.removeCallbacksAndMessages(null)
        handler = null
        notificationCount = 0
    }
}
