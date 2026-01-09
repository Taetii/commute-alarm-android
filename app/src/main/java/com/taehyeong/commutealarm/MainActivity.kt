package com.taehyeong.commutealarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.taehyeong.commutealarm.ui.CommuteAlarmApp
import com.taehyeong.commutealarm.ui.theme.CommuteAlarmTheme
import com.taehyeong.commutealarm.util.FailureNotificationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Cancel failure notifications when user opens the app
        FailureNotificationManager.cancelFailureNotifications(this)
        
        setContent {
            CommuteAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CommuteAlarmApp()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Also cancel when returning to the app
        FailureNotificationManager.cancelFailureNotifications(this)
    }
}
