package com.yuskil.subscriptionmanager

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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

private const val YOUTUBE = "유튜브"

private fun String.searchKey() = lowercase(Locale.ROOT).filter(Char::isLetterOrDigit)

internal fun findServicePreset(key: String?): ServicePreset? = serviceCatalog.firstOrNull { it.key == key }

@Composable
internal fun ServicePicker(
    query: String,
    onQuery: (String) -> Unit,
    listModifier: Modifier,
    onSelect: (ServicePreset?) -> Unit
) {
    var showYouTubePrices by rememberSaveable { mutableStateOf(false) }
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
    val displayed = results.distinctBy { if (it.name == YOUTUBE) YOUTUBE else it.key }
    if (displayed.isEmpty()) Text("검색 결과가 없습니다.")
    LazyColumn(listModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(displayed, key = { if (it.name == YOUTUBE) YOUTUBE else it.key }) { preset ->
            val isYouTube = preset.name == YOUTUBE
            OutlinedButton(
                onClick = { if (isYouTube) showYouTubePrices = true else onSelect(preset) },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isYouTube) YouTubeButtonIcon()
                Text(if (isYouTube) YOUTUBE else preset.optionLabel(includeService = true))
            }
        }
    }
    if (showYouTubePrices) {
        AlertDialog(
            onDismissRequest = { showYouTubePrices = false },
            title = { Text("유튜브 결제 선택") },
            text = {
                Column(
                    Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    serviceCatalog.filter { it.name == YOUTUBE }.forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                showYouTubePrices = false
                                onSelect(preset)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            YouTubeButtonIcon()
                            Text(when (preset.plan) {
                                "" -> "직접 입력"
                                "금액 지정" -> formattedPrice(requireNotNull(preset.price), "KRW") + " · 금액 지정"
                                else -> preset.optionLabel(includeService = false)
                            })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showYouTubePrices = false }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun YouTubeButtonIcon() {
    Image(
        painter = painterResource(R.drawable.ic_youtube_play),
        contentDescription = null
    )
    Spacer(Modifier.width(8.dp))
}

private fun ServicePreset.optionLabel(includeService: Boolean): String =
    listOf(
        name.takeIf { includeService }.orEmpty(),
        plan, channel, cycle.label,
        price?.let { formattedPrice(it, "KRW") } ?: "금액 직접 입력"
    ).filter(String::isNotBlank).joinToString(" · ")
