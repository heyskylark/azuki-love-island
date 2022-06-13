interface ParticipantResponse {
    azukiId: number
    ownerAddress: string
    imageUrl: string
    backgroundTrait: string
    twitterHandle: string
    seasonNumber: number
    bio: string | undefined
    hobbies: string[] | undefined
    submitted: boolean
    validated: boolean
}

export default ParticipantResponse;
