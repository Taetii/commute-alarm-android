package com.taehyeong.commutealarm

import android.app.Activity
import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager

/**
 * Transparent Activity to wake up the screen on lock screen
 * This activity starts, wakes the screen, and immediately finishes
 */
class WakeActivity : Activity() {
    
    companion object {
        private const val TAG = "WakeActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "★★★ WakeActivity started ★★★")
        
        // Set window flags to turn on screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        // Wake lock to ensure screen stays on
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "CommuteAlarm:ScreenWake"
        )
        wakeLock.acquire(10 * 1000L) // 10 seconds
        
        Log.d(TAG, "Screen should be on now, finishing activity")
        
        // Finish immediately - the accessibility service will handle Hiworks
        finish()
    }
}
