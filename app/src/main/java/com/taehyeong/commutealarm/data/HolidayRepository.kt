package com.taehyeong.commutealarm.data

import java.time.DayOfWeek
import java.time.LocalDate

object HolidayRepository {
    
    // Korean fixed holidays (backup for API)
    private val KOREA_HOLIDAYS_2025 = listOf(
        "2025-01-01", // 신정
        "2025-01-28", "2025-01-29", "2025-01-30", // 설날
        "2025-03-01", // 삼일절
        "2025-05-05", // 어린이날
        "2025-05-06", // 석가탄신일
        "2025-06-06", // 현충일
        "2025-08-15", // 광복절
        "2025-10-03", // 개천절
        "2025-10-05", "2025-10-06", "2025-10-07", // 추석
        "2025-10-09", // 한글날
        "2025-12-25", // 성탄절
    )
    
    private val KOREA_HOLIDAYS_2026 = listOf(
        "2026-01-01", // 신정
        "2026-02-16", "2026-02-17", "2026-02-18", // 설날
        "2026-03-01", // 삼일절
        "2026-05-05", // 어린이날
        "2026-05-24", // 석가탄신일
        "2026-06-06", // 현충일
        "2026-08-15", // 광복절
        "2026-09-24", "2026-09-25", "2026-09-26", // 추석
        "2026-10-03", // 개천절
        "2026-10-09", // 한글날
        "2026-12-25", // 성탄절
    )
    
    private val cachedHolidays = mutableSetOf<String>().apply {
        addAll(KOREA_HOLIDAYS_2025)
        addAll(KOREA_HOLIDAYS_2026)
    }
    
    fun isHoliday(date: LocalDate): Boolean {
        return cachedHolidays.contains(date.toString())
    }
    
    fun isWeekend(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
    }
    
    fun isDayOff(date: LocalDate): Boolean {
        return isWeekend(date) || isHoliday(date)
    }
    
    // TODO: Implement API call to fetch holidays dynamically
    // suspend fun fetchHolidays(year: Int): List<String> { ... }
}
