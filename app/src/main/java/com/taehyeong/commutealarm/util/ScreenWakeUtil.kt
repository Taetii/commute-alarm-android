package com.taehyeong.commutealarm.util

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log

/**
 * Utility class to wake up the screen and unlock the device for automation
 */
object ScreenWakeUtil {
    private const val TAG = "ScreenWakeUtil"
    private var wakeLock: PowerManager.WakeLock? = null
    
    /**
     * Wake up the screen and acquire a wake lock to keep the CPU running
     */
    fun wakeUpScreen(context: Context) {
        Log.d(TAG, "Waking up screen...")
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // Acquire wake lock to keep CPU running
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
            "CommuteAlarm:WakeLock"
        )
        wakeLock?.acquire(60 * 1000L) // 60 seconds max
        
        Log.d(TAG, "Wake lock acquired, screen should be on")
    }
    
    /**
     * Release the wake lock when automation is complete
     */
    fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "Wake lock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wake lock", e)
        }
    }
    
    /**
     * Check if the screen is currently on
     */
    fun isScreenOn(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
    }
    
    /**
     * Check if the device is currently locked
     */
    fun isDeviceLocked(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardLocked
    }
}
