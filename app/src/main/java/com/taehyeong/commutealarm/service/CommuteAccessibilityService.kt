package com.taehyeong.commutealarm.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.taehyeong.commutealarm.util.FailureNotificationManager

class CommuteAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "CommuteAccessibility"
        private const val HIWORKS_PACKAGE = "kr.co.hiworks.office"
        
        private var instance: CommuteAccessibilityService? = null
        private var pendingAction: CommuteAction? = null
        
        enum class CommuteAction { CHECK_IN, CHECK_OUT }
        
        fun triggerCheckIn(context: Context) {
            pendingAction = CommuteAction.CHECK_IN
            launchHiworks(context)
        }
        
        fun triggerCheckOut(context: Context) {
            pendingAction = CommuteAction.CHECK_OUT
            launchHiworks(context)
        }
        
        private fun launchHiworks(context: Context) {
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(HIWORKS_PACKAGE)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    Log.e(TAG, "Hiworks app not found")
                    FailureNotificationManager.startFailureNotifications(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch Hiworks", e)
                FailureNotificationManager.startFailureNotifications(context)
            }
        }
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private var currentStep = 0
    private var retryCount = 0
    private val maxRetries = 5
    private var hasStarted = false
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "★★★ Accessibility Service Connected ★★★")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        val packageName = event.packageName?.toString() ?: return
        Log.d(TAG, "Event received: package=$packageName, type=${event.eventType}, action=$pendingAction")
        
        if (pendingAction == null) return
        if (packageName != HIWORKS_PACKAGE) return
        
        Log.d(TAG, "★ Hiworks event detected! Starting automation...")
        
        // Only start once per action
        if (!hasStarted) {
            hasStarted = true
            handleHiworksEvent()
        }
    }
    
    private fun handleHiworksEvent() {
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Waiting 4 seconds for app to load...")
        handler.postDelayed({
            Log.d(TAG, "Starting executeNextStep()")
            executeNextStep()
        }, 4000) // Wait 4 seconds for Hiworks app to fully load
    }
    
    private fun executeNextStep() {
        when (currentStep) {
            0 -> {
                // Step 1: Click menu button (bottom right area)
                Log.d(TAG, "Step 1: Looking for menu button")
                if (!clickByText("전체 메뉴") && !clickByText("메뉴")) {
                    // Try clicking by position if text not found
                    clickByContentDescription("전체 메뉴")
                }
                currentStep++
                scheduleNextStep(1500)
            }
            1 -> {
                // Step 2: Click "근무" icon in menu (NOT "근무체크" which is different)
                Log.d(TAG, "Step 2: Looking for 근무 icon in menu")
                // Use exact match to avoid clicking "근무체크"
                if (clickByExactText("근무")) {
                    Log.d(TAG, "Step 2: Found 근무 icon")
                    currentStep++
                    scheduleNextStep(1500)
                } else {
                    retryOrFail()
                }
            }
            2 -> {
                // Step 3: Click "출근하기" or "퇴근하기"
                val targetText = when (pendingAction) {
                    CommuteAction.CHECK_IN -> "출근하기"
                    CommuteAction.CHECK_OUT -> "퇴근하기"
                    null -> return
                }
                Log.d(TAG, "Step 3: Looking for $targetText button")
                if (clickByText(targetText)) {
                    currentStep++
                    scheduleNextStep(2000)
                } else {
                    retryOrFail()
                }
            }
            3 -> {
                // Step 4: Success - take screenshot and share
                Log.d(TAG, "Step 4: Automation complete, taking screenshot")
                onSuccess()
            }
        }
    }
    
    private fun scheduleNextStep(delayMs: Long) {
        handler.postDelayed({ executeNextStep() }, delayMs)
    }
    
    private fun retryOrFail() {
        retryCount++
        if (retryCount < maxRetries) {
            Log.d(TAG, "Retrying step $currentStep (attempt $retryCount)")
            scheduleNextStep(2000)
        } else {
            onFailure()
        }
    }
    
    private fun onSuccess() {
        Log.d(TAG, "Commute automation successful!")
        resetState()
        // TODO: Take screenshot and share
        FailureNotificationManager.cancelFailureNotifications(this)
    }
    
    private fun onFailure() {
        Log.e(TAG, "Commute automation failed at step $currentStep")
        resetState()
        FailureNotificationManager.startFailureNotifications(this)
    }
    
    private fun resetState() {
        currentStep = 0
        retryCount = 0
        pendingAction = null
        hasStarted = false
        handler.removeCallbacksAndMessages(null)
    }
    
    private fun clickByText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        
        for (node in nodes) {
            if (node.isClickable) {
                val result = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                node.recycle()
                if (result) return true
            }
            // Try clicking parent if node itself isn't clickable
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    parent.recycle()
                    if (result) return true
                }
                parent = parent.parent
            }
            node.recycle()
        }
        return false
    }
    
    // Click only when text matches exactly (not contains)
    private fun clickByExactText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        
        for (node in nodes) {
            // Check if text matches exactly
            val nodeText = node.text?.toString()?.trim()
            if (nodeText == text) {
                Log.d(TAG, "Found exact match: '$nodeText'")
                if (node.isClickable) {
                    val result = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    node.recycle()
                    if (result) return true
                }
                // Try clicking parent
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        parent.recycle()
                        if (result) return true
                    }
                    parent = parent.parent
                }
            }
            node.recycle()
        }
        return false
    }
    
    private fun clickByContentDescription(description: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        return findAndClickByContentDescription(rootNode, description)
    }
    
    private fun findAndClickByContentDescription(node: AccessibilityNodeInfo, description: String): Boolean {
        if (node.contentDescription?.toString()?.contains(description, ignoreCase = true) == true) {
            if (node.isClickable) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickByContentDescription(child, description)) {
                child.recycle()
                return true
            }
            child.recycle()
        }
        return false
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
        resetState()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        resetState()
    }
}
