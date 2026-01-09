package com.taehyeong.commutealarm.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.taehyeong.commutealarm.data.SettingsRepository
import com.taehyeong.commutealarm.service.AlarmReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object CommuteScheduler {
    
    private const val TAG = "CommuteScheduler"
    private const val CHECK_IN_REQUEST_CODE = 1001
    private const val CHECK_OUT_REQUEST_CODE = 1002
    
    fun scheduleAlarms(context: Context) {
        runBlocking {
            val settings = SettingsRepository.getSettings(context).first()
            
            if (!settings.isEnabled) {
                Log.d(TAG, "Scheduling disabled")
                cancelAlarms(context)
                return@runBlocking
            }
            
            scheduleCheckIn(context, settings.checkInTime)
            scheduleCheckOut(context, settings.checkOutTime)
        }
    }
    
    private fun scheduleCheckIn(context: Context, time: LocalTime) {
        scheduleAlarm(
            context = context,
            time = time,
            action = AlarmReceiver.ACTION_CHECK_IN,
            requestCode = CHECK_IN_REQUEST_CODE
        )
    }
    
    private fun scheduleCheckOut(context: Context, time: LocalTime) {
        scheduleAlarm(
            context = context,
            time = time,
            action = AlarmReceiver.ACTION_CHECK_OUT,
            requestCode = CHECK_OUT_REQUEST_CODE
        )
    }
    
    private fun scheduleAlarm(
        context: Context,
        time: LocalTime,
        action: String,
        requestCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate next trigger time
        var triggerDateTime = LocalDateTime.of(LocalDate.now(), time)
        if (triggerDateTime.isBefore(LocalDateTime.now())) {
            triggerDateTime = triggerDateTime.plusDays(1)
        }
        
        val triggerMillis = triggerDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        // Show pending alarm intent (for lock screen display)
        val showIntent = Intent(context, com.taehyeong.commutealarm.MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 100,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            // Use setAlarmClock - this is treated EXACTLY like the system alarm clock
            // and will ALWAYS wake the screen and show on lock screen
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "★ Scheduled ALARM CLOCK: $action at $triggerDateTime ★")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm clock", e)
            // Fallback to setExactAndAllowWhileIdle
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
                Log.d(TAG, "Fallback: Scheduled exact alarm: $action at $triggerDateTime")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule any alarm", e2)
            }
        }
    }
    
    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        listOf(
            CHECK_IN_REQUEST_CODE to AlarmReceiver.ACTION_CHECK_IN,
            CHECK_OUT_REQUEST_CODE to AlarmReceiver.ACTION_CHECK_OUT
        ).forEach { (requestCode, action) ->
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                this.action = action
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        
        Log.d(TAG, "All alarms cancelled")
    }
}
