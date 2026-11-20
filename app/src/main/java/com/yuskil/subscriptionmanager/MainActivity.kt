package com.yuskil.subscriptionmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import java.time.LocalDate
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { SubscriptionApp() } }
    }
}

private enum class Screen { Home, Add, Details, Edit }
private enum class Cycle(val label: String, val paymentsPerYear: Int) {
    Monthly("월간", 12), Quarterly("분기", 4), Yearly("연간", 1)
}

private data class Subscription(
    val id: Int,
    val name: String,
    val price: Long,
    val cycle: Cycle,
    val date: LocalDate,
    val category: String = "",
    val memo: String = "",
    val active: Boolean = true
)

private fun sampleSubscriptions(): List<Subscription> {
    val today = LocalDate.now()
    return listOf(
        Subscription(1, "동영상 스트리밍", 14900, Cycle.Monthly, today.plusDays(3), "영상"),
        Subscription(2, "클라우드 저장 공간", 9900, Cycle.Quarterly, today.plusDays(10), "생산성"),
        Subscription(3, "독서 구독", 59000, Cycle.Yearly, today.plusDays(21), "도서")
    )
}

private fun won(value: Long): String = String.format(Locale.KOREA, "%,d원", value)

@Composable
private fun SubscriptionApp() {
    var subscriptions by remember { mutableStateOf(sampleSubscriptions()) }
    var screen by remember { mutableStateOf(Screen.Home) }
    var selectedId by remember { mutableIntStateOf(0) }
    val selected = subscriptions.find { it.id == selectedId }
    val back: () -> Unit = {
        screen = if (screen == Screen.Edit) Screen.Details else Screen.Home
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
                    if (screen != Screen.Home) TextButton(onClick = back) { Text("뒤로") }
                    when (screen) {
                        Screen.Home -> Home(subscriptions, { screen = Screen.Add }) {
                            selectedId = it
                            screen = Screen.Details
                        }
                        Screen.Add -> SubscriptionForm(initial = null) { entry ->
                            val id = (subscriptions.maxOfOrNull { it.id } ?: 0) + 1
                            subscriptions = subscriptions + entry.copy(id = id)
                            selectedId = id
                            screen = Screen.Details
                        }
                        Screen.Details -> if (selected != null) {
                            Details(selected) { screen = Screen.Edit }
                        }
                        Screen.Edit -> if (selected != null) {
                            SubscriptionForm(selected) { entry ->
                                subscriptions = subscriptions.map { if (it.id == entry.id) entry else it }
                                screen = Screen.Details
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Home(subscriptions: List<Subscription>, onAdd: () -> Unit, onSelect: (Int) -> Unit) {
    val sorted = subscriptions.sortedBy { it.date }
    val active = sorted.filter { it.active }
    val annual = active.sumOf { it.price * it.cycle.paymentsPerYear }
    val upcoming = active.filter { !it.date.isBefore(LocalDate.now()) }.take(3)

    Text("구독 매니저", style = MaterialTheme.typography.headlineLarge)
    Text("샘플 데이터로 체험 중입니다. 앱을 종료하면 변경 내용이 초기화됩니다.")
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("월평균 환산 지출")
            Text(won((annual + 6) / 12), style = MaterialTheme.typography.headlineMedium)
            Text("연간 환산 지출 · " + won(annual))
            Text("활성 구독 기준", style = MaterialTheme.typography.bodySmall)
        }
    }
    Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("구독 추가") }
    Text("예정된 결제", style = MaterialTheme.typography.titleLarge)
    if (upcoming.isEmpty()) Text("예정된 결제가 없습니다.")
    upcoming.forEach { item ->
        Text(item.date.toString() + " · " + item.name + " · " + won(item.price))
    }

    Text("구독 목록", style = MaterialTheme.typography.titleLarge)
    sorted.forEach { item ->
        OutlinedCard(onClick = { onSelect(item.id) }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(won(item.price) + " · " + item.cycle.label)
                Text("다음 결제일 " + item.date + if (item.active) "" else " · 비활성")
            }
        }
    }
}

@Composable
private fun Details(item: Subscription, onEdit: () -> Unit) {
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
private fun SubscriptionForm(initial: Subscription?, onSave: (Subscription) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(initial?.price?.toString().orEmpty()) }
    var cycle by rememberSaveable { mutableStateOf(initial?.cycle ?: Cycle.Monthly) }
    var date by rememberSaveable { mutableStateOf(initial?.date?.toString().orEmpty()) }
    var category by rememberSaveable { mutableStateOf(initial?.category.orEmpty()) }
    var memo by rememberSaveable { mutableStateOf(initial?.memo.orEmpty()) }
    var active by rememberSaveable { mutableStateOf(initial?.active ?: true) }
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
    Button(onClick = {
        submitted = true
        if (name.isNotBlank() && amount != null && billingDate != null) {
            onSave(Subscription(initial?.id ?: 0, name.trim(), amount, cycle, billingDate, category.trim(), memo.trim(), active))
        }
    }, modifier = Modifier.fillMaxWidth()) { Text("저장") }
}
