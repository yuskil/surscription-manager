package com.yuskil.subscriptionmanager

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal data class ServicePreset(val name: String, val category: String)

private val serviceCatalog = listOf(
    ServicePreset("유튜브 프리미엄", "영상"),
    ServicePreset("티빙", "영상"),
    ServicePreset("넷플릭스", "영상"),
    ServicePreset("노벨피아", "독서"),
    ServicePreset("밀리의 서재", "독서"),
    ServicePreset("ChatGPT Plus", "AI"),
    ServicePreset("Gemini", "AI"),
    ServicePreset("Claude", "AI")
)

internal fun findServicePreset(name: String?): ServicePreset? =
    serviceCatalog.firstOrNull { it.name == name }

@Composable
internal fun ServicePicker(onSelect: (ServicePreset?) -> Unit) {
    Text("서비스 선택", style = MaterialTheme.typography.headlineMedium)
    OutlinedButton(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
        Text("직접 추가")
    }

    serviceCatalog.groupBy { it.category }.forEach { (category, services) ->
        Text(category, style = MaterialTheme.typography.titleMedium)
        services.forEach { service ->
            OutlinedButton(
                onClick = { onSelect(service) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(service.name)
            }
        }
    }
}
