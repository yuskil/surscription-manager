package com.yuskil.subscriptionmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.Locale

internal data class ServicePreset(
    val name: String,
    val category: String,
    val plan: String = "",
    val channel: String = "",
    val price: Long? = null,
    val cycle: Cycle = Cycle.Monthly,
    val renewal: Renewal = Renewal.Auto,
    val creatorRequired: Boolean = false,
    val aliases: String = "",
    val note: String = ""
) {
    val key get() = listOf(name, plan, channel, cycle.name, renewal.name).joinToString("|")
    val searchable get() = listOf(name, category, plan, channel, aliases).joinToString(" ").searchKey()
}

private fun String.searchKey() = lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)

internal fun findServicePreset(key: String?): ServicePreset? = serviceCatalog.firstOrNull { it.key == key }

@Composable
internal fun ServicePicker(
    query: String,
    onQuery: (String) -> Unit,
    listModifier: Modifier,
    onSelect: (ServicePreset?) -> Unit
) {
    Text("요금제 검색", style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        label = { Text("서비스명, 요금제, 결제 경로") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedButton(onClick = { onSelect(null) }, modifier = Modifier.fillMaxWidth()) {
        Text("직접 추가")
    }
    val terms = query.split(Regex("\\s+")).map { it.searchKey() }.filter(String::isNotEmpty)
    val results = serviceCatalog.filter { preset -> terms.all { it in preset.searchable } }
    if (results.isEmpty()) Text("검색 결과가 없습니다.")
    LazyColumn(listModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(results, key = { it.key }) { preset ->
            OutlinedButton(onClick = { onSelect(preset) }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    listOf(
                        preset.name, preset.plan, preset.channel, preset.cycle.label,
                        preset.price?.let { formattedPrice(it, "KRW") } ?: "금액 직접 입력"
                    ).filter(String::isNotBlank).joinToString(" · ")
                )
            }
        }
    }
}
