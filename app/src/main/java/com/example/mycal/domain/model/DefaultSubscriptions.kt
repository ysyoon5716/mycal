package com.example.mycal.domain.model

object DefaultSubscriptions {

    data class DefaultCalendarSource(
        val name: String,
        val url: String,
        val color: Int = 0xFFFF0000.toInt() // Red color for holidays
    )

    val KOREAN_HOLIDAYS = DefaultCalendarSource(
        name = "대한민국 공휴일",
        url = "https://holidays.hyunbin.page/basic.ics",
        color = 0xFFFF0000.toInt()
    )

    val DEFAULT_SUBSCRIPTIONS = listOf(
        KOREAN_HOLIDAYS
    )
}