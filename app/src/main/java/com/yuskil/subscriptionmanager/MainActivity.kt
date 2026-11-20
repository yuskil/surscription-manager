package com.yuskil.subscriptionmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.util.Currency

class MainActivity : ComponentActivity() {
    private val subscriptions: SubscriptionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { SubscriptionApp(subscriptions) } }
    }
}

private enum class Screen { Home, PickService, Add, Details, Edit }


@Composable
private fun SubscriptionApp(viewModel: SubscriptionViewModel) {
    val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedPresetKey by rememberSaveable { mutableStateOf<String?>(null) }
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
            screen = when (screen) {
                Screen.Add -> Screen.PickService
                Screen.Edit -> Screen.Details
                else -> Screen.Home
            }
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
                        .then(if (screen == Screen.PickService) Modifier.fillMaxHeight()
                            else Modifier.verticalScroll(rememberScrollState())).padding(20.dp),
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
                                Home(entries, {
                                    searchQuery = ""
                                    screen = Screen.PickService
                                }) {
                                    selectedId = it
                                    screen = Screen.Details
                                }
                            }
                        }
                        Screen.PickService -> ServicePicker(searchQuery, { searchQuery = it }, Modifier.weight(1f)) { service ->
                            selectedPresetKey = service?.key
                            screen = Screen.Add
                        }
                        Screen.Add -> SubscriptionForm(
                            initial = null,
                            saveState = saveState,
                            onSave = viewModel::save,
                            preset = findServicePreset(selectedPresetKey)
                        )
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
    val today = LocalDate.now()
    val active = subscriptions.map { it.fields }.filter { it.active }
    val annual = active.filter { it.currency == "KRW" && it.renewal == Renewal.Auto }.sumOf {
        if (it.cycle == Cycle.ThirtyDays) (it.price * 365 + 15) / 30
        else it.price * it.cycle.paymentsPerYear
    }
    val events = active.mapNotNull { item -> item.eventDate(today)?.let { it to item } }
        .sortedBy { it.first }
    val upcoming = events.filter { it.second.renewal == Renewal.Auto }.take(3)
    val expirations = events.filter { it.second.renewal == Renewal.Prepaid }.take(3)

    Text("구독 매니저", style = MaterialTheme.typography.headlineLarge)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("월평균 환산 지출")
            Text(formattedPrice((annual + 6) / 12, "KRW"), style = MaterialTheme.typography.headlineMedium)
            Text("연간 환산 지출 · " + formattedPrice(annual, "KRW"))
            Text("활성 원화 자동 갱신 구독 기준", style = MaterialTheme.typography.bodySmall)
        }
    }
    Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text("구독 추가") }
    Text("예정된 결제", style = MaterialTheme.typography.titleLarge)
    if (upcoming.isEmpty()) Text("예정된 결제가 없습니다.")
    upcoming.forEach { (date, item) ->
        Text("$date · ${item.displayName} · ${formattedPrice(item.price, item.currency)}")
    }
    if (expirations.isNotEmpty()) {
        Text("예정된 혜택 만료", style = MaterialTheme.typography.titleLarge)
        expirations.forEach { (date, item) -> Text("$date · ${item.displayName}") }
    }

    Text("구독 목록", style = MaterialTheme.typography.titleLarge)
    if (subscriptions.isEmpty()) Text("구독 추가를 눌러 첫 구독을 등록하세요.")
    subscriptions.sortedBy { it.fields.eventDate(today) ?: it.fields.date }.forEach { (id, item) ->
        OutlinedCard(onClick = { onSelect(id) }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.displayName, style = MaterialTheme.typography.titleMedium)
                Text(formattedPrice(item.price, item.currency) + " · " + item.cycle.label)
                Text(eventText(item, today) + if (item.active) "" else " · 비활성")
            }
        }
    }
}

