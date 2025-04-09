package com.project2.domain.rank.enums

import java.time.LocalDateTime

enum class RankingPeriod(private val months: Int) {
    ONE_MONTH(1),
    THREE_MONTHS(3),
    SIX_MONTHS(6);

    fun getStartDate(): LocalDateTime {
        return LocalDateTime.now().minusMonths(months.toLong())
    }
}