package com.taehyeong.commutealarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received: ${intent.action}")
        
        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now()
            
            // Check if today is a day off
            if (HolidayRepository.isDayOff(today)) {
                Log.d(TAG, "Today is a day off (weekend or holiday), skipping")
                return@launch
            }
            
            // Check if today is a leave day
            if (LeaveRepository.isLeaveDay(context, today)) {
                Log.d(TAG, "Today is a leave day, skipping")
                return@launch
            }
            
            // Execute the commute action
            when (intent.action) {
                ACTION_CHECK_IN -> {
                    Log.d(TAG, "Triggering check-in")
                    CommuteAccessibilityService.triggerCheckIn(context)
                }
                ACTION_CHECK_OUT -> {
                    Log.d(TAG, "Triggering check-out")
                    CommuteAccessibilityService.triggerCheckOut(context)
                }
            }
        }
    }
}
