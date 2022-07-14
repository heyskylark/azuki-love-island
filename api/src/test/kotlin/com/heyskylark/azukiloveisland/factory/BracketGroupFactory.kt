package com.heyskylark.azukiloveisland.factory

import com.heyskylark.azukiloveisland.model.voting.BracketGroup

class BracketGroupFactory {
    companion object {
        fun createBracketGroups(
            startingId: Int = 1,
            numberOfGroups: Int = 1
        ): Set<BracketGroup> {
            val bracketGroups: MutableSet<BracketGroup> = mutableSetOf()

            for(roundNumber in startingId..numberOfGroups) {
                bracketGroups.add(
                    BracketGroup(
                        submissionId1 = "test-submission-${startingId}A",
                        submissionId2 = "test-submission-${startingId}B",
                        sortOrder = startingId
                    )
                )
            }

            return bracketGroups
        }

        fun cutBracketGroupsInHalf(bracketGroups: Set<BracketGroup>): Set<BracketGroup> {
            if (bracketGroups.size < 2) {
                throw RuntimeException("Cannot cut a bracket group of size ${bracketGroups.size} in half.")
            }

            val newBracketGroup: MutableSet<BracketGroup> = mutableSetOf()
            var lastSubmission: String? = null
            bracketGroups.forEach { group ->
                lastSubmission = if (lastSubmission != null) {
                    val bracketGroup = BracketGroup(
                        submissionId1 = group.submissionId1,
                        submissionId2 = lastSubmission,
                        sortOrder = newBracketGroup.size + 1
                    )

                    newBracketGroup.add(bracketGroup)
                    null
                } else {
                    group.submissionId1
                }
            }

            return newBracketGroup
        }
    }
}