@Composable
private fun Details(item: SubscriptionFields, onEdit: () -> Unit) {
    Text(item.displayName, style = MaterialTheme.typography.headlineMedium)
    if (item.plan.isNotBlank()) Text("요금제 · " + item.plan)
    if (item.channel.isNotBlank()) Text("결제 경로 · " + item.channel)
    Text("결제 금액 · " + formattedPrice(item.price, item.currency))
    Text("결제 주기 · " + item.cycle.label)
    Text(eventText(item, LocalDate.now()))
    Text("갱신 방식 · " + item.renewal.label)
    Text(if (item.reminder) "알림 설정 켜짐" else "알림 설정 꺼짐")
    Text("카테고리 · " + item.category.ifBlank { "미설정" })
    Text("메모 · " + item.memo.ifBlank { "없음" })
    Text(if (item.active) "활성 구독" else "비활성 구독")
    Button(onClick = onEdit) { Text("수정") }
}

private fun eventText(item: SubscriptionFields, today: LocalDate): String {
    val date = if (item.active) item.eventDate(today) ?: item.date else item.date
    return "${item.eventLabel} · $date"
}

private val dateFormat = VisualTransformation { text ->
    val digits = text.text
    val formatted = digits.take(4) +
        (if (digits.length > 4) "-" + digits.drop(4).take(2) else "") +
        (if (digits.length > 6) "-" + digits.drop(6) else "")
    TransformedText(AnnotatedString(formatted), object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int =
            offset + if (offset > 6) 2 else if (offset > 4) 1 else 0

        override fun transformedToOriginal(offset: Int): Int =
            formatted.take(offset).count(Char::isDigit)
    })
}

