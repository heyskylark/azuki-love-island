package com.heyskylark.azukiloveisland.service

import java.time.Instant
import org.springframework.stereotype.Component

@Component("timeService")
class TimeService {
    fun getNow(): Instant {
        return Instant.now()
    }
}