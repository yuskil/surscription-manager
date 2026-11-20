package com.yuskil.subscriptionmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val subscriptions: SubscriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { SubscriptionApp(subscriptions) } }
    }
}

private enum class Screen { Home, Add, Details, Edit }
private fun won(value: Long): String = String.format(Locale.KOREA, "%,d원", value)

@Composable
private fun SubscriptionApp(viewModel: SubscriptionViewModel) {
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    var selectedId by rememberSaveable { mutableLongStateOf(0L) }
    val selected = subscriptions?.find { it.id == selectedId }
    val saving = saveState == SaveState.Saving

    LaunchedEffect(saveState) {
        val result = saveState as? SaveState.Saved ?: return@LaunchedEffect
        selectedId = result.id
        screen = Screen.Details
        viewModel.clearSaveState()
    }
    val back: () -> Unit = {
        if (!saving) {
            viewModel.clearSaveState()
            screen = if (screen == Screen.Edit) Screen.Details else Screen.Home
        }
    }

    BackHandler(enabled = screen != Screen.Home, onBack = back)
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            key(screen) {
                Column(
                    modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (screen != Screen.Home) {
                        TextButton(onClick = back, enabled = !saving) { Text("뒤로") }
                    }
                    when (screen) {
                        Screen.Home -> {
                            val entries = subscriptions
                            if (entries == null) {
                                Text("구독을 불러오는 중…")
                            } else {
                                Home(entries, { screen = Screen.Add }) {
                                    selectedId = it
                                    screen = Screen.Details
                                }
                            }
                        }
                        Screen.Add -> SubscriptionForm(null, saveState, viewModel::save)
                        Screen.Details -> if (selected == null) {
                            Text("구독을 불러오는 중…")
                        } else {
                            Details(selected.fields) { screen = Screen.Edit }
                        }
                        Screen.Edit -> if (selected == null) {
                            Text("구독을 불러오는 중…")
                        } else {
                            SubscriptionForm(selected, saveState, viewModel::save)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Home(subscriptions: List<Subscription>, onAdd: () -> Unit, onSelect: (Long) -> Unit) {
    val active = subscriptions.filter { it.fields.active }
    val annual = active.sumOf { it.fields.price * it.fields.cycle.paymentsPerYear }
    val upcoming = active.filter { !it.fields.date.isBefore(LocalDate.now()) }.take(3)

    Text("구독 매니저", style = MaterialTheme.typography.headlineLarge)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("월평균 환산 지출")
            Text(won((annual + 6) / 12), style = MaterialTheme.typography.headlineMedium)
            Text("연간 환산 지출 · " + won(annual))
            Text("활성 구독 기준 · 결제 주기를 월 단위로 환산", style = MaterialTheme.typography.bodySmall)
        }
    }
    Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("구독 추가") }
    Text("예정된 결제", style = MaterialTheme.typography.titleLarge)
    if (upcoming.isEmpty()) Text("예정된 결제가 없습니다.")
    upcoming.forEach { (_, item) ->
        Text(item.date.toString() + " · " + item.name + " · " + won(item.price))
    }

    Text("구독 목록", style = MaterialTheme.typography.titleLarge)
    if (subscriptions.isEmpty()) Text("구독 추가를 눌러 첫 구독을 등록하세요.")
    subscriptions.forEach { (id, item) ->
        OutlinedCard(onClick = { onSelect(id) }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(won(item.price) + " · " + item.cycle.label)
                Text("다음 결제일 " + item.date + if (item.active) "" else " · 비활성")
            }
        }
    }
}

@Composable
private fun Details(item: SubscriptionFields, onEdit: () -> Unit) {
    Text(item.name, style = MaterialTheme.typography.headlineMedium)
    Text("결제 금액 · " + won(item.price))
    Text("결제 주기 · " + item.cycle.label)
    Text("다음 결제일 · " + item.date)
    Text("카테고리 · " + item.category.ifBlank { "미설정" })
    Text("메모 · " + item.memo.ifBlank { "없음" })
    Text(if (item.active) "활성 구독" else "비활성 구독")
    Button(onClick = onEdit) { Text("수정") }
}

@Composable
private fun SubscriptionForm(
    initial: Subscription?,
    saveState: SaveState,
    onSave: (Long?, SubscriptionFields) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initial?.fields?.name.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(initial?.fields?.price?.toString().orEmpty()) }
    var cycle by rememberSaveable { mutableStateOf(initial?.fields?.cycle ?: Cycle.Monthly) }
    var date by rememberSaveable { mutableStateOf(initial?.fields?.date?.toString().orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initial?.fields?.category.orEmpty()) }
    var memo by rememberSaveable { mutableStateOf(initial?.fields?.memo.orEmpty()) }
    var active by rememberSaveable { mutableStateOf(initial?.fields?.active ?: true) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val amount = price.trim().toLongOrNull()?.takeIf { it in 1..999_999_999 }
    val billingDate = runCatching { LocalDate.parse(date.trim()) }.getOrNull()

    Text(if (initial == null) "구독 등록" else "구독 수정", style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(name, { name = it }, label = { Text("서비스명") },
        singleLine = true, isError = submitted && name.isBlank(), modifier = Modifier.fillMaxWidth())
    OutlinedTextField(price, { price = it }, label = { Text("결제 금액 (원)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
        isError = submitted && amount == null, modifier = Modifier.fillMaxWidth())
    Text("결제 주기", style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Cycle.entries.forEach { option ->
            FilterChip(selected = cycle == option, onClick = { cycle = option }, label = { Text(option.label) })
        }
    }
    OutlinedTextField(date, { date = it }, label = { Text("다음 결제일") },
        supportingText = { Text("YYYY-MM-DD 형식으로 입력") },
        singleLine = true, isError = submitted && billingDate == null, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(category, { category = it }, label = { Text("카테고리 (선택)") },
        singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(memo, { memo = it }, label = { Text("메모 (선택)") }, modifier = Modifier.fillMaxWidth())
    if (initial != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("활성 구독")
            Switch(checked = active, onCheckedChange = { active = it })
        }
    }
    if (submitted && (name.isBlank() || amount == null || billingDate == null)) {
        Text("서비스명, 1~999,999,999원 사이의 금액, 올바른 결제일을 입력하세요.",
            color = MaterialTheme.colorScheme.error)
    }
    if (saveState == SaveState.Failed) {
        Text("저장하지 못했습니다. 다시 시도하세요.", color = MaterialTheme.colorScheme.error)
    }
    Button(
        onClick = {
            submitted = true
            if (name.isNotBlank() && amount != null && billingDate != null) {
                onSave(
                    initial?.id,
                    SubscriptionFields(name.trim(), amount, cycle, billingDate, category.trim(), memo.trim(), active)
                )
            }
        },
        enabled = saveState == SaveState.Idle || saveState == SaveState.Failed,
        modifier = Modifier.fillMaxWidth()
    ) { Text(if (saveState == SaveState.Saving) "저장 중…" else "저장") }
}
