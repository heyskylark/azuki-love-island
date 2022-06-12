package com.heyskylark.azukiloveisland.model

import java.time.Instant

data class Bracket(
    val startTime: Instant,
    val endTime: Instant,
    // Always even until final bracket which is 1 group
    // Sorted by groupNumber
    val status: Status = Status.ACTIVE,
    val seasonNumber: Int = 1
) {
    val id: String = "$seasonNumber.${startTime.toEpochMilli()}"
}

enum class Status {
    ACTIVE,
    COMPLETE
}