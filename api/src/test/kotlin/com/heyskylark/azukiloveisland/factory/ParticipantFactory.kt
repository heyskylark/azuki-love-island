package com.heyskylark.azukiloveisland.factory

import com.heyskylark.azukiloveisland.model.Participant
import com.heyskylark.azukiloveisland.model.azuki.BackgroundTrait
import com.heyskylark.azukiloveisland.model.azuki.Gender
import java.net.URL

class ParticipantFactory {
    companion object {
        fun createParticipant(
            id: String,
            seasonNumber: Int,
            submitted: Boolean = true
        ): Participant {
            val azukiId = azukiIdFromString(id)

            return Participant(
                id = id,
                azukiId = azukiId.toLong(),
                ownerAddress = "$id-address",
                imageUrl = URL("https://$id.com"),
                backgroundTrait = BackgroundTrait.COOL_GRAY,
                twitterHandle = id.take(14),
                seasonNumber = seasonNumber,
                quote = "$id - quote",
                bio = "$id - bio",
                submitted = submitted,
                gender = if (azukiId % 2 == 0) {
                    Gender.MALE
                } else Gender.FEMALE
            )
        }

        fun azukiIdFromString(randomString: String): Int {
            return randomString.hashCode() % 10000
        }
    }
}