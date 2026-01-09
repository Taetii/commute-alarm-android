package com.taehyeong.commutealarm.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taehyeong.commutealarm.R
import com.taehyeong.commutealarm.data.CommuteSettings
import com.taehyeong.commutealarm.data.SettingsRepository
import com.taehyeong.commutealarm.service.CommuteAccessibilityService
import com.taehyeong.commutealarm.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    var checkInTime by remember { mutableStateOf(LocalTime.of(8, 30)) }
    var checkOutTime by remember { mutableStateOf(LocalTime.of(18, 30)) }
    var isEnabled by remember { mutableStateOf(true) }
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }
    
    val isAccessibilityEnabled = remember { 
        mutableStateOf(isAccessibilityServiceEnabled(context)) 
    }
    
    LaunchedEffect(Unit) {
        isAccessibilityEnabled.value = isAccessibilityServiceEnabled(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Primary, PrimaryVariant, Background)
                )
            )
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Text(
            text = "Hiworks-checker",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Real-time Clock
        val currentTime by produceState(initialValue = LocalTime.now()) {
            while (true) {
                value = LocalTime.now()
                kotlinx.coroutines.delay(1000L)
            }
        }
        Text(
            text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Accessibility Service Warning
        if (!isAccessibilityEnabled.value) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ 접근성 서비스 비활성화",
                        color = AccentRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Text("접근성 설정 열기")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Enable Toggle
        GlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.enabled),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                val scope = rememberCoroutineScope()
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { newValue ->
                        isEnabled = newValue
                        scope.launch {
                            SettingsRepository.saveSettings(context, CommuteSettings(
                                isEnabled = newValue,
                                checkInHour = checkInTime.hour,
                                checkInMinute = checkInTime.minute,
                                checkOutHour = checkOutTime.hour,
                                checkOutMinute = checkOutTime.minute
                            ))
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentGreen,
                        checkedTrackColor = AccentGreen.copy(alpha = 0.5f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Check-in Time (Clickable)
        GlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCheckInPicker = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.check_in_time),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = checkInTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    color = AccentGreen,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Check-out Time (Clickable)
        GlassCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCheckOutPicker = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.check_out_time),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = checkOutTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    color = AccentBlue,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { 
                    CommuteAccessibilityService.triggerCheckIn(context)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("출근 체크하기", modifier = Modifier.padding(8.dp))
            }
            
            Button(
                onClick = { 
                    CommuteAccessibilityService.triggerCheckOut(context)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("퇴근 체크하기", modifier = Modifier.padding(8.dp))
            }
        }
    }
    
    // Coroutine scope for saving settings
    val coroutineScope = rememberCoroutineScope()
    
    // Time Picker Dialogs
    if (showCheckInPicker) {
        TimePickerDialog(
            initialTime = checkInTime,
            onConfirm = { newTime ->
                checkInTime = newTime
                showCheckInPicker = false
                // Save settings and schedule alarm
                coroutineScope.launch {
                    SettingsRepository.saveSettings(context, CommuteSettings(
                        isEnabled = isEnabled,
                        checkInHour = newTime.hour,
                        checkInMinute = newTime.minute,
                        checkOutHour = checkOutTime.hour,
                        checkOutMinute = checkOutTime.minute
                    ))
                }
            },
            onDismiss = { showCheckInPicker = false }
        )
    }
    
    if (showCheckOutPicker) {
        TimePickerDialog(
            initialTime = checkOutTime,
            onConfirm = { newTime ->
                checkOutTime = newTime
                showCheckOutPicker = false
                // Save settings and schedule alarm
                coroutineScope.launch {
                    SettingsRepository.saveSettings(context, CommuteSettings(
                        isEnabled = isEnabled,
                        checkInHour = checkInTime.hour,
                        checkInMinute = checkInTime.minute,
                        checkOutHour = newTime.hour,
                        checkOutMinute = newTime.minute
                    ))
                }
            },
            onDismiss = { showCheckOutPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val serviceName = "${context.packageName}/${CommuteAccessibilityService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(serviceName)
}
