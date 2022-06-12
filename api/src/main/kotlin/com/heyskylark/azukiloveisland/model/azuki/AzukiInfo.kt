package com.heyskylark.azukiloveisland.model.azuki

import java.net.URL

data class AzukiInfo(
    val azukiId: Long,
    val azukiImageUrl: URL,
    val ownerAddress: String,
    val backgroundTrait: BackgroundTrait,
    val gender: Gender = Gender.UNDETERMINED
)

enum class Gender {
    MALE,
    FEMALE,
    UNDETERMINED
}
