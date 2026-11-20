package com.yuskil.subscriptionmanager

import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Currency

internal enum class Renewal(val label: String) {
    Auto("자동 갱신"), Prepaid("선불 · 자동 갱신 없음"), Unknown("확인 필요")
}

internal fun Cycle.after(purchase: LocalDate, count: Long = 1): LocalDate = when (this) {
    Cycle.Monthly -> purchase.plusMonths(count)
    Cycle.Quarterly -> purchase.plusMonths(count * 3)
    Cycle.Yearly -> purchase.plusYears(count)
    Cycle.ThirtyDays -> purchase.plusDays(count * 30)
    Cycle.Unknown -> purchase
}

internal fun SubscriptionFields.eventDate(today: LocalDate): LocalDate? {
    if (today <= date) return date
    if (renewal != Renewal.Auto || cycle == Cycle.Unknown) return null

    val anchor = purchaseDate ?: date
    val elapsed = when (cycle) {
        Cycle.Monthly -> ChronoUnit.MONTHS.between(anchor, today)
        Cycle.Quarterly -> ChronoUnit.MONTHS.between(anchor, today) / 3
        Cycle.Yearly -> ChronoUnit.YEARS.between(anchor, today)
        Cycle.ThirtyDays -> ChronoUnit.DAYS.between(anchor, today) / 30
        Cycle.Unknown -> 0
    }
    val candidate = cycle.after(anchor, elapsed)
    return if (candidate < today) cycle.after(anchor, elapsed + 1) else candidate
}

internal val SubscriptionFields.eventLabel: String
    get() = when (renewal) {
        Renewal.Auto -> "다음 결제일"
        Renewal.Prepaid -> "혜택 만료일"
        Renewal.Unknown -> "확인 날짜"
    }

internal val SubscriptionFields.displayName: String
    get() = listOf(name, creator).filter(String::isNotBlank).joinToString(" · ")

internal fun parsePrice(value: String, code: String): Long? = runCatching {
    val decimals = Currency.getInstance(code).defaultFractionDigits
    require(decimals >= 0 && Regex("[0-9]+(\\.[0-9]+)?").matches(value.trim()))
    BigDecimal(value.trim()).movePointRight(decimals).longValueExact()
        .takeIf { it in 1..999_999_999 }
}.getOrNull()

internal fun priceInput(price: Long, code: String): String =
    BigDecimal.valueOf(price, Currency.getInstance(code).defaultFractionDigits).toPlainString()

internal fun formattedPrice(price: Long, code: String): String =
    if (code == "KRW") "%,d원".format(java.util.Locale.KOREA, price)
    else "${priceInput(price, code)} $code"
