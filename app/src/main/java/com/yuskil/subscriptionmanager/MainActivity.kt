package com.yuskil.subscriptionmanager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme {
    Surface(modifier = Modifier.fillMaxSize()) {
     Column(modifier = Modifier.safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("구독 매니저", style = MaterialTheme.typography.headlineMedium)
      Text("여러 서비스의 정기 결제 비용을 한곳에서 확인하세요.")
      Text("개발 환경 확인용 화면입니다. 구독 등록 기능은 다음 단계에서 추가합니다.")
     }
    }
   }
  }
 }
}
