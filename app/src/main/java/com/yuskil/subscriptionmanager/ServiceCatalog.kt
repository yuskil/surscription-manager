package com.yuskil.subscriptionmanager

// 한국 신규 가입 공개 자료 기준, 2026-11-20.
internal val serviceCatalog = buildList {
    service("유튜브", "영상", "YouTube|유튜브 프리미엄") {
        plan("프리미엄", "웹·Android", 14900)
        plan("프리미엄 라이트", "웹·Android", 8500)
        plan("뮤직 프리미엄", "웹·Android", 11990)
    }
    service("티빙", "영상", "TVING") {
        plan("광고형 스탠다드", "웹", 5500)
        plan("베이직", "웹", 9500)
        plan("스탠다드", "웹", 13500)
        plan("프리미엄", "웹", 17000)
        plan("베이직", "웹", null, cycle = Cycle.Yearly, renewal = Renewal.Auto, note = "연간 요금은 현재 결제 화면에서 확인해 주세요.")
        plan("스탠다드", "웹", null, cycle = Cycle.Yearly, renewal = Renewal.Auto, note = "연간 요금은 현재 결제 화면에서 확인해 주세요.")
        plan("프리미엄", "웹", null, cycle = Cycle.Yearly, renewal = Renewal.Auto, note = "연간 요금은 현재 결제 화면에서 확인해 주세요.")
    }
    service("넷플릭스", "영상", "Netflix") {
        plan("광고형 스탠다드", "웹", 7000)
        plan("스탠다드", "웹", 13500)
        plan("프리미엄", "웹", 17000)
    }
    service("노벨피아", "독서", "Novelpia") {
        plan("PLUS", "웹", 14900)
        plan("Chat+", "웹", null)
    }
    service("밀리의서재", "독서", "밀리의 서재|Millie") {
        plan("전자책", "웹", 11900)
        plan("전자책", "웹", 119000, cycle = Cycle.Yearly)
        plan("전자책", "App Store", 14900)
        plan("전자책", "App Store", 149000, cycle = Cycle.Yearly)
    }
    service("ChatGPT", "AI", "챗GPT|챗지피티|OpenAI") {
        plan("Go", "App Store", 13000)
        plan("Plus", "App Store", 29000)
        plan("Go", "웹", null)
        plan("Plus", "웹", null)
        plan("Pro 100", "웹", null)
        plan("Pro 200", "웹", null)
        plan("Pro 500", "웹", null)
    }
    service("Gemini", "AI", "제미나이|Google AI|구글 AI") {
        plan("Google AI Plus 400GB", "웹", 7500)
        plan("Google AI Plus 400GB", "App Store", 75000, cycle = Cycle.Yearly)
        plan("Google AI Plus 2TB", "웹", null)
        plan("Google AI Plus 2TB", "웹", null, cycle = Cycle.Yearly)
        plan("Google AI Pro 5TB", "웹", 29000)
        plan("Google AI Pro 5TB", "App Store", 290000, cycle = Cycle.Yearly)
        plan("Google AI Ultra 5배", "웹", 119000)
        plan("Google AI Ultra 20배", "웹", 300000)
    }
    service("Claude", "AI", "클로드|Anthropic") {
        plan("Pro", "App Store", 33000)
        plan("Pro", "App Store", 349000, cycle = Cycle.Yearly)
        plan("Max", "App Store", 199000)
        plan("Ultra", "App Store", 449000)
        plan("Pro", "웹", null)
        plan("Pro", "웹", null, cycle = Cycle.Yearly)
        plan("Max", "웹", null)
        plan("Ultra", "웹", null)
    }
    service("치지직", "채널 구독", "치치직|CHZZK") {
        plan("팬 구독(1티어)", "웹", 4900, renewal = Renewal.Auto, creatorRequired = true)
        plan("형광팬 구독(2티어)", "웹", 14900, renewal = Renewal.Auto, creatorRequired = true)
        plan("팬 구독(1티어)", "Android 인앱", null, renewal = Renewal.Auto, creatorRequired = true)
        plan("팬 구독 1개월권", "App Store", null, cycle = Cycle.Monthly, renewal = Renewal.Prepaid, creatorRequired = true)
    }
    service("SOOP", "방송", "숲|아프리카TV") {
        plan("채널 구독 자동결제", "SOOP 직접 결제", 4500, cycle = Cycle.ThirtyDays, renewal = Renewal.Auto, creatorRequired = true)
        plan("베이직 구독 1개월", "App Store", 6500, cycle = Cycle.Monthly, renewal = Renewal.Unknown, creatorRequired = true)
        plan("플러스 레벨1 구독 1개월", "App Store", 13600, cycle = Cycle.Monthly, renewal = Renewal.Unknown, creatorRequired = true)
        plan("퀵뷰 플러스 월 자동결제", "App Store", 6500)
        plan("퀵뷰 30일 이용권", "App Store", 5500, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
        plan("퀵뷰 플러스 30일 이용권", "App Store", 8800, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
    }
    service("유튜브", "채널 구독", "YouTube 채널 멤버십|유튜브 멤버십") {
        plan("채널 멤버십", "", null, renewal = Renewal.Auto, creatorRequired = true)
    }
    service("Patreon", "채널 구독", "패트리온|페이트리온") {
        plan("크리에이터 멤버십", "", null, cycle = Cycle.Unknown, renewal = Renewal.Unknown, note = "크리에이터별 청구 방식과 실제 통화를 확인해 주세요.", creatorRequired = true)
    }
    service("위버스", "채널 구독", "Weverse|위버스 DM") {
        plan("Weverse DM App 월 구독", "App Store", null, renewal = Renewal.Auto, note = "안내 가격은 17 Jelly입니다. 실제 청구액을 입력하세요.", creatorRequired = true)
        plan("Weverse DM App 월 구독", "Google Play", null, renewal = Renewal.Auto, note = "안내 가격은 17 Jelly입니다. 실제 청구액을 입력하세요.", creatorRequired = true)
    }
    service("원신", "게임", "Genshin Impact|공월") {
        plan("공월 축복", "Xbox Store", 5900, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
        plan("공월 축복", "PlayStation Store", null, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid, note = "공식 상품 페이지 6,800원과 게임 소개 5,800원이 다릅니다.")
        plan("축복 패키지", "App Store", 7100, cycle = Cycle.Unknown, renewal = Renewal.Unknown, note = "App Store 표기 가격입니다. 공월 축복과 동일한 상품인지와 이용 기간은 확인이 필요합니다.")
    }
    service("명조: 워더링 웨이브", "게임", "명조|Wuthering Waves|달빛 관측 카드") {
        plan("달빛 관측 카드", "PlayStation Store", 6500, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
    }
    service("붕괴: 스타레일", "게임", "붕괴 스타레일|스타레일|HSR|Honkai Star Rail") {
        plan("열차 보급 허가증", "App Store", 5900, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
        plan("열차 보급 허가증", "PlayStation Store", 5900, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
    }
    service("젠레스 존 제로", "게임", "젠존제|ZZZ|Zenless Zone Zero") {
        plan("로프넷 회원", "App Store", 5900, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
        plan("로프넷 회원", "PlayStation Store", 5900, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
    }
    service("명일방주: 엔드필드", "게임", "엔드필드|Arknights Endfield") {
        plan("월정액", "PlayStation Store", 6800, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid)
    }
    service("블루 아카이브", "게임", "블루아카|Blue Archive") {
        plan("월간 청휘석 패키지", "App Store", 9900, cycle = Cycle.Unknown, renewal = Renewal.Unknown)
        plan("하프 월간 청휘석 패키지", "App Store", 4400, cycle = Cycle.Unknown, renewal = Renewal.Unknown)
    }
    service("에픽세븐", "게임", "Epic Seven") {
        plan("월정액 패키지", "App Store", 5500, cycle = Cycle.Unknown, renewal = Renewal.Unknown)
        plan("월정액 패키지2", "App Store", 11000, cycle = Cycle.Unknown, renewal = Renewal.Unknown)
    }
    service("천애명월도M", "게임", "천애명월도") {
        plan("일반 월정액", "App Store", 6000, cycle = Cycle.Unknown, renewal = Renewal.Unknown)
        plan("고급 월정액", "App Store", 15000, cycle = Cycle.Unknown, renewal = Renewal.Unknown)
    }
    service("파이널판타지14", "게임", "파판14|FF14|FFXIV|파이널 판타지") {
        plan("30일 이용권", "크리스탈샵", null, cycle = Cycle.ThirtyDays, renewal = Renewal.Prepaid, note = "19,800 크리스탈 상품입니다. 실제 청구액을 입력하세요.")
        plan("30일 이용권 자동결제", "크리스탈샵", null, cycle = Cycle.ThirtyDays, renewal = Renewal.Auto, note = "30일마다 19,800 크리스탈을 충전해 구매합니다. 실제 청구액을 입력하세요.")
    }
    service("EA Play", "게임", "일렉트로닉 아츠|EA app") {
        plan("EA Play", "EA 공식", 7000)
        plan("EA Play", "EA 공식", 54000, cycle = Cycle.Yearly)
        plan("EA Play Pro", "EA app", 22350)
        plan("EA Play Pro", "EA app", 133000, cycle = Cycle.Yearly)
    }
    service("GTA+", "게임", "GTA Plus|지티에이|그랜드 테프트 오토") {
        plan("GTA+", "PlayStation Store", 10780)
    }
    service("레인보우 식스 시즈", "게임", "R6|Rainbow Six Siege|레식") {
        plan("R6 멤버십", "PlayStation Store", 11900)
    }
    service("Ubisoft+", "게임", "Ubisoft Plus|유비소프트") {
        plan("Classics", "PlayStation Store", null, renewal = Renewal.Auto, note = "7,000원 표시와 세금 별도 안내가 함께 있어 최종가 확인이 필요합니다.")
    }
}

private fun MutableList<ServicePreset>.service(
    name: String,
    category: String,
    aliases: String,
    block: ServicePlans.() -> Unit
) {
    ServicePlans(this, name, category, aliases).block()
}

private class ServicePlans(
    private val catalog: MutableList<ServicePreset>,
    private val name: String,
    private val category: String,
    private val aliases: String
) {
    fun plan(
        name: String,
        channel: String,
        price: Long?,
        cycle: Cycle = Cycle.Monthly,
        renewal: Renewal = Renewal.Auto,
        note: String = "",
        creatorRequired: Boolean = false
    ) {
        catalog.add(ServicePreset(this.name, category, name, channel, price, cycle, renewal, creatorRequired, aliases, note))
    }
}
