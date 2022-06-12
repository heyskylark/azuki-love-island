package com.heyskylark.azukiloveisland.model

import java.net.URL

data class Group(
    val groupNumber: Int,
    val participant1Id: Long,
    val participant2Id: Long,
    val participant1Votes: Int = 0,
    val participant2Votes: Int = 0,
    val twitterPollURL: URL? = null,
    val bracketId: String,
    val __v: Long = 1
) {
    val id: String = "$bracketId.$groupNumber"
}
