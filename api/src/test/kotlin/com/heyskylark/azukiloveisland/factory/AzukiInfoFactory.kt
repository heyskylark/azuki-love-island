package com.heyskylark.azukiloveisland.factory

import com.heyskylark.azukiloveisland.model.azuki.AzukiInfo
import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import com.heyskylark.azukiloveisland.model.azuki.Gender
import java.net.URL

class AzukiInfoFactory {
    companion object {
        const val DEFAULT_OWNER_ADDRESS = "0x0c9A1A471079995d034E7ddDa01aae6FDa66Bc7d"
        val DEFAULT_BACKGROUND_TRAIT = BackgroundTrait.RED

        fun createAzukiInfo(azukiId: Long): AzukiInfo {
            return AzukiInfo(
                azukiId = azukiId,
                azukiImageUrl = URL("https://ikzttp.mypinata.cloud/ipfs/QmYDvPAXtiJg7s8JdRBSLWdgSphQdac8j1YuQNNxcGE1hg/$azukiId.png"),
                ownerAddress = DEFAULT_OWNER_ADDRESS,
                backgroundTrait = DEFAULT_BACKGROUND_TRAIT,
                gender = Gender.UNDETERMINED
            )
        }
    }
}