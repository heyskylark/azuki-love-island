interface ParticipantRequest {
    azukiId: string
    twitterHandle: string
    bio: string | undefined
    hobbies: string | undefined
}

export default ParticipantRequest;