@Composable
private fun SubscriptionForm(
    initial: Subscription?,
    saveState: SaveState,
    onSave: (Long?, SubscriptionFields) -> Unit,
    preset: ServicePreset? = null
) {
    val fields = initial?.fields
    var name by rememberSaveable { mutableStateOf((fields?.name ?: preset?.name).orEmpty()) }
    var plan by rememberSaveable { mutableStateOf((fields?.plan ?: preset?.plan).orEmpty()) }
    var channel by rememberSaveable { mutableStateOf((fields?.channel ?: preset?.channel).orEmpty()) }
    var creator by rememberSaveable { mutableStateOf(fields?.creator.orEmpty()) }
    var currency by rememberSaveable { mutableStateOf(fields?.currency ?: "KRW") }
    var price by rememberSaveable {
        mutableStateOf(fields?.let { priceInput(it.price, it.currency) } ?: preset?.price?.toString().orEmpty())
    }
    var cycle by rememberSaveable { mutableStateOf(fields?.cycle ?: preset?.cycle ?: Cycle.Monthly) }
    var renewal by rememberSaveable { mutableStateOf(fields?.renewal ?: preset?.renewal ?: Renewal.Auto) }
    var purchaseEntry by rememberSaveable { mutableStateOf(initial == null && preset?.renewal == Renewal.Prepaid) }
    var date by rememberSaveable { mutableStateOf(fields?.date?.toString()?.filter(Char::isDigit).orEmpty()) }
    var category by rememberSaveable { mutableStateOf((fields?.category ?: preset?.category).orEmpty()) }
    var memo by rememberSaveable { mutableStateOf(fields?.memo.orEmpty()) }
    var active by rememberSaveable { mutableStateOf(fields?.active ?: true) }
    var reminder by rememberSaveable { mutableStateOf(fields?.reminder ?: false) }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }
    val permit = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionDenied = !granted
    }
    val creatorRequired = preset?.creatorRequired == true || !fields?.creator.isNullOrBlank()
    val viaPurchase = purchaseEntry && cycle != Cycle.Unknown && renewal != Renewal.Unknown
    val enteredDate = runCatching {
        LocalDate.parse("${date.take(4)}-${date.drop(4).take(2)}-${date.drop(6)}").takeIf { it.year >= 1 }
    }.getOrNull()
    val dateLabel = when (renewal) {
        Renewal.Auto -> "결제 기준일"
        Renewal.Prepaid -> "혜택 만료일"
        Renewal.Unknown -> "확인 날짜"
    }
    val scheduledDate = enteredDate?.let { if (viaPurchase) cycle.after(it) else it }
    val amount = parsePrice(price, currency)
    val invalid = name.isBlank() || amount == null || scheduledDate == null ||
        (creatorRequired && creator.isBlank())

    Text(if (initial == null) "구독 등록" else "구독 수정", style = MaterialTheme.typography.headlineMedium)
    preset?.note?.takeIf(String::isNotBlank)?.let { Text(it) }
    EntryField(name, { name = it }, "서비스명", submitted && name.isBlank())
    EntryField(plan, { plan = it }, "요금제 또는 티어")
    EntryField(channel, { channel = it }, "결제 경로")
    if (creatorRequired) {
        EntryField(creator, { creator = it }, "채널 또는 크리에이터명", submitted && creator.isBlank())
    }
    EntryField(currency, { currency = it.uppercase(java.util.Locale.ROOT).take(3) }, "통화 코드 (예: KRW, USD)")
    OutlinedTextField(price, { price = it }, label = { Text("실제 결제 금액") },
        keyboardOptions = KeyboardOptions(keyboardType = if (currency == "KRW") KeyboardType.Number else KeyboardType.Decimal),
        singleLine = true, isError = submitted && amount == null, modifier = Modifier.fillMaxWidth())
    OptionMenu("결제 주기", cycle, Cycle.entries, { it.label }) { cycle = it }
    OptionMenu("갱신 방식", renewal, Renewal.entries, { it.label }) { renewal = it }
    if (fields == null && cycle != Cycle.Unknown && renewal != Renewal.Unknown) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = purchaseEntry, onCheckedChange = { purchaseEntry = it })
            Text("구매일로 ${if (renewal == Renewal.Auto) "다음 결제일" else "만료일"} 계산")
        }
    }
    OutlinedTextField(
        date, { value -> date = value.mapNotNull { it.digitToIntOrNull()?.digitToChar() }.take(8).joinToString("") },
        label = { Text(if (viaPurchase) "구매일" else dateLabel) },
        supportingText = {
            Text(if (viaPurchase && scheduledDate != null) "$dateLabel · $scheduledDate"
                else "숫자 8자리 입력 · YYYY-MM-DD")
        },
        visualTransformation = dateFormat,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true, isError = submitted && scheduledDate == null, modifier = Modifier.fillMaxWidth()
    )
    EntryField(category, { category = it }, "카테고리 (선택)")
    OutlinedTextField(memo, { memo = it }, label = { Text("메모 (선택)") }, modifier = Modifier.fillMaxWidth())
    if (initial != null) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("활성 구독")
            Switch(checked = active, onCheckedChange = { active = it })
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(when (renewal) {
            Renewal.Auto -> "결제 예정 알림"
            Renewal.Prepaid -> "만료 및 재구매 알림"
            Renewal.Unknown -> "구독 확인 알림"
        })
        Switch(checked = reminder, onCheckedChange = { enabled ->
            reminder = enabled
            if (enabled && Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) permit.launch(Manifest.permission.POST_NOTIFICATIONS)
        })
    }
    if (reminder && permissionDenied) Text("알림을 받으려면 기기 설정에서 알림을 허용하세요.")
    if (submitted && invalid) {
        Text("서비스명, 실제 금액과 통화, 올바른 날짜를 확인하세요. 채널 구독은 채널명도 필요합니다.",
            color = MaterialTheme.colorScheme.error)
    }
    if (saveState == SaveState.Failed) {
        Text("저장하지 못했습니다. 다시 시도하세요.", color = MaterialTheme.colorScheme.error)
    }
    Button(
        onClick = {
            submitted = true
            if (!invalid && amount != null && scheduledDate != null) {
                onSave(initial?.id, SubscriptionFields(
                    name.trim(), amount, cycle, scheduledDate, category.trim(), memo.trim(), active,
                    plan.trim(), channel.trim(), creator.trim(), currency, renewal, reminder,
                    if (viaPurchase) enteredDate
                    else fields?.purchaseDate?.takeIf { fields.date == scheduledDate && fields.cycle == cycle }
                ))
            }
        },
        enabled = saveState == SaveState.Idle || saveState == SaveState.Failed,
        modifier = Modifier.fillMaxWidth()
    ) { Text(if (saveState == SaveState.Saving) "저장 중…" else "저장") }
}

@Composable
private fun EntryField(value: String, change: (String) -> Unit, label: String, error: Boolean = false) {
    OutlinedTextField(value, change, label = { Text(label) }, singleLine = true,
        isError = error, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun <T> OptionMenu(label: String, selected: T, values: List<T>, display: (T) -> String, change: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("$label · ${display(selected)}") }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(text = { Text(display(value)) }, onClick = {
                    expanded = false
                    change(value)
                })
            }
        }
    }
}
