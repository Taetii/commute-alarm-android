package com.taehyeong.commutealarm.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taehyeong.commutealarm.ui.theme.*

@Composable
fun HelpScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
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
        Text(
            text = "📋 초기 설정 가이드",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "앱이 잠금화면에서 정상 작동하려면 아래 설정이 필요합니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 1. 접근성 서비스
        HelpCard(
            title = "1️⃣ 접근성 서비스 활성화 (필수)",
            content = """
                Hiworks 앱을 자동으로 조작하려면 접근성 서비스가 필요합니다.
                
                1. 아래 버튼을 눌러 접근성 설정으로 이동
                2. "설치된 앱" 또는 "다운로드된 앱" 선택
                3. "Hiworks-checker" 찾아서 선택
                4. 토글을 켜서 활성화
                5. "허용" 확인
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("접근성 설정 열기", modifier = Modifier.padding(8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 2. 배터리 최적화 제외
        HelpCard(
            title = "2️⃣ 배터리 최적화 제외 (필수)",
            content = """
                배터리 최적화가 켜져 있으면 알람이 정확한 시간에 작동하지 않을 수 있습니다.
                
                1. 아래 버튼을 눌러 앱 정보로 이동
                2. "배터리" 메뉴 선택
                3. "제한 없음" 또는 "최적화 안함" 선택
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                // Go to app details settings - works on all phones
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("배터리 최적화 설정 열기", modifier = Modifier.padding(8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 3. 잠금화면 설정
        HelpCard(
            title = "3️⃣ 잠금화면 루틴 설정 (지문/Face ID 사용 시)",
            content = """
                지문/Face ID 잠금이 있으면 화면이 켜져도 잠금이 풀리지 않아 자동화가 진행되지 않습니다.
                
                삼성 "모드 및 루틴" 설정:
                
                [루틴 1: 잠금 해제]
                • 조건: 시간 (출근 5분 전, 예: 08:25)
                • 동작: 잠금화면 유형 → "없음" 또는 "스와이프"
                
                [루틴 2: 잠금 복원] 
                • 조건: 시간 (출근 후, 예: 08:35)
                • 동작: 잠금화면 유형 → 기존 방식 (지문 등)
                
                ※ 퇴근 시간도 동일하게 설정하세요.
                ※ 설정 → 모드 및 루틴 → 루틴 탭에서 추가합니다.
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                // Try to open Modes and Routines, fallback to main settings
                try {
                    val intent = Intent()
                    intent.setClassName("com.samsung.android.app.routines", "com.samsung.android.app.routines.MainActivity")
                    context.startActivity(intent)
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("모드 및 루틴 열기 (삼성)", modifier = Modifier.padding(8.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 5. 사용 방법
        HelpCard(
            title = "📱 사용 방법",
            content = """
                1. 출근/퇴근 시간을 설정하세요
                2. 활성화 토글이 켜져 있어야 합니다
                3. 설정된 시간에 자동으로 Hiworks 앱을 열고 출퇴근 버튼을 클릭합니다
                4. 이미 출퇴근을 완료한 경우 자동으로 앱을 종료합니다
                
                테스트:
                "출근 체크하기" 또는 "퇴근 체크하기" 버튼으로 바로 테스트할 수 있습니다.
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 6. 문제 해결
        HelpCard(
            title = "🔧 문제 해결",
            content = """
                자동화가 실패하면:
                • 10초마다 알림이 30회 반복됩니다 (5분간)
                • 알림을 탭하면 앱이 열립니다
                • 수동으로 Hiworks에서 출퇴근을 처리하세요
                
                여전히 작동하지 않으면:
                • 위의 1~4번 설정을 다시 확인하세요
                • 폰을 재부팅 후 다시 시도하세요
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HelpCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = AccentGreen,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}
