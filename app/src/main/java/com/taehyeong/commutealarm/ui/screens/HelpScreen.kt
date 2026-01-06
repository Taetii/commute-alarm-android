package com.taehyeong.commutealarm.ui.screens

import android.content.Intent
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
            text = "도움말",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 접근성 서비스 설정 안내
        HelpCard(
            title = "1. 접근성 서비스 활성화",
            content = """
                앱이 Hiworks를 자동으로 조작하려면 접근성 서비스가 필요합니다.
                
                설정 방법:
                1. 아래 "접근성 설정 열기" 버튼을 누르세요
                2. "설치된 앱" 또는 "다운로드된 앱" 찾기
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
        
        // Android 14+ 안내
        HelpCard(
            title = "2. Android 14 이상 사용자",
            content = """
                Android 14부터 보안이 강화되어 접근성 서비스 활성화가 막힐 수 있습니다.
                
                해결 방법:
                1. PC에 USB로 폰 연결
                2. ADB 명령어 실행:
                
                adb shell settings put secure enabled_accessibility_services com.taehyeong.commutealarm/com.taehyeong.commutealarm.service.CommuteAccessibilityService
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 사용 방법
        HelpCard(
            title = "3. 사용 방법",
            content = """
                • 출근/퇴근 시간을 설정하세요
                • 활성화 토글이 켜져 있어야 합니다
                • 설정된 시간에 자동으로 Hiworks 앱을 열고 출퇴근 버튼을 클릭합니다
                
                테스트:
                "출근 체크하기" 또는 "퇴근 체크하기" 버튼으로 바로 테스트할 수 있습니다.
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 주의사항
        HelpCard(
            title = "⚠️ 주의사항",
            content = """
                • 배터리 최적화에서 이 앱을 제외해주세요
                • 절전 모드에서는 작동하지 않을 수 있습니다
                • 화면이 꺼진 상태에서도 알람은 작동합니다
                • 연차나 공휴일에는 자동으로 건너뜁니다
            """.trimIndent()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 문제 해결
        HelpCard(
            title = "4. 문제 해결",
            content = """
                자동화가 실패하면:
                • 10초마다 알림이 30회 반복됩니다 (5분간)
                • 알림을 탭하면 앱이 열립니다
                • 수동으로 Hiworks에서 출퇴근을 처리하세요
            """.trimIndent()
        )
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